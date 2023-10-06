package agents.heroAgents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import agents.AgentUtils;
import agents.BaseAgent;
import agents.BaseEntityAgent;
import agents.BaseEntityAgent.BehaviourType;
import agents.BaseEntityAgent.StatusEffect;
import agents.behaviours.CombatBehaviour;
import agents.behaviours.CraftingBehaviour;
import agents.behaviours.ExplorationBehaviour;
import agents.behaviours.GatherCollectibleBehaviour;
import agents.behaviours.FleeingHeroBehaviour;
import agents.behaviours.MessageProcessingHeroBehaviour;
import agents.behaviours.StructureBuildingHeroBehaviour;
import agents.behaviours.IntentSenderHeroBehaviour;
import agents.behaviours.MessageProcessingBehaviour;
import agents.behaviours.StructureBuildingBehaviour;
import agents.behaviours.TradeInitiatorBehaviour;
import agents.heroAgents.BaseHeroAgent.HeroPersonalityTrait;
import agents.items.HeroInventory;
import agents.items.BaseItem;
import agents.items.BaseItem.ItemQuality;
import agents.items.equipment.Equipment;
import agents.items.equipment.Equipment.EquipmentType;
import agents.items.potions.FleeingPotion;
import agents.items.potions.HealthPotion;
import agents.items.potions.BasePotion;
import agents.items.potions.BasePotion.PotionType;
import collectiables.CollectableLoot;
import collectiables.chests.BaseChest.ChestType;
import collectiables.materials.BaseMaterial.MaterialType;
import ontology.GameOntology;
import saveData.TileSaveData;
import selection.ToolSelection;
import tiles.Tile;

public abstract class BaseHeroAgent extends BaseEntityAgent{
	
	//Collectible gathering
	protected final int MAX_DISTANCE_TO_COLLECTABLE = 15;
	protected transient Tile targetTileWithCollectable;
	protected transient HashMap<Tile, Integer> tilesWithCollectable = new HashMap<>();
	protected HashMap<String, Float> collectablePriorities;
	
	//Trading
	protected transient BaseAgent tradeAgent;
	protected boolean isTrading;
	protected int tradeCooldown = 0;
	
	protected transient HeroInventory inventory;

	public enum HeroType{
		WARRIOR,
		ROGUE,
		ARCHER
	}
	
	protected HeroType heroType;
	
	public enum HeroPersonalityTrait{
		BLOODTHIRSTY,
		GREEDY,
		BRAVE,
		COWARD,
		RANDOM
	}
	
	protected HeroPersonalityTrait personalityTrait;
	
	public enum HeroAbilityTrait{
		TANK,
		NIMBLE,
		PRECISE,
		WILD,
		BLACKSMITH,
		ALCHEMIST,
		RESOURCEFUL,
		POISON_DABLER,
		FROST_INFUSER,
		ANTIVENOM,
		BURNING_BLOOD,
		LAST_STAND,
		FORGE_DISASTER,
		HERB_WASTER,
		FAST_LEARNER,
		DIMWIT,
		POST_DEATH_BITTERNESS,
		RANDOM
	}
	
	protected ArrayList<HeroAbilityTrait> abilityTraits;
	
	public BaseHeroAgent() {
		super();
	}
	
