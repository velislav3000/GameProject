package agents.monsterAgents;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javax.imageio.ImageIO;

import agents.AgentUtils;
import agents.BaseAgent;
import agents.BaseEntityAgent.StatusEffect;
import agents.behaviours.EatMaterialGolemBehaviour;
import agents.behaviours.ExplorationBehaviour;
import agents.behaviours.FleeingBehaviour;
import agents.behaviours.IntentSenderMonsterBehaviour;
import agents.behaviours.SpawnMaterialGolemBehaviour;
import agents.monsterAgents.BaseMonsterAgent.MonsterTrait;
import collectiables.CollectableLoot;
import collectiables.chests.BaseChest;
import collectiables.chests.GoldChest;
import collectiables.chests.IronChest;
import collectiables.chests.WoodenChest;
import collectiables.materials.AdamantiteMaterial;
import collectiables.materials.BaseMaterial;
import collectiables.materials.CobaltMaterial;
import collectiables.materials.BaseMaterial.MaterialType;
import collectiables.materials.IronMaterial;
import collectiables.materials.MythrilMaterial;
import collectiables.materials.StoneMaterial;
import ontology.GameOntology;
import saveData.TileSaveData;
import tiles.Tile;

public class GolemAgent extends BaseMonsterAgent{

	public enum GolemStage{
		STONE,
		IRON,
		COBALT,
		MYTHRIL,
		ADAMANTITE
	}
	
	private GolemStage stage;
	
	private int spawnMaterialCooldown;
	
	private int minDroppedMaterial;
	private int maxDroppedMaterial;
	
	//Collectible gathering
	private int collectedMaterials;
	private int neededMaterials;
	private MaterialType neededMaterialType;
	private final int MAX_DISTANCE_TO_COLLECTABLE = 15;
	private transient Tile targetTileWithCollectable;
	private transient HashMap<Tile, Integer> tilesWithCollectable = new HashMap<>();
	
	private static String agentStoneImageName;
	private static String agentIronImageName;
	private static String agentCobaltImageName;
	private static String agentMythrilImageName;
	private static String agentAdamantiteImageName;
	private transient static BufferedImage agentStoneImage;
	private transient static BufferedImage agentIronImage;
	private transient static BufferedImage agentCobaltImage;
	private transient static BufferedImage agentMythrilImage;
	private transient static BufferedImage agentAdamantiteImage;
	private static String agentStoneBossImageName;
	private static String agentIronBossImageName;
	private static String agentCobaltBossImageName;
	private static String agentMythrilBossImageName;
	private static String agentAdamantiteBossImageName;
	private transient static BufferedImage agentStoneBossImage;
	private transient static BufferedImage agentIronBossImage;
	private transient static BufferedImage agentCobaltBossImage;
	private transient static BufferedImage agentMythrilBossImage;
	private transient static BufferedImage agentAdamantiteBossImage;
	
	public GolemAgent() {
		super();
		
		stage = GolemStage.STONE;
		neededMaterialType = MaterialType.IRON;
		
		agentStoneImageName = "agent_golem_stone.png";
		agentIronImageName = "agent_golem_iron.png";
		agentCobaltImageName = "agent_golem_cobalt.png";
		agentMythrilImageName = "agent_golem_mythril.png";
		agentAdamantiteImageName = "agent_golem_adamantite.png";
		agentStoneBossImageName = "agent_golem_stone_boss.png";
		agentIronBossImageName = "agent_golem_iron_boss.png";
		agentCobaltBossImageName = "agent_golem_cobalt_boss.png";
		agentMythrilBossImageName = "agent_golem_mythril_boss.png";
		agentAdamantiteBossImageName = "agent_golem_adamantite_boss.png";
	}
	
	@Override
	protected void setup() {
		if(agentWasLoaded) {
			return;
		}
		
		monsterType = MonsterType.GOLEM;
		
		Random random = new Random();
		
		baseMaxHealth = random.nextInt(20)+50;
		currentHealth = baseMaxHealth;
		maxHealth = baseMaxHealth;
		attack = random.nextInt(10)+10;
		evasionChance = 0;
		critChance = 10;
		
		baseDroppedGold = 65;
		minDroppedMaterial = 1;
		maxDroppedMaterial = 10;
		
		super.setup();
		
		collectedMaterials = 0;
		if(isBossMonster()) {
			neededMaterials = 10;
		}
		else {
			neededMaterials = 6;
		}
		
		spawnMaterialCooldown = 15;
	}
	
