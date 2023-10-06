package agents.heroAgents;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import javax.imageio.ImageIO;

import agents.BaseStructureAgent;
import agents.behaviours.structure.CombatStructureBehaviour;
import agents.behaviours.structure.CraftingStructureBehaviour;
import agents.behaviours.structure.GatherCollectibleStructureBehaviour;
import agents.behaviours.structure.MessageProcessingCityStructureBehaviour;
import agents.behaviours.structure.MessageProcessingStructureBehaviour;
import agents.behaviours.structure.SpawnUnitBehaviour;
import agents.BaseAgent;
import agents.BaseEntityAgent;
import agents.BaseEntityAgent.BehaviourType;
import agents.BaseEntityAgent.RelationshipType;
import agents.items.BaseItem;
import agents.items.CityInventory;
import agents.items.ItemUtils;
import agents.items.equipment.Equipment;
import agents.items.equipment.Equipment.EquipmentType;
import agents.items.potions.BasePotion;
import agents.items.potions.BasePotion.PotionType;
import collectiables.CollectableLoot;
import collectiables.materials.BaseMaterial.MaterialType;
import ontology.GameOntology;
import saveData.TileSaveData;
import tiles.Tile;
import tiles.Tile.TileType;

public class CityAgent extends BaseStructureAgent{
	
	private transient CityInventory inventory;
	
	private transient ArrayList<Tile> perceivedTiles;
	private transient ArrayList<Tile> oldPerceivedTiles;
	private transient ArrayList<Tile> tilesWithCollectibles;
	
	private final int BASE_LEVEL_UP_COST = 300;
	
	private transient static BufferedImage agentImage;
	
	public CityAgent() {
		super();
		agentImageName = "agent_city.png";
		
		addBehaviour(new MessageProcessingCityStructureBehaviour(this));
	}
	
	@Override
	protected void setup() {
		if(agentWasLoaded) {
			return;
		}
		
		maxHealth = 150;
		currentHealth = maxHealth;
		attack = 15;
		
		inventory = new CityInventory(this);
		
		perceivedTiles = new ArrayList<>();
		oldPerceivedTiles = new ArrayList<>();
		tilesWithCollectibles = new ArrayList<>();
		
		baseAgentSpawningCooldown = 30;
		
		super.setup();
		
		perceiveSurroundings();
		updateCityTeritory();
	}
	
	@Override
	public void update() {
		if(!isAlive()) {
			return;
		}
		
		regenarateHealth();
		
		if(agentSpawningCooldown > 0) {
			agentSpawningCooldown--;
		}
		
		if(callAlliesCooldown > 0) {
			callAlliesCooldown--;
		}
		
		if(canLevelUp()) {
			levelUp();
			return;
		}
		
		perceiveSurroundings();
		updateCityTeritory();
		updateAgentsInCombatWith();
		
		if(!agentsInCombatWith.isEmpty()) {
			setBehaviour(StructureBehaviourType.COMBAT);
			this.addBehaviour(new CombatStructureBehaviour(this));
			return;
		}
		
		if(this.getBehaviour() == StructureBehaviourType.TRADING) {
			return;
		}
		
		if(agentSpawningCooldown == 0) {
			setBehaviour(StructureBehaviourType.SPAWN_UNIT);
			this.addBehaviour(new SpawnUnitBehaviour(this));
			return;
		}
		
		if(inventory.canCraftSomething()) {
			setBehaviour(StructureBehaviourType.CRAFTING);
			this.addBehaviour(new CraftingStructureBehaviour(inventory));
			return;
		}
			
		perceiveNearbyCollectables();
		
		if(!tilesWithCollectibles.isEmpty()) {
			setBehaviour(StructureBehaviourType.GATHER_MATERIALS);
			addBehaviour(new GatherCollectibleStructureBehaviour(this, tilesWithCollectibles));
			return;
		}
		
		setBehaviour(StructureBehaviourType.IDLE);
	}
	
