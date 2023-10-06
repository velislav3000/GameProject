package agents.monsterAgents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import agents.AgentUtils;
import agents.BaseAgent;
import agents.BaseEntityAgent;
import agents.BaseEntityAgent.BehaviourType;
import agents.BaseEntityAgent.RelationshipType;
import agents.BaseEntityAgent.StatusEffect;
import agents.behaviours.CombatBehaviour;
import agents.behaviours.ExplorationBehaviour;
import agents.behaviours.FleeingBehaviour;
import agents.behaviours.IntentSenderMonsterBehaviour;
import agents.behaviours.MessageProcessingBehaviour;
import agents.behaviours.StructureBuildingBehaviour;
import agents.heroAgents.BaseHeroAgent;
import agents.heroAgents.BaseHeroAgent.HeroAbilityTrait;
import agents.heroAgents.BaseHeroAgent.HeroPersonalityTrait;
import collectiables.CollectableLoot;
import collectiables.chests.BaseChest;
import collectiables.chests.GoldChest;
import collectiables.chests.IronChest;
import collectiables.chests.WoodenChest;
import ontology.GameOntology;
import selection.ToolSelection;
import tiles.Tile;
import tiles.Tile.TileType;

public class BaseMonsterAgent extends BaseEntityAgent {

	protected int baseDroppedGold;
	
	public enum MonsterType{
		SKELETON,
		SPIDER,
		SPIDER_EGG,
		SHROOMER,
		GOLEM
	}
	
	protected MonsterType monsterType;
	
	public enum MonsterTrait{
		FEROCIOUS,
		TAME,
		ARMORED,
		FRAGILE,
		BOSS,
		DETONATOR,
		MONEYBAGS,
		BURNING_WASTELAND,
		FROZEN_WASTELAND,
		RANDOM
	}
	
	protected ArrayList<MonsterTrait> traits;
	
	public BaseMonsterAgent() {
		super();
	}
	
	@Override
	protected void setup() {
		super.setup();
		
		traits = new ArrayList<>();
		if(!spawnerName.contentEquals("null") || ToolSelection.selectedMonsterTraits.contains(MonsterTrait.RANDOM)) {
			setRandomTraits();
		}
		else {
			for (MonsterTrait monsterTrait : ToolSelection.selectedMonsterTraits) {
				traits.add(MonsterTrait.valueOf(monsterTrait.toString()));
			}
		}
		
		addTraitBonusesToStats();
		
		GameOntology.addMonsterAgent(this);
		
		if(spawnStructure != null && spawnStructure.getOwner() != null) {
			BaseEntityAgent structureOwner = spawnStructure.getOwner();
			
			setRelationshipWithAgent(structureOwner.getLocalName(), structureOwner.getDisplayName(), RelationshipType.ALLIES);
		}
		
		addMessageProcessingBehavior();
		
		AgentUtils.agentsArray.add(this);
	}
	
	protected void setRandomTraits() {
		Random random = new Random();
		int numOfAbilityTraits = random.nextInt(MonsterTrait.values().length-5);
		ArrayList<MonsterTrait> avaliableTraits = new ArrayList<>(Arrays.asList(MonsterTrait.values()));
		avaliableTraits.remove(MonsterTrait.RANDOM);
		avaliableTraits.remove(MonsterTrait.BOSS);
		
		for(int i = 0; i < numOfAbilityTraits; i++) {
			int index = random.nextInt(avaliableTraits.size());
			MonsterTrait chosenTrait = avaliableTraits.get(index);
			
			traits.add(chosenTrait);
			avaliableTraits.remove(chosenTrait);
			
			if(chosenTrait.equals(MonsterTrait.FEROCIOUS)){
				avaliableTraits.remove(MonsterTrait.TAME);
			}
			else if(chosenTrait.equals(MonsterTrait.TAME)){
				avaliableTraits.remove(MonsterTrait.FEROCIOUS);
			}
			else if(chosenTrait.equals(MonsterTrait.ARMORED)){
				avaliableTraits.remove(MonsterTrait.FRAGILE);
			}
			else if(chosenTrait.equals(MonsterTrait.FRAGILE)){
				avaliableTraits.remove(MonsterTrait.ARMORED);
			}
			else if(chosenTrait.equals(MonsterTrait.BURNING_WASTELAND)){
				avaliableTraits.remove(MonsterTrait.FROZEN_WASTELAND);
			}
			else if(chosenTrait.equals(MonsterTrait.FROZEN_WASTELAND)){
				avaliableTraits.remove(MonsterTrait.BURNING_WASTELAND);
			}
		}
		
		int chance = random.nextInt(20);
		if(chance == 0 && !spawnerName.contains("Skeleton")) {
			traits.add(MonsterTrait.BOSS);
		}
	}
	