	@Override
	protected void setup() {
		super.setup();
		
		Random random = new Random();
		
		if(spawnStructure != null) {
			personalityTrait = HeroPersonalityTrait.RANDOM;
		}
		else {
			personalityTrait = HeroPersonalityTrait.valueOf(ToolSelection.selectedHeroPersonalityTrait.toString());
		}
		
		if(personalityTrait == HeroPersonalityTrait.RANDOM) {
			int chance = random.nextInt(4);
			switch (chance) {
			case 0: {
				personalityTrait = HeroPersonalityTrait.BLOODTHIRSTY;
				break;
			}
			case 1: {
				personalityTrait = HeroPersonalityTrait.GREEDY;
				break;
			}
			case 2: {
				personalityTrait = HeroPersonalityTrait.BRAVE;
				break;
			}
			case 3: {
				personalityTrait = HeroPersonalityTrait.COWARD;
				break;
			}
			}
		}
		
		abilityTraits = new ArrayList<>();
		
		if(spawnStructure != null || ToolSelection.selectedHeroAbilityTraits.contains(HeroAbilityTrait.RANDOM)) {
			setRandomAbilityTraits();
		}
		else {
			for (HeroAbilityTrait abilityTrait : ToolSelection.selectedHeroAbilityTraits) {
				abilityTraits.add(HeroAbilityTrait.valueOf(abilityTrait.toString()));
			}
		}
		
		addTraitBonusesToStats();
		
		inventory = new HeroInventory(this);
		
		collectablePriorities = new HashMap<>();
		collectablePriorities.put(MaterialType.ADAMANTITE.toString(), 1F);
		collectablePriorities.put(ChestType.GOLD_CHEST.toString(), 1F);
		collectablePriorities.put(MaterialType.MYTHRIL.toString(), 1.25F);
		collectablePriorities.put(ChestType.IRON_CHEST.toString(), 1.35F);
		collectablePriorities.put(MaterialType.BLOODROSE.toString(), 1.35F);
		collectablePriorities.put(MaterialType.SHADOWSTALK.toString(), 1.5F);
		collectablePriorities.put(MaterialType.COBALT.toString(), 1.5F);
		collectablePriorities.put(MaterialType.MOONGLOW.toString(), 1.65F);
		collectablePriorities.put(ChestType.WOODEN_CHEST.toString(), 1.75F);
		collectablePriorities.put(MaterialType.DAYBLOOM.toString(), 2F);
		collectablePriorities.put(MaterialType.IRON.toString(), 2F);
		collectablePriorities.put(MaterialType.STONE.toString(), 2.5F);
		
		int startingGold = random.nextInt(151) + 50;//from 50 to 200
		
		if(personalityTrait == HeroPersonalityTrait.GREEDY) {
			startingGold = Math.round(startingGold * 1.5f);
		}
		
		inventory.addGold(startingGold);
		
		GameOntology.addHeroAgent(this);
		
		if(spawnStructure != null && spawnStructure.getOwner() != null) {
			int chance = random.nextInt(10);
			
			BaseEntityAgent structureOwner = spawnStructure.getOwner();
			
			if(chance == 0) {
				setRelationshipWithAgent(structureOwner.getLocalName(), structureOwner.getDisplayName(), RelationshipType.NEUTRAL);
			}
			else if(chance <= 2) {
				setRelationshipWithAgent(structureOwner.getLocalName(), structureOwner.getDisplayName(), RelationshipType.ALLIES);
			}
			else {
				setRelationshipWithAgent(structureOwner.getLocalName(), structureOwner.getDisplayName(), RelationshipType.FRIENDLY);
			}
		}
		
		addStartingItems();
		
		this.addBehaviour(new MessageProcessingHeroBehaviour(this));
		
		AgentUtils.agentsArray.add(this);
	}
	
	protected void setRandomAbilityTraits() {
		Random random = new Random();
		int numOfAbilityTraits = random.nextInt(HeroAbilityTrait.values().length-5);
		ArrayList<HeroAbilityTrait> avaliableAbilities = new ArrayList<>(Arrays.asList(HeroAbilityTrait.values()));
		avaliableAbilities.remove(HeroAbilityTrait.RANDOM);
		
		for(int i = 0; i < numOfAbilityTraits; i++) {
			int index = random.nextInt(avaliableAbilities.size());
			HeroAbilityTrait chosenTrait = avaliableAbilities.get(index);
			
			abilityTraits.add(chosenTrait);
			avaliableAbilities.remove(chosenTrait);
			
			if(chosenTrait.equals(HeroAbilityTrait.TANK)){
				avaliableAbilities.remove(HeroAbilityTrait.NIMBLE);
			}
			else if(chosenTrait.equals(HeroAbilityTrait.NIMBLE)){
				avaliableAbilities.remove(HeroAbilityTrait.TANK);
			}
			else if(chosenTrait.equals(HeroAbilityTrait.PRECISE)){
				avaliableAbilities.remove(HeroAbilityTrait.WILD);
			}
			else if(chosenTrait.equals(HeroAbilityTrait.WILD)){
				avaliableAbilities.remove(HeroAbilityTrait.PRECISE);
			}
			else if(chosenTrait.equals(HeroAbilityTrait.BLACKSMITH)){
				avaliableAbilities.remove(HeroAbilityTrait.FORGE_DISASTER);
			}
			else if(chosenTrait.equals(HeroAbilityTrait.FORGE_DISASTER)){
				avaliableAbilities.remove(HeroAbilityTrait.BLACKSMITH);
			}
			else if(chosenTrait.equals(HeroAbilityTrait.ALCHEMIST)){
				avaliableAbilities.remove(HeroAbilityTrait.HERB_WASTER);
			}
			else if(chosenTrait.equals(HeroAbilityTrait.HERB_WASTER)){
				avaliableAbilities.remove(HeroAbilityTrait.ALCHEMIST);
			}
		}
	}
	
