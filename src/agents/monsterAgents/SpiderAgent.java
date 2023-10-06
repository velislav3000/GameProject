package agents.monsterAgents;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javax.imageio.ImageIO;

import agents.BaseAgent;
import agents.BaseEntityAgent;
import agents.BaseEntityAgent.StatusEffect;
import agents.behaviours.ExplorationBehaviour;
import agents.behaviours.FleeingBehaviour;
import agents.behaviours.IntentSenderMonsterBehaviour;
import agents.behaviours.LayEggSpiderBehaviour;
import agents.behaviours.StructureBuildingBehaviour;
import agents.heroAgents.BaseHeroAgent;
import agents.heroAgents.CityAgent;
import agents.monsterAgents.BaseMonsterAgent.MonsterTrait;
import collectiables.CollectableLoot;
import collectiables.chests.BaseChest;
import collectiables.chests.GoldChest;
import collectiables.chests.IronChest;
import collectiables.chests.WoodenChest;
import collectiables.materials.BaseMaterial.MaterialType;
import ontology.GameOntology;
import saveData.TileSaveData;

public class SpiderAgent extends BaseMonsterAgent{
	
	private int minDroppedString;
	private int maxDroppedString;
	private int layEggCooldown;
	
	private transient static BufferedImage agentImage;
	private static String agentBossImageName;
	private transient static BufferedImage agentBossImage;
	
	public SpiderAgent() {
		super();
		agentImageName = "agent_spider.png";
		agentBossImageName = "agent_spider_boss.png";
	}
	
	@Override
	protected void setup() {
		if(agentWasLoaded) {
			return;
		}
		
		monsterType = MonsterType.SPIDER;
		
		Random random = new Random();
		
		baseMaxHealth = random.nextInt(16)+25;
		currentHealth = baseMaxHealth;
		maxHealth = baseMaxHealth;
		attack = random.nextInt(8)+8;
		evasionChance = 5;
		critChance = 10;
		
		baseDroppedGold = 30;
		minDroppedString = 0;
		maxDroppedString = 5;
		
		layEggCooldown = 10;
		
		super.setup();
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
			
			if(layEggCooldown > 0) {
				layEggCooldown--;
			}
			
			if(hasStatusEffect(StatusEffect.FROZEN)) {
				return;
			}
		}
		
		perceiveSurroundings();
		perceiveNearbyAgents();
		updateAgentsInCombatWith();
		
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
		
		if(layEggCooldown == 0) {
			this.setBehaviour(BehaviourType.SPECIAL);
			this.addBehaviour(new LayEggSpiderBehaviour(this));
			return;
		}
		
		if(canBuildStructure()) {
			this.setBehaviour(BehaviourType.STRUCTURE_BUILDING);
			this.addBehaviour(new StructureBuildingBehaviour(this));
			return;
		}
		
		if(movableToTiles.isEmpty()) {
			this.setBehaviour(BehaviourType.IDLE);
		}
		else {
			this.setBehaviour(BehaviourType.EXPLORATION);
		}
		
		switch (behaviour) {
		case IDLE: {
			break;
		}
		case EXPLORATION: {
			addBehaviour(new ExplorationBehaviour(this));
			break;
		}
		}
	}
	
	@Override
	public CollectableLoot extractOwnLoot() {
		int gold = baseDroppedGold * level;
		
		Random random = new Random();
		int droppedString = random.nextInt(maxDroppedString + level/2) + minDroppedString + level/2;
		if(isBossMonster()) {
			droppedString = droppedString * 3;
		}
		if(hasTrait(MonsterTrait.MONEYBAGS)) {
			gold = gold * 3;
			droppedString = droppedString * 3;
		}
		HashMap<MaterialType, Integer> droppedMaterials = new HashMap<>();
		droppedMaterials.put(MaterialType.STRING, droppedString);
		
		return new CollectableLoot(gold, droppedMaterials, null, null);
	}
	
	@Override
	protected void levelUp() {
		int oldLevel = level;
		int oldHealth = baseMaxHealth;
		int oldAttack = attack;
		
		Random random = new Random();
		int healthIncrease = Math.round(baseMaxHealth * (random.nextInt(6)+5) * 0.01f);
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

	public int getPerceivedAgentsNum() {
		return perceivedAgents.size();
	}

	public void setLayEggCooldown(int layEggCooldown) {
		this.layEggCooldown = layEggCooldown;
	}
	
	@Override
	public void onDealtDamage(BaseAgent target) {
		super.onDealtDamage(target);
		if(isBossMonster() && target.isEntityAgent()) {
			Random random = new Random();
			int chance = random.nextInt(4);
			if(chance == 0) {
				((BaseEntityAgent)target).addStatusEffect(StatusEffect.POISON, 5);
			}
		}
	}
	
	@Override
	public BufferedImage getImage() {
		if(agentImage == null) {
			try {
				agentImage = ImageIO.read(new File(agentImagePath, agentImageName));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(agentBossImage == null) {
			try {
				agentBossImage = ImageIO.read(new File(agentImagePath, agentBossImageName));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if(isBossMonster()) {
			return agentBossImage;
		}
		return agentImage;
	}
	
	@Override
	public void loadAgent(TileSaveData save) {
		agentImageName = "agent_spider.png";
		agentBossImageName = "agent_spider_boss.png";
		super.loadAgent(save);
	}
}