	protected void addMessageProcessingBehavior() {
		this.addBehaviour(new MessageProcessingBehaviour(this));
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
	
	protected void changeCurrentTile() {
		if(level >= 10) {
			if(hasTrait(MonsterTrait.BURNING_WASTELAND) && currentTile.getTileType() != TileType.SAND &&
					currentTile.getTileType() != TileType.BURNING_SAND) {
				currentTile.setTileType(TileType.SAND);
			}
			else if(hasTrait(MonsterTrait.FROZEN_WASTELAND) && currentTile.getTileType() != TileType.SNOW &&
					currentTile.getTileType() != TileType.ICE) {
				currentTile.setTileType(TileType.SNOW);
			}
		}
	}
	
	@Override
	public void onDealtDamage(BaseAgent target) {
		if(hasTrait(MonsterTrait.BURNING_WASTELAND)) {
			Random random = new Random();
			int chance = random.nextInt(10);
			if(chance == 0) {
				((BaseEntityAgent)target).addStatusEffect(StatusEffect.BURNING, 2);
			}
		}
		if(hasTrait(MonsterTrait.FROZEN_WASTELAND)) {
			Random random = new Random();
			int chance = random.nextInt(15);
			if(chance == 0) {
				((BaseEntityAgent)target).addStatusEffect(StatusEffect.FROZEN, 1);
			}
		}
	}
	
	protected void addTraitBonusesToStats() {
		
		float goldDropMultiplier = 1;
		
		if(traits.contains(MonsterTrait.FEROCIOUS)) {
			attack = Math.round(attack * 1.5f);
			goldDropMultiplier += 0.5;
		}
		else if(traits.contains(MonsterTrait.TAME)) {
			attack = Math.round(attack * 0.75f);
			goldDropMultiplier -= 0.25;
		}
		
		if(traits.contains(MonsterTrait.ARMORED)) {
			baseMaxHealth = Math.round(baseMaxHealth * 1.5f);
			currentHealth = baseMaxHealth;
			maxHealth = baseMaxHealth;
			goldDropMultiplier += 0.5;
		}
		else if(traits.contains(MonsterTrait.FRAGILE)) {
			baseMaxHealth = Math.round(baseMaxHealth * 0.75f);
			currentHealth = baseMaxHealth;
			maxHealth = baseMaxHealth;
			goldDropMultiplier -= 0.25;
		}
		
		if(traits.contains(MonsterTrait.BOSS)) {
			attack = Math.round(attack * 3);
			baseMaxHealth = Math.round(baseMaxHealth * 3f);
			currentHealth = baseMaxHealth;
			maxHealth = baseMaxHealth;
			goldDropMultiplier += 3;
		}
		
		baseDroppedGold = Math.round(baseDroppedGold * goldDropMultiplier);
	}
	
	@Override
	public CollectableLoot extractOwnLoot() {
		int gold = baseDroppedGold * level;
		
		if(hasTrait(MonsterTrait.MONEYBAGS)) {
			gold = gold * 3;
		}
		
		return new CollectableLoot(gold, new HashMap<>(), null, null);
	}
	
	@Override
	public void onDeath() {
		
		if(isBossMonster()) {
			if(currentTile.getCollectable() == null) {
				BaseChest chest = null;
				
				if(level >= 8) {
					chest = new GoldChest();
				}
				else {
					chest = new IronChest();
				}
				
				currentTile.setCollectable(chest);
			}
		}
		else {
			Random random = new Random();
			int chance = random.nextInt(10);
			if(chance == 0 && currentTile.getCollectable() == null) {
				BaseChest chest = null;
				
				if(level >= 14) {
					chest = new GoldChest();
				}
				else if(level >= 7) {
					chest = new IronChest();
				}
				else {
					chest = new WoodenChest();
				}
				
				currentTile.setCollectable(chest);
			}
		}
		
		detonate();
	}
	
	protected void detonate() {
		if(hasTrait(MonsterTrait.DETONATOR)) {
			for (Tile tile : currentTile.getNeighbours()) {
				if(tile.getAgent() != null) {
					tile.getAgent().takeDamage(getAttack() * 3, this);
				}
			}
		}
	}
	
	@Override
	protected void levelUp() {
		
	}
	
	public MonsterType getMonsterType() {
		return monsterType;
	}

	public ArrayList<MonsterTrait> getTraits() {
		return traits;
	}

	@Override
	public String getStringOfTraits() {
		String traits = "";
		
		for(MonsterTrait trait : this.traits) {
			if(trait == this.traits.get(0)) {
				traits += trait.toString();
			}
			else {
				traits += ", " + trait.toString();
			}
		}
		
		return traits;
	}

	@Override
	public void collectLoot(CollectableLoot loot) {
		
	}
	
	@Override
	public RelationshipType getRelationshipWithAgent(BaseAgent agent) {
		
		if(agent.equals(spawnStructure)) {
			return RelationshipType.ALLIES;
		}
		
		return super.getRelationshipWithAgent(agent);
	}
	
	public boolean isBossMonster() {
		return traits.contains(MonsterTrait.BOSS);
	}
	
	public boolean hasTrait(MonsterTrait trait) {
		return traits.contains(trait);
	}
	
	@Override
	public int getDroppedXP() {
		if(isBossMonster()) {
			return (int) Math.pow(2, level-1) * 3;
		}
		return super.getDroppedXP();
	}
}