	protected void addStartingItems() {
		Random random = new Random();
		int chance = random.nextInt(10);
		ItemQuality quality = ItemQuality.POOR;
		
		if(chance <= 3) {
			chance = random.nextInt(10);
			if(chance == 0) {
				quality = ItemQuality.GREAT;
			}
			else if(chance <= 3) {
				quality = ItemQuality.AVERAGE;
			}
			
			inventory.addPotion(new HealthPotion(quality));
		}
		
		if(personalityTrait == HeroPersonalityTrait.BRAVE) {
			chance = random.nextInt(10);
			if(chance == 0) {
				quality = ItemQuality.GREAT;
			}
			else if(chance <= 3) {
				quality = ItemQuality.AVERAGE;
			}
			
			inventory.addPotion(new HealthPotion(quality));
		}
		
		chance = random.nextInt(10);
		if(chance == 0) {
			chance = random.nextInt(10);
			quality = ItemQuality.POOR;
			if(chance == 0) {
				quality = ItemQuality.GREAT;
			}
			else if(chance <= 3) {
				quality = ItemQuality.AVERAGE;
			}
			
			inventory.addPotion(new FleeingPotion(quality));
		}
		
		if(personalityTrait == HeroPersonalityTrait.COWARD) {
			chance = random.nextInt(10);
			if(chance == 0) {
				chance = random.nextInt(10);
				quality = ItemQuality.POOR;
				if(chance == 0) {
					quality = ItemQuality.GREAT;
				}
				else if(chance <= 3) {
					quality = ItemQuality.AVERAGE;
				}
				
				inventory.addPotion(new FleeingPotion(quality));
			}
		}
	}
	