	@Override
	public void update() {
		if(!isAlive()) {
			return;
		}
		
		if(!alreadyCommunicated) {
			regenarateHealth();
			looseHealth();
			
			changeCurrentTile();

			if(hasStatusEffect(StatusEffect.FROZEN)) {
				updateStatusEffects();
				return;
			}
			
			updateStatusEffects();
			movedTwice = false;

			if(callAlliesCooldown > 0) {
				callAlliesCooldown--;
			}
			
			if(isBossMonster() && spawnMaterialCooldown > 0) {
				spawnMaterialCooldown--;
			}
		}
		
		perceiveSurroundings();
		perceiveNearbyAgents();
		updateAgentsInCombatWith();
		updateKnownCollectables();
		perceiveNearbyCollectables();
		
		if(!perceivedAgents.isEmpty() && alreadyCommunicated == false) {
			alreadyCommunicated = true;
			addBehaviour(new IntentSenderMonsterBehaviour(this, perceivedAgents));
			return;
		}
		
		alreadyCommunicated = false;
		
		if(behaviour == BehaviourType.COMBAT && combatTargetAgent != null) {
				addCombatBehaviour();
				return;
		}
		
		if(behaviour == BehaviourType.FLEEING && isTargetedByOtherAgents()){
			addBehaviour(new FleeingBehaviour(this));
			return;
		}
		
		if(isBossMonster() && spawnMaterialCooldown == 0) {
			spawnMaterialCooldown = 15;
			addBehaviour(new SpawnMaterialGolemBehaviour(this));
			return;
		}
		
		if(!tilesWithCollectable.isEmpty()) {
			
			if(targetTileWithCollectable != null
					&& (targetTileWithCollectable.getCollectable() == null || !targetTileWithCollectable.getCollectable().canBeCollected())
					&& (targetTileWithCollectable.getAgent() != null && targetTileWithCollectable.getAgent() != this)){
				
				Random random = new Random();
				int chance = random.nextInt(20);
				if(chance == 0) {
					BaseAgent looterAgent = targetTileWithCollectable.getAgent();
					lowerRelationshipWithAgent(looterAgent.getLocalName(), looterAgent.getDisplayName());
				}
			}
			
			selectClosestCollectables();
			if(targetTileWithCollectable != null) {
				calculatePathToTile(targetTileWithCollectable, currentTile);
				this.setBehaviour(BehaviourType.GATHER_MATERIAL);
			}
			else {
				tilesWithCollectable.clear();
				this.setBehaviour(BehaviourType.EXPLORATION);
			}
		}
		else {
			targetTileWithCollectable = null;
			this.setBehaviour(BehaviourType.EXPLORATION);
		}
		
		if(movableToTiles.isEmpty()) {
			this.setBehaviour(BehaviourType.IDLE);
		}
		
		switch (behaviour) {
		case IDLE: {
			break;
		}
		case EXPLORATION: {
			addBehaviour(new ExplorationBehaviour(this));
			break;
		}
		case GATHER_MATERIAL: {
			addBehaviour(new EatMaterialGolemBehaviour(this));
			break;
		}
		}
	}
	
	private void perceiveNearbyCollectables() {
		for (Tile tile : perceivedTiles) {
			if(tile.getCollectable() != null && tile.getCollectable().canBeCollected()
					&& tile.getCollectable().getType().contentEquals(neededMaterialType.toString())) {
				calculatePathToTile(tile, currentTile);
				if(pathToTile.size() < MAX_DISTANCE_TO_COLLECTABLE && (!pathToTile.isEmpty() || tile.getAgent() == this)) {
					tilesWithCollectable.put(tile, pathToTile.size());
					addToLog("I see " + tile.getCollectable().getType());
				}
			}
		}
	}
	
