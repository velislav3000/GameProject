package agents.items;

import java.util.ArrayList;

import agents.items.equipment.Equipment;
import agents.items.equipment.Equipment.EquipmentType;
import agents.items.potions.BasePotion;
import collectiables.materials.BaseMaterial.MaterialType;
import jade.util.leap.Serializable;
import ontology.GameOntology;

public abstract class BaseInventory implements Serializable {

	protected ArrayList<BaseItem> itemInventory;
	protected ArrayList<Equipment> equipmentInventory;
	protected ArrayList<Equipment> bootsInventory;
	protected ArrayList<Equipment> chestplateInventory;
	protected ArrayList<Equipment> leggingsInventory;
	protected ArrayList<Equipment> helmetInventory;
	protected ArrayList<Equipment> shieldInventory;
	protected ArrayList<Equipment> swordInventory;
	protected ArrayList<Equipment> daggerInventory;
	protected ArrayList<Equipment> bowInventory;
	protected ArrayList<Equipment> hammerInventory;
	protected ArrayList<BasePotion> potionInventory;
	protected int gold;
	
	
	public BaseInventory() {
		itemInventory = new ArrayList<>();
		equipmentInventory = new ArrayList<>();
		bootsInventory = new ArrayList<>();
		chestplateInventory = new ArrayList<>();
		leggingsInventory = new ArrayList<>();
		helmetInventory = new ArrayList<>();
		shieldInventory = new ArrayList<>();
		swordInventory = new ArrayList<>();
		daggerInventory = new ArrayList<>();
		bowInventory = new ArrayList<>();
		hammerInventory = new ArrayList<>();
		
		potionInventory = new ArrayList<>();
		
		gold = 0;
	}
	
	protected abstract void addOwnedRelationshipForItem(BaseItem item);
	
	protected abstract void addOwnedRelationshipForItem(ArrayList<String> itemNames);
	
	protected abstract void logAquiredPotion(BasePotion potion);
	
	protected abstract void logAquiredEquipment(Equipment equipment);
	
	public void addPotion(BasePotion potion) {
		potionInventory.add(potion);
		logAquiredPotion(potion);
		itemInventory.add(potion);
		addOwnedRelationshipForItem(potion);
	}
	
	public void addPotion(ArrayList<BasePotion> potions) {
		ArrayList<String> potionNames = new ArrayList<>();
		
		for (BasePotion potion : potions) {
			potionInventory.add(potion);
			logAquiredPotion(potion);
		
			itemInventory.add(potion);
			potionNames.add(potion.getName());
		}
		
		addOwnedRelationshipForItem(potionNames);
	}
	
	public void addEquipment(Equipment equipment) {
		switch (equipment.getType()) {
		case BOOTS: {
			bootsInventory.add(equipment);
			break;
		}
		case CHESTPLATE: {
			chestplateInventory.add(equipment);
			break;
		}
		case LEGGINGS: {
			leggingsInventory.add(equipment);
			break;
		}
		case HELMET: {
			helmetInventory.add(equipment);
			break;
		}
		case SHIELD: {
			shieldInventory.add(equipment);
			break;
		}
		case SWORD: {
			swordInventory.add(equipment);
			break;
		}
		case DAGGER: {
			daggerInventory.add(equipment);
			break;
		}
		case BOW: {
			bowInventory.add(equipment);
			break;
		}
		case HAMMER: {
			hammerInventory.add(equipment);
			break;
		}
		}
		
		equipmentInventory.add(equipment);
		logAquiredEquipment(equipment);
		itemInventory.add(equipment);
		addOwnedRelationshipForItem(equipment);
	}
	
	public void addEquipment(ArrayList<Equipment> equipmentArray) {
		ArrayList<String> equipmentNames = new ArrayList<>();
		
		for (Equipment equipment : equipmentArray) {
			switch (equipment.getType()) {
			case BOOTS: {
				bootsInventory.add(equipment);
				break;
			}
			case CHESTPLATE: {
				chestplateInventory.add(equipment);
				break;
			}
			case LEGGINGS: {
				leggingsInventory.add(equipment);
				break;
			}
			case HELMET: {
				helmetInventory.add(equipment);
				break;
			}
			case SHIELD: {
				shieldInventory.add(equipment);
				break;
			}
			case SWORD: {
				swordInventory.add(equipment);
				break;
			}
			case DAGGER: {
				daggerInventory.add(equipment);
				break;
			}
			case BOW: {
				bowInventory.add(equipment);
				break;
			}
			case HAMMER: {
				hammerInventory.add(equipment);
				break;
			}
			}
			
			equipmentInventory.add(equipment);
			logAquiredEquipment(equipment);
			
			itemInventory.add(equipment);
			equipmentNames.add(equipment.getName());
		}
		
		addOwnedRelationshipForItem(equipmentNames);
	}
	
	public abstract int getMaterialTotal(MaterialType type);
	
	public abstract void addMaterial(MaterialType type, int amount);
	
	public abstract void removeMaterial(MaterialType type, int amount);
	
	protected abstract int getAllMaterialsValue();

	public int getGold() {
		return gold;
	}
	
	public void addGold(int value) {
		gold += value;
	}
	
	public void removeGold(int value) {
		gold -= value;
	}

	public int getTotalGold() {
		return gold + getAllMaterialsValue();
	}
	
	public void deleteAllItems() {
		if(itemInventory != null) {
			ArrayList<String> itemNames = new ArrayList<>();
			for (BaseItem item : itemInventory) {
				itemNames.add(item.getName());
			}
			GameOntology.removeObjectsFromOntology(itemNames);
		}
	}
	
	protected ArrayList<CraftingRecipe> getCopyOfAllCraftingRecipies() {
		ArrayList<CraftingRecipe> craftingRecipies = new ArrayList<>();
		
		for (CraftingRecipe craftingRecipe : ItemUtils.craftingRecipies) {
			craftingRecipies.add(craftingRecipe);
		}
		
		return craftingRecipies;
	}
	
	public abstract Equipment getEquipmentWithBetterStats(EquipmentType type, int bonusStat);

	public ArrayList<BaseItem> getItems() {
		ArrayList<BaseItem> temp = new ArrayList<>();
		temp.addAll(itemInventory);
		return temp;
	}
}