	@Override
	public void update() {
		if(!isAlive()) {
			return;
		}
		
		if(!alreadyCommunicated) {
			regenarateHealth();
			looseHealth();

			if(hasStatusEffect(StatusEffect.FROZEN)) {
				updateStatusEffects();
				return;
			}
			
			updateStatusEffects();
			movedTwice = false;
			
			if(tradeCooldown > 0) {
				tradeCooldown--;
			}
			
			if(callAlliesCooldown > 0) {
				callAlliesCooldown--;
			}
		}
		
		perceiveSurroundings();
		perceiveNearbyAgents();
		updateAgentsInCombatWith();
		updateKnownCollectables();
		perceiveNearbyCollectables();
		
		if(!perceivedAgents.isEmpty() && alreadyCommunicated == false) {
			alreadyCommunicated = true;
			addBehaviour(new IntentSenderHeroBehaviour(this, perceivedAgents));
			return;
		}
		
		alreadyCommunicated = false;
		
		if(behaviour == BehaviourType.COMBAT && combatTargetAgent != null) {
				addCombatBehaviour();
				return;
		}
		
		if(behaviour == BehaviourType.FLEEING && isTargetedByOtherAgents()){
			addBehaviour(new FleeingHeroBehaviour(this));
			return;
		}
		
		if(behaviour == BehaviourType.TRADING && isTrading == false) {
			this.addBehaviour(new TradeInitiatorBehaviour(this, tradeAgent));
			isTrading = true;
			return;
		}
		else if(isTrading) {
			return;
		}
		
		if(behaviour == BehaviourType.TRADING_IDLE) {
			isTrading = true;
			return;
		}
		
		if(canBuildStructure()) {
			this.setBehaviour(BehaviourType.STRUCTURE_BUILDING);
			this.addBehaviour(new StructureBuildingHeroBehaviour(this, inventory));
			return;
		}
		
		if(inventory.canCraftSomethingUsefull()) {
			this.setBehaviour(BehaviourType.CRAFTING);
			addBehaviour(new CraftingBehaviour(inventory));
			return;
		}
		
		if(!tilesWithCollectable.isEmpty()) {
			
			if(targetTileWithCollectable != null
					&& (targetTileWithCollectable.getCollectable() == null || !targetTileWithCollectable.getCollectable().canBeCollected())
					&& (targetTileWithCollectable.getAgent() != null && targetTileWithCollectable.getAgent() != this)){
				
				Random random = new Random();
				int chance = random.nextInt(20);
				if(chance == 0) {
					lowerRelationshipWithAgent(targetTileWithCollectable.getAgent().getLocalName(), targetTileWithCollectable.getAgent().getDisplayName());
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
			addBehaviour(new GatherCollectibleBehaviour(this));
			break;
		}
		}
	}

	protected void perceiveNearbyCollectables() {
		for (Tile tile : perceivedTiles) {
			if(tile.getCollectable() != null && tile.getCollectable().canBeCollected()) {
				calculatePathToTile(tile, currentTile);
				if(pathToTile.size() < MAX_DISTANCE_TO_COLLECTABLE && (!pathToTile.isEmpty() || tile.getAgent() == this)) {
					tilesWithCollectable.put(tile, pathToTile.size());
					addToLog("I see " + tile.getCollectable().getType());
				}
			}
		}
	}
	
	protected void updateKnownCollectables() {
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
	
	protected void selectClosestCollectables() {
		float closestDistance = Float.MAX_VALUE;
		
		for (Tile tile : tilesWithCollectable.keySet()) {
			float distanceToMaterial = tilesWithCollectable.get(tile) * collectablePriorities.get(tile.getCollectable().getType());
			if(tile.getCollectable().canBeCollected() &&  closestDistance > distanceToMaterial) {
				closestDistance = distanceToMaterial;
				targetTileWithCollectable = tile;
			}
		}
	}
	
	public void collectCollectable() {
		CollectableLoot loot = currentTile.collectCollectable();
		
		if(personalityTrait == HeroPersonalityTrait.GREEDY) {
			int gold = Math.round(loot.getGold() * 1.2f);
			
			HashMap<MaterialType, Integer> materials = new HashMap<>();
			
			if(loot.getMaterials() != null) {
				for (MaterialType material : loot.getMaterials().keySet()) {
					materials.put(material, Math.round(loot.getMaterials().get(material)*1.2f));
				}
			}
			
			loot = new CollectableLoot(gold, materials, loot.getEquipment(), loot.getPotions());
		}
		
		collectLoot(loot);
	}
	
	public void collectLoot(CollectableLoot loot) {
		
		if(loot == null || !isAlive()) {
			return;
		}
		
		if(loot.getGold() > 0) {
			inventory.addGold(loot.getGold());
			addToLog("I obtained " + loot.getGold() + " gold");
		}
		
		HashMap<MaterialType, Integer> collectedMaterials = loot.getMaterials();
		if(collectedMaterials != null) {
			for (MaterialType materialType : collectedMaterials.keySet()) {
					int gatheredAmount = collectedMaterials.get(materialType);
					if(gatheredAmount > 0) {
						inventory.addMaterial(materialType, gatheredAmount);
						//System.out.println(materialType + " - " + inventory.getMaterialTotal(materialType));
						addToLog("I obtained " + gatheredAmount + " " + materialType.toString());
					}
			}
		}
		
		ArrayList<Equipment> equipmentArray = loot.getEquipment();
		if(equipmentArray != null) {
			for (Equipment equipment : equipmentArray) {
				inventory.addEquipment(equipmentArray);
			}
			equipBestEquipment();
		}
		
		ArrayList<BasePotion> potions = loot.getPotions();
		if(potions != null) {
			inventory.addPotion(potions);
		}
	}
	
	@Override
	public CollectableLoot extractOwnLoot() {
		if(hasAbilityTrait(HeroAbilityTrait.POST_DEATH_BITTERNESS)) {
			inventory.extractAll();
			return null;
		}
		
		return inventory.extractAll();
	}
	
	protected void equipBestEquipment() {
		inventory.equipBestEquipment();
		maxHealth = baseMaxHealth + inventory.getHealthBonusFromEquipment();
		currentHealth = Math.min(currentHealth, maxHealth);
	}
	
	public void drinkBestPotion(PotionType type) {
		inventory.drinkBestPotion(type);
	}
	
	public boolean hasPotion(PotionType type) {
		return inventory.hasPotion(type);
	}
	
	public Tile getTileWithMaterial() {
		return targetTileWithCollectable;
	}

	public void setTileWithMaterial(Tile tileWithMaterial) {
		this.targetTileWithCollectable = tileWithMaterial;
	}
	
	public int getGold() {
		return inventory.getGold();
	}
	
	@Override
	public int getAttack() {
		return attack + inventory.getAttackBonusFromEquipment();
	}
	
	@Override
	public void onDealtDamage(BaseAgent target) {
		if(target.isEntityAgent()) {
			if(hasAbilityTrait(HeroAbilityTrait.POISON_DABLER)) {
				Random random = new Random();
				int chance = random.nextInt(10);
				if(chance == 0) {
					((BaseEntityAgent)target).addStatusEffect(StatusEffect.POISON, 3);
				}
			}
			if(hasAbilityTrait(HeroAbilityTrait.FROST_INFUSER)) {
				Random random = new Random();
				int chance = random.nextInt(20);
				if(chance == 0) {
					((BaseEntityAgent)target).addStatusEffect(StatusEffect.FROZEN, 1);
				}
			}
		}
	}
	
	@Override
	protected boolean didDefyDeath() {
		if(hasAbilityTrait(HeroAbilityTrait.LAST_STAND)) {
			currentHealth = getMaxHealth()/10;
			abilityTraits.remove(HeroAbilityTrait.LAST_STAND);
			return true;
		}
		return super.didDefyDeath();
	}
	
	@Override
	public void addStatusEffect(StatusEffect effect, int turnNum) {
		
		if(effect == StatusEffect.POISON && hasAbilityTrait(HeroAbilityTrait.ANTIVENOM)) {
			return;
		}
		
		if(effect == StatusEffect.FROZEN && hasAbilityTrait(HeroAbilityTrait.BURNING_BLOOD)) {
			return;
		}
		
		super.addStatusEffect(effect, turnNum);
	}
	
	@Override
	public void doDelete() {
		inventory.deleteAllItems();
		super.doDelete();
	}
	
	public String getTradeDesireString() {
		Random random = new Random();
		int chance = random.nextInt(6);
		EquipmentType type = null;
		
		switch (chance) {
		case 0: {
			type = EquipmentType.BOOTS;
			break;
		}
		case 1: {
			type = EquipmentType.CHESTPLATE;
			break;
		}
		case 2: {
			type = EquipmentType.LEGGINGS;
			break;
		}
		case 3: {
			type = EquipmentType.HELMET;
			break;
		}
		case 4: {
			type = EquipmentType.SHIELD;
			break;
		}
		case 5: {
			type = EquipmentType.SWORD;
			break;
		}
		}
		
		Equipment equipment = inventory.getEquipedEquipment(type);
		
		int bonusStat = 0;
		if(equipment != null) {
			bonusStat = equipment.getBonus();
		}
		
		return type + ":" + bonusStat;
	}
	
	protected void addTraitBonusesToStats() {
		
		if(personalityTrait == HeroPersonalityTrait.BLOODTHIRSTY) {
			attack = Math.round(attack*1.15f);
		}
		else if(personalityTrait == HeroPersonalityTrait.BRAVE) {
			attack = Math.round(attack*1.1f);
			baseMaxHealth = Math.round(baseMaxHealth*1.1f);
			maxHealth = baseMaxHealth;
			currentHealth = baseMaxHealth;
		}
		else if(personalityTrait == HeroPersonalityTrait.COWARD) {
			attack = Math.round(attack*0.85f);
		}
		
		if(abilityTraits.contains(HeroAbilityTrait.TANK)) {
			baseMaxHealth = Math.round(baseMaxHealth*1.3f);
			maxHealth = baseMaxHealth;
			currentHealth = baseMaxHealth;
			evasionChance = Math.round(evasionChance*0.5f);
		}
		else if(abilityTraits.contains(HeroAbilityTrait.NIMBLE)) {
			baseMaxHealth = Math.round(baseMaxHealth*0.7f);
			maxHealth = baseMaxHealth;
			currentHealth = baseMaxHealth;
			evasionChance = Math.round(evasionChance*2.0f);
		}
		
		if(abilityTraits.contains(HeroAbilityTrait.WILD)) {
			attack = Math.round(attack*1.3f); 
			critChance = Math.round(critChance*0.5f);
		}
		else if(abilityTraits.contains(HeroAbilityTrait.PRECISE)) {
			attack = Math.round(attack*0.7f); 
			critChance = Math.round(critChance*2.0f);
		}
	}
	
	public Equipment getEquipmentWithBetterStats(EquipmentType type, int bonusStat) {
		return inventory.getEquipmentWithBetterStats(type, bonusStat);
	}

	public Equipment getEquipedEquipment(EquipmentType type) {
		return inventory.getEquipedEquipment(type);
	}

	public void addEquipment(Equipment equipment) {
		inventory.addEquipment(equipment);
		equipBestEquipment();
	}

	public void removeEquipmentByName(String equipmentName) {
		inventory.removeEquipmentByName(equipmentName);
	}
	
	public void addGold(int value) {
		inventory.addGold(value);
	}

	public void removeGold(int value) {
		inventory.removeGold(value);
	}

	public BaseAgent getTradeAgent() {
		return tradeAgent;
	}

	public void setTradeAgent(BaseAgent senderAgent) {
		this.tradeAgent = senderAgent;
	}
	
	public void setIsTrading(boolean isTrading) {
		this.isTrading = isTrading;
	}
	
	public void startTradeCooldown(int cooldown) {
		tradeCooldown = cooldown;
	}
	
	public boolean isTradingEnabled() {
		return tradeCooldown == 0;
	}
	
	public int getMaterialTotal(MaterialType material) {
		return inventory.getMaterialTotal(material);
	}

	public int getTotalGold() {
		return inventory.getTotalGold();
	}

	public void removeMaterial(MaterialType material, int amount) {
		inventory.addMaterial(material, amount);
	}

	public void addMaterial(MaterialType material, int amount) {
		inventory.addMaterial(material, amount);
	}
	
	public HeroType getHeroType() {
		return heroType;
	}
	
	public boolean isHeroType(HeroType heroType) {
		return this.heroType == heroType;
	}

	public HeroPersonalityTrait getPersonalityTrait() {
		return personalityTrait;
	}

	public ArrayList<HeroAbilityTrait> getAbilityTraits() {
		return abilityTraits;
	}

	public ArrayList<BaseItem> getAllItems() {
		return inventory.getItems();
	}

	@Override
	public String getStringOfTraits() {
		String traits = personalityTrait.toString();
		
		for(HeroAbilityTrait abilityTrait : abilityTraits) {
			traits += ", " + abilityTrait;
		}
		
		return traits;
	}
	
	@Override
	public void gainXP(int xp) {
		if(hasAbilityTrait(HeroAbilityTrait.FAST_LEARNER)) {
			xp *= 2;
		}
		else if(hasAbilityTrait(HeroAbilityTrait.DIMWIT)) {
			xp /= 2;
		}
		super.gainXP(xp);
	}
	
	@Override
	protected boolean canBuildStructure() {
		if(level >= 10 && movableToTiles.size() > 1 && ownedStructures.isEmpty() && getNumberOfNearbyCollectables() >= 2 && canBuildStructureOnNeighboringTile()) {
			return true;
		}
		return false;
	}
	
	protected int getNumberOfNearbyCollectables() {
		int counter = 0;
		
		for (Tile tile : perceivedTiles) {
			if(tile.getCollectable() != null) {
				counter++;
			}
		}
		
		return counter;
	}

	public boolean isMissingPotions() {
		return inventory.isMissingPotions();
	}

	public PotionType getMissingPotionType() {
		return inventory.getMissingPotionType();
	}

	public void addPotion(BasePotion potion) {
		inventory.addPotion(potion);
	}

	public Equipment getRandomUnusedEquipment() {
		return inventory.getRandomUnusedEquipment();
	}

	public boolean desiresPotion(PotionType type) {
		return inventory.desiresPotion(type);
	}
	
	@Override
	public int getBaseMaxHealth() {
		return baseMaxHealth;
	}

	public boolean hasAbilityTrait(HeroAbilityTrait trait) {
		return abilityTraits.contains(trait);
	}

	public HeroInventory getInventory() {
		return inventory;
	}
	
	@Override
	public void loadAgent(TileSaveData save) {
		super.loadAgent(save);
		
		tilesWithCollectable = new HashMap<>();
		
		inventory = save.getHeroInventory();
		inventory.setOwner(this);
	}
}