	private void updateKnownCollectables() {
		ArrayList<Tile> tilesToRemove = new ArrayList<>();
		for (Tile tile : tilesWithCollectable.keySet()) {
			if(tile.getCollectable() == null) {
				tilesToRemove.add(tile);
				continue;
			}
			
			calculatePathToTile(tile, currentTile);
			tilesWithCollectable.put(tile, pathToTile.size());
			if(pathToTile.size() > MAX_DISTANCE_TO_COLLECTABLE || (pathToTile.isEmpty() && tile.getAgent() != this)) {
				tilesToRemove.add(tile);
			}
		}
		
		for (Tile tile : tilesToRemove) {
			tilesWithCollectable.remove(tile);
		}
	}
	
	private void selectClosestCollectables() {
		float closestDistance = Float.MAX_VALUE;
		
		for (Tile tile : tilesWithCollectable.keySet()) {
			float distanceToMaterial = tilesWithCollectable.get(tile);
			if(tile.getCollectable().canBeCollected() &&  closestDistance > distanceToMaterial) {
				closestDistance = distanceToMaterial;
				targetTileWithCollectable = tile;
			}
		}
	}
	
	@Override
	public CollectableLoot extractOwnLoot() {
		int gold = baseDroppedGold * level;
		
		Random random = new Random();
		int droppedMaterial = random.nextInt(maxDroppedMaterial + level/2) + minDroppedMaterial + level/2;
		if(isBossMonster()) {
			droppedMaterial = droppedMaterial * 3;
		}
		if(hasTrait(MonsterTrait.MONEYBAGS)) {
			gold = gold * 3;
			droppedMaterial = droppedMaterial * 3;
		}
		HashMap<MaterialType, Integer> droppedMaterials = new HashMap<>();
		droppedMaterials.put(getDroppedMaterial(), droppedMaterial);
		
		return new CollectableLoot(gold, droppedMaterials, null, null);
	}
	
	@Override
	public void onDeath() {
		createMaterialOnDeath();
		detonate();
	}
	
	private void createMaterialOnDeath() {
		if(currentTile.getCollectable() != null) {
			return;
		}
		
		BaseMaterial material = null;
		
		switch (stage) {
		case STONE: {
			material = new StoneMaterial();
			break;
		}
		case IRON: {
			material = new IronMaterial();
			break;
		}
		case COBALT: {
			material = new CobaltMaterial();
			break;
		}
		case MYTHRIL: {
			material = new MythrilMaterial();
			break;
		}
		case ADAMANTITE: {
			material = new AdamantiteMaterial();
			break;
		}
		}
		currentTile.setCollectable(material);
		AgentUtils.materialsArray.add(material);
	}
	
	private MaterialType getDroppedMaterial() {
		switch (stage) {
		case STONE: {
			return MaterialType.STONE;
		}
		case IRON: {
			return MaterialType.IRON;
		}
		case COBALT: {
			return MaterialType.COBALT;
		}
		case MYTHRIL: {
			return MaterialType.MYTHRIL;
		}
		case ADAMANTITE: {
			return MaterialType.ADAMANTITE;
		}
		default:
			return MaterialType.STONE;
		}
	}
	
	@Override
	protected void levelUp() {
		int oldLevel = level;
		int oldHealth = baseMaxHealth;
		int oldAttack = attack;
		
		Random random = new Random();
		int healthIncrease = Math.round(baseMaxHealth * (random.nextInt(9)+7) * 0.01f);
		baseMaxHealth += healthIncrease;
		maxHealth = baseMaxHealth;
		currentHealth += healthIncrease;
		attack += Math.round(attack * (random.nextInt(6)+5) * 0.01f);
		level += 1;
		
		//System.out.println("LEVEL UP: " + getLocalName() + " is now level " + level);
		addToLog("I leveled up to level " + level);
		
		GameOntology.updateEntityAgentStats(this, oldLevel, oldHealth, oldAttack);
		
		neededXP = (int) Math.pow(2, level);
		gainXP(0);
	}
	
	public void eatMaterial() {
		currentTile.collectCollectable();
		if(stage == GolemStage.ADAMANTITE) {
			heal(Math.round(getMaxHealth() * 0.1f));
		}
		else {
			collectedMaterials += 1;
			if(collectedMaterials == neededMaterials) {
				riseStage();
				collectedMaterials = 0;
				tilesWithCollectable.clear();
			}
		}
	}
	