	public void perceiveSurroundings() {
		oldPerceivedTiles.clear();
		oldPerceivedTiles.addAll(perceivedTiles);
		
		perceivedTiles = currentTile.getNeighbours();
		
		ArrayList<Tile> blockedFromPerceptionTiles = new ArrayList<>();
		ArrayList<Tile> secondTierTiles = new ArrayList<>();
		
		for (Tile tile : perceivedTiles) {
			if(tile.getTileType() == TileType.MOUNTAIN || tile.getTileType() == TileType.FOREST || tile.getTileType() == TileType.MUSHROOM) {
				for (Tile t : tile.getNeighboursOppositeOfTile(currentTile)) {
					if(!blockedFromPerceptionTiles.contains(t)) {
						blockedFromPerceptionTiles.add(t);
					}
				}
			}
		}
		
		for(int i=0; i<currentTile.getNeighbours().size(); i++) {
			ArrayList<Tile> tempTiles = perceivedTiles.get(i).getNeighbours();
			for(Tile t : tempTiles) {
				if(!perceivedTiles.contains(t) && !blockedFromPerceptionTiles.contains(t)) {
					perceivedTiles.add(t);
					secondTierTiles.add(t);
				}
			}
		}
		
		if(currentTile.getTileType() == TileType.GRASS || currentTile.getTileType() == TileType.SAND || currentTile.getTileType() == TileType.SNOW
				|| currentTile.getTileType() == TileType.BURNING_SAND || currentTile.getTileType() == TileType.ICE) {
			blockedFromPerceptionTiles = new ArrayList<>();
			for (Tile tile : secondTierTiles) {
				if(tile.getTileType() == TileType.MOUNTAIN || tile.getTileType() == TileType.FOREST || tile.getTileType() == TileType.MUSHROOM) {
					for (Tile t : tile.getNeighboursOppositeOfTile(currentTile)) {
						if(!blockedFromPerceptionTiles.contains(t)) {
							blockedFromPerceptionTiles.add(t);
						}
					}
				}
			}
			
			for(int i=0; i< secondTierTiles.size(); i++) {
				ArrayList<Tile> tempTiles = secondTierTiles.get(i).getNeighbours();
				for(Tile t : tempTiles) {
					if(!secondTierTiles.contains(t) && !perceivedTiles.contains(t) && !blockedFromPerceptionTiles.contains(t)) {
						perceivedTiles.add(t);
					}
				}
			}
		}
	}
	
	private void updateCityTeritory() {
		for(Tile tile : perceivedTiles) {
			if(tile.getOwningCity() == null) {
				tile.setOwningCity(this);
			}
		}
		
		for(Tile tile : oldPerceivedTiles) {
			if(!perceivedTiles.contains(tile) && this.equals(tile.getOwningCity())) {
				tile.setOwningCity(null);
			}
		}
	}

	public void perceiveNearbyCollectables() {
		tilesWithCollectibles.clear();
		for (Tile tile : perceivedTiles) {
			if(this.equals(tile.getOwningCity()) && tile.getCollectable() != null && tile.getCollectable().canBeCollected()) {
				tilesWithCollectibles.add(tile);
				addToLog("I see " + tile.getCollectable().getType());
			}
		}
	}
	
	public void collectLoot(CollectableLoot loot) {
		
		if(loot == null || !isAlive()) {
			return;
		}
		
		inventory.addGold(loot.getGold());
		addToLog("I obtained " + loot.getGold() + " gold");
		
		HashMap<MaterialType, Integer> collectedMaterials = loot.getMaterials();
		if(collectedMaterials != null) {
			for (MaterialType materialType : collectedMaterials.keySet()) {
					int gatheredAmount = collectedMaterials.get(materialType);
					if(gatheredAmount > 0) {
						inventory.addMaterial(materialType, gatheredAmount);
						addToLog("I obtained " + gatheredAmount + " " + materialType.toString());
					}
			}
		}
		
		ArrayList<Equipment> equipments = loot.getEquipment();
		if(equipments != null) {
			inventory.addEquipment(equipments);
		}
		
		ArrayList<BasePotion> potions = loot.getPotions();
		if(potions != null) {
			inventory.addPotion(potions);
		}
	}
	
	@Override
	protected boolean canLevelUp() {
		if(level == 10) {
			return false;
		}
		
		if(inventory.getTotalGold() >= level * BASE_LEVEL_UP_COST) {
			return true;
		}
		return false;
	}

	@Override
	protected void levelUp() {
		
		if(inventory.getGold() >= level * BASE_LEVEL_UP_COST) {
			inventory.removeGold(level * BASE_LEVEL_UP_COST);
		}
		else {
			removeMaterialsNeededForLevelUp(level * BASE_LEVEL_UP_COST - inventory.getGold());
			inventory.removeGold(inventory.getGold());
		}
		
		int oldLevel = level;
		int oldHealth = maxHealth;
		int oldAttack = attack;
		
		level += 1;
		maxHealth += 30;
		currentHealth += 30;
		attack += 5;

		addToLog("I leveled up to level " + level);
		
		GameOntology.updateStructureAgentStats(this, oldLevel, oldHealth, oldAttack, owner);
	}
	
	private void removeMaterialsNeededForLevelUp(int neededValue) {
		
		for (MaterialType material : MaterialType.values()) {
			int materialAmount = inventory.getImrpovingMaterialTotal(material);
			int totalMaterialValue = materialAmount * ItemUtils.getBaseMaterialPrice(material);
			
			if(totalMaterialValue >= neededValue) {
				int neededMaterialAmount = Math.round(1.0F*neededValue / ItemUtils.getBaseMaterialPrice(material));
				inventory.removeMaterial(material, neededMaterialAmount);
				return;
			}
			else {
				inventory.removeMaterial(material, materialAmount);
				neededValue -= totalMaterialValue;
			}
		}
	}
	