	private void riseStage() {
		switch (stage) {
		case STONE: {
			stage = GolemStage.IRON;
			neededMaterialType = MaterialType.COBALT;
			break;
		}
		case IRON: {
			stage = GolemStage.COBALT;
			neededMaterialType = MaterialType.MYTHRIL;
			break;
		}
		case COBALT: {
			stage = GolemStage.MYTHRIL;
			neededMaterialType = MaterialType.ADAMANTITE;
			break;
		}
		case MYTHRIL: {
			stage = GolemStage.ADAMANTITE;
			break;
		}
		}
		
		int healthIncrease = Math.round(baseMaxHealth * 0.3f);
		baseMaxHealth += healthIncrease;
		maxHealth = baseMaxHealth;
		currentHealth += healthIncrease;
		
		attack += Math.round(attack * 0.3f);
	}

	@Override
	public BufferedImage getImage() {
		if(agentStoneImage == null) {
			try {
				agentStoneImage = ImageIO.read(new File(agentImagePath, agentStoneImageName));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(agentIronImage == null) {
			try {
				agentIronImage = ImageIO.read(new File(agentImagePath, agentIronImageName));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(agentCobaltImage == null) {
			try {
				agentCobaltImage = ImageIO.read(new File(agentImagePath, agentCobaltImageName));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(agentMythrilImage == null) {
			try {
				agentMythrilImage = ImageIO.read(new File(agentImagePath, agentMythrilImageName));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(agentAdamantiteImage == null) {
			try {
				agentAdamantiteImage = ImageIO.read(new File(agentImagePath, agentAdamantiteImageName));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(agentStoneBossImage == null) {
			try {
				agentStoneBossImage = ImageIO.read(new File(agentImagePath, agentStoneBossImageName));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(agentIronBossImage == null) {
			try {
				agentIronBossImage = ImageIO.read(new File(agentImagePath, agentIronBossImageName));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(agentCobaltBossImage == null) {
			try {
				agentCobaltBossImage = ImageIO.read(new File(agentImagePath, agentCobaltBossImageName));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(agentMythrilBossImage == null) {
			try {
				agentMythrilBossImage = ImageIO.read(new File(agentImagePath, agentMythrilBossImageName));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(agentAdamantiteBossImage == null) {
			try {
				agentAdamantiteBossImage = ImageIO.read(new File(agentImagePath, agentAdamantiteBossImageName));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if(isBossMonster()) {
			switch (stage) {
			case STONE: {
				return agentStoneBossImage;
			}
			case IRON: {
				return agentIronBossImage;
			}
			case COBALT: {
				return agentCobaltBossImage;
			}
			case MYTHRIL: {
				return agentMythrilBossImage;
			}
			case ADAMANTITE: {
				return agentAdamantiteBossImage;
			}
			}
		}
		else {
			switch (stage) {
			case STONE: {
				return agentStoneImage;
			}
			case IRON: {
				return agentIronImage;
			}
			case COBALT: {
				return agentCobaltImage;
			}
			case MYTHRIL: {
				return agentMythrilImage;
			}
			case ADAMANTITE: {
				return agentAdamantiteImage;
			}
			}
		}
		return agentStoneImage;
	}
	
	public Tile getTileWithMaterial() {
		return targetTileWithCollectable;
	}

	public void setTileWithMaterial(Tile tileWithMaterial) {
		this.targetTileWithCollectable = tileWithMaterial;
	}

	public GolemStage getStage() {
		return stage;
	}
	
	@Override
	public void loadAgent(TileSaveData save) {
		super.loadAgent(save);
		tilesWithCollectable = new HashMap<>();
		
		agentStoneImageName = "agent_golem_stone.png";
		agentIronImageName = "agent_golem_iron.png";
		agentCobaltImageName = "agent_golem_cobalt.png";
		agentMythrilImageName = "agent_golem_mythril.png";
		agentAdamantiteImageName = "agent_golem_adamantite.png";
		agentStoneBossImageName = "agent_golem_stone_boss.png";
		agentIronBossImageName = "agent_golem_iron_boss.png";
		agentCobaltBossImageName = "agent_golem_cobalt_boss.png";
		agentMythrilBossImageName = "agent_golem_mythril_boss.png";
		agentAdamantiteBossImageName = "agent_golem_adamantite_boss.png";
	}
}