	@Override
	public void takeDamage(int damage, BaseAgent attacker) {
		if(!isAlive()) {
			return;	
		}
		
		Random random = new Random();
		int chance = random.nextInt(100)+1;
		if(chance <= attacker.getCritChance()) {
			currentHealth -= damage*2;
			//System.out.println("CRITICAL HIT! " + this.getLocalName() + " takes " + damage*2 + " damage");
			addToLog("I took " + damage*2 + " damage from " + attacker.getDisplayName());
			attacker.addToLog("I attacked " + this.getLocalName() + " for " + damage*2 + "damage");
		}
		else {
			currentHealth -= damage;
			//System.out.println(this.getLocalName() + " takes " + damage + " damage");
			addToLog("I took " + damage + " damage from " + attacker.getDisplayName());
			attacker.addToLog("I attacked " + this.getLocalName() + " for " + damage + "damage");
		}
		
		if(currentHealth<0) {
			//System.out.println(this.getLocalName() + " dies");
			
			if(owner != null) {
				owner.removeOwnedStructure(this);
			}
			
			if(attacker.isAlive()) {
				attacker.gainXP(getDroppedXP());
				if(attacker.isHeroAgent()) {
					attacker.addToLog("I captured " + this.getDisplayName());
					this.addToLog("I was captured by " + attacker.getDisplayName());
					BaseEntityAgent oldOwner = owner;
					owner = (BaseHeroAgent) attacker;
					owner.addOwnedStructure(this);
					
					inventory.getPillaged((BaseHeroAgent) attacker); 
					
					currentHealth += maxHealth * 0.2f;
					
					agentsInCombatWith.remove(attacker);
					attacker.getAgentsInCombatWith().remove(this);
					
					GameOntology.updateStructureAgentStats(this, level, maxHealth, attack, oldOwner);
					return;
				}
				else {
					isDead = true;
					attacker.addToLog("I killed " + this.getDisplayName());
					addToLog("I died");
					currentTile.deleteAgent();
				}
			}
			else {
				isDead = true;
				addToLog("I died");
				currentTile.deleteAgent();
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
		return agentImage;
	}

	public Equipment getEquipmentWithBetterStats(EquipmentType type, int bonusStat) {
		return inventory.getEquipmentWithBetterStats(type, bonusStat);
	}

	public void addGoldFromTrade(int gold) {
		if(owner != null) {
			int goldToKeep = gold/2;
			inventory.addGold(goldToKeep);
			((BaseHeroAgent) getOwner()).addGold(gold - goldToKeep);
		}
		else {
			inventory.addGold(gold);
		}
	}

	public void addMaterialFromTrade(MaterialType material, int amount) {
		if(owner != null) {
			int amountToKeep = amount/2;
			inventory.addMaterial(material, amountToKeep);
			((BaseHeroAgent) owner).addMaterial(material, amount - amountToKeep);
		}
		else {
			inventory.addMaterial(material, amount);
		}
		
	}

	public void removeEquipment(Equipment equipment) {
		inventory.removeEquipment(equipment);
	}

	@Override
	public String getSpawnedUnitClassName() {
		Random random = new Random();
		int chance = random.nextInt(3);
		
		switch (chance) {
		case 0: {
			return "Warrior";
		}
		case 1: {
			return "Rogue";
		}
		case 2: {
			return "Archer";
		}
		}
		
		return "";
	}

	public int getGold() {
		return inventory.getGold();
	}

	public int getMaterialTotal(MaterialType material) {
		return inventory.getMaterialTotal(material);
	}

	public ArrayList<BaseItem> getAllItems() {
		return inventory.getItems();
	}

	public BasePotion getPotion(PotionType searchedPotionType) {
		return inventory.getPotion(searchedPotionType);
	}

	public void removePotion(BasePotion potion) {
		inventory.removePotion(potion);
	}

	public void removeGold(int value) {
		inventory.removeGold(value);
	}

	public void addEquipment(Equipment equipment) {
		inventory.addEquipment(equipment);
	}
	
	@Override
	public RelationshipType getRelationshipWithAgent(BaseAgent agent) {
		if(owner != null) {
			if(agent.equals(owner)) {
				return RelationshipType.ALLIES;
			}
			
			RelationshipType relation = GameOntology.getRelationshipWithAgent(owner.getLocalName(), agent.getLocalName());
			if(relation != null) {
				return relation;
			}
			else {
				if(agent.isHeroAgent()) {
					return RelationshipType.NEUTRAL;
				}
				return RelationshipType.ENEMIES;
			}
		}
		else {
			if(agent.isHeroAgent()) {
				return RelationshipType.NEUTRAL;
			}
			return RelationshipType.ENEMIES;
		}
	}
	
	@Override
	public void doDelete() {
		ArrayList<Tile> tiles = new ArrayList<>();
		tiles.addAll(perceivedTiles);
		tiles.addAll(oldPerceivedTiles);
		
		for (Tile tile : tiles) {
			if(this.equals(tile.getOwningCity())) {
				tile.setOwningCity(null);
			}
		}
		
		inventory.deleteAllItems();
		super.doDelete();
	}

	public CityInventory getInventory() {
		return inventory;
	}
	
	@Override
	public void loadAgent(TileSaveData save) {
		super.loadAgent(save);
		
		inventory = save.getCityInventory();
		inventory.setOwner(this);
		
		perceivedTiles = new ArrayList<>();
		oldPerceivedTiles = new ArrayList<>();
		tilesWithCollectibles = new ArrayList<>();

		agentImageName = "agent_city.png";
	}
}
