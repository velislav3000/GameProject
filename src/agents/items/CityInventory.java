package agents.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import agents.heroAgents.BaseHeroAgent;
import agents.heroAgents.CityAgent;
import agents.heroAgents.BaseHeroAgent.HeroType;
import agents.items.CraftingRecipe.RecipeType;
import agents.items.equipment.Equipment;
import agents.items.equipment.Equipment.EquipmentType;
import agents.items.potions.BasePotion;
import agents.items.potions.BasePotion.PotionType;
import collectiables.CollectableLoot;
import collectiables.materials.BaseMaterial.MaterialType;
import ontology.GameOntology;

public class CityInventory extends BaseInventory{
	
	private transient CityAgent ownerRef;
	private HashMap<MaterialType, Integer> craftingMaterialInventory;
	private HashMap<MaterialType, Integer> improvingMaterialInventory;
	
	public CityInventory(CityAgent owner) {
		super();
		
		ownerRef = owner;
		
		craftingMaterialInventory = new HashMap<>();
		improvingMaterialInventory = new HashMap<>();
		for (int i = 0; i < MaterialType.values().length; i++) {
			craftingMaterialInventory.put(MaterialType.values()[i], 0);
			improvingMaterialInventory.put(MaterialType.values()[i], 0);
		}
	}
	
	@Override
	protected void addOwnedRelationshipForItem(BaseItem item) {
		GameOntology.addOwnedRelationshipBetweenAgentAndItem(ownerRef, item.getName());
	}
	
	@Override
	protected void addOwnedRelationshipForItem(ArrayList<String> itemNames) {
		GameOntology.addOwnedRelationshipBetweenAgentAndItem(ownerRef, itemNames);
	}
	
	public void addCraftedEquipment(Equipment equipment) {
		if(ownerRef.getOwner() != null) {
			BaseHeroAgent heroAgent = (BaseHeroAgent) ownerRef.getOwner();
			
			if(!((equipment.getType() == EquipmentType.SHIELD || equipment.getType() == EquipmentType.SWORD
					|| equipment.getType() == EquipmentType.HAMMER) && heroAgent.getHeroType() != HeroType.WARRIOR
					|| equipment.getType() == EquipmentType.BOW && heroAgent.getHeroType() != HeroType.ARCHER
					|| equipment.getType() == EquipmentType.DAGGER && heroAgent.getHeroType() != HeroType.ROGUE)) {
				Equipment equipedEquipment = heroAgent.getEquipedEquipment(equipment.getType());
				
				if(equipedEquipment == null || equipment.getBonus() > equipedEquipment.getBonus()) {
					ArrayList<Equipment> equipmentToSend = new ArrayList<>();
					equipmentToSend.add(equipment);
					
					CollectableLoot loot = new CollectableLoot(0, new HashMap<>(), equipmentToSend, null);
					
					ownerRef.getOwner().collectLoot(loot);
					
					return;
				}
			}
		}
		addEquipment(equipment);
	}
	
	public void addCraftedPotion(BasePotion potion) {
		if(ownerRef.getOwner() != null) {
			
			ArrayList<BasePotion> potionsToSend = new ArrayList<>();
			potionsToSend.add(potion);
			
			CollectableLoot loot = new CollectableLoot(0, new HashMap<>(), null, potionsToSend);
			
			ownerRef.getOwner().collectLoot(loot);
		}
		else {
			addPotion(potion);
		}
	}
	
	public void removeEquipment(Equipment equipment) {
		itemInventory.remove(equipment);
		bootsInventory.remove(equipment);
		chestplateInventory.remove(equipment);
		leggingsInventory.remove(equipment);
		helmetInventory.remove(equipment);
		shieldInventory.remove(equipment);
		swordInventory.remove(equipment);
		daggerInventory.remove(equipment);
		bowInventory.remove(equipment);
		hammerInventory.remove(equipment);
		
		GameOntology.removeOwnedRelationshipBetweenAgentAndItem(ownerRef, equipment.getName());
	}
	
	public int getImrpovingMaterialTotal(MaterialType type) {
		return improvingMaterialInventory.get(type);
	}
	
	@Override
	public int getMaterialTotal(MaterialType type) {
		return improvingMaterialInventory.get(type) + craftingMaterialInventory.get(type);
	}
	
	@Override
	public void addMaterial(MaterialType type, int amount) {
		int amountForCrafting = amount/2;
		craftingMaterialInventory.put(type, amountForCrafting + craftingMaterialInventory.get(type));
		improvingMaterialInventory.put(type, amount - amountForCrafting + improvingMaterialInventory.get(type));
	}
	
	@Override
	public void removeMaterial(MaterialType type, int amount) {
		int totalAmount = improvingMaterialInventory.get(type) - amount;
		improvingMaterialInventory.put(type, totalAmount);
	}
	
	public boolean canCraftSomething() {
		ArrayList<CraftingRecipe> craftingRecipies = getCopyOfAllCraftingRecipies();
		
		for(int i = craftingRecipies.size()-1; i >= 0; i--) {
			
			CraftingRecipe craftingRecipe = craftingRecipies.get(i);
			
			if(!craftingRecipe.canUse(this)) {
				craftingRecipies.remove(craftingRecipe);
				continue;
			}
		}
		
		return !craftingRecipies.isEmpty();
	}
	
	public ArrayList<CraftingRecipe> getUsableCraftingRecipies() {
		ArrayList<CraftingRecipe> craftingRecipies = getCopyOfAllCraftingRecipies();
		
		for(int i = craftingRecipies.size()-1; i >= 0; i--) {
			
			CraftingRecipe craftingRecipe = craftingRecipies.get(i);
			
			if(!craftingRecipe.canUse(this)) {
				craftingRecipies.remove(craftingRecipe);
				continue;
			}
		}
		
		return craftingRecipies;
	}
	
	@Override
	protected int getAllMaterialsValue() {
		int totalValue = 0;
		
		for (int i = 0; i < MaterialType.values().length; i++) {
			MaterialType material = MaterialType.values()[i];
			totalValue += improvingMaterialInventory.get(material) * ItemUtils.getBaseMaterialPrice(material);
		}
		
		return totalValue;
	}
	
	@Override
	public Equipment getEquipmentWithBetterStats(EquipmentType type, int bonusStat) {
		ArrayList<Equipment> equipmentInvetory = null;
		
		switch (type) {
		case BOOTS: {
			equipmentInvetory = bootsInventory;
			break;
		}
		case CHESTPLATE: {
			equipmentInvetory = chestplateInventory;
			break;
		}
		case HELMET: {
			equipmentInvetory = helmetInventory;
			break;
		}
		case LEGGINGS: {
			equipmentInvetory = leggingsInventory;
			break;
		}
		case SHIELD: {
			equipmentInvetory = shieldInventory;
			break;
		}
		case BOW: {
			equipmentInvetory = bowInventory;
			break;
		}
		case DAGGER: {
			equipmentInvetory = daggerInventory;
			break;
		}
		case HAMMER: {
			equipmentInvetory = hammerInventory;
			break;
		}
		case SWORD: {
			equipmentInvetory = swordInventory;
			break;
		}
		}
		
		ArrayList<Equipment> betterEquipment = new ArrayList<>();
		for (Equipment equipment : equipmentInvetory) {
			if(equipment.getBonus() > bonusStat) {
				betterEquipment.add(equipment);
			}
		}

		if(betterEquipment.isEmpty()) {
			return null;
		}
		
		Random random = new Random();
		int randomIndex = random.nextInt(betterEquipment.size());
		
		return betterEquipment.get(randomIndex);
	}

	public BasePotion getPotion(PotionType searchedPotionType) {
		for(BasePotion potion : potionInventory) {
			if(potion.getType() == searchedPotionType) {
				return potion;
			}
		}
		return null;
	}

	public void removePotion(BasePotion potion) {
		potionInventory.remove(potion);
		itemInventory.remove(potion);
	}

	public void getPillaged(BaseHeroAgent pillager) {
		int pillagedGold = gold/2;
		gold -= pillagedGold;
		
		ArrayList<Equipment> pillagedEquipment = new ArrayList<>();
		ArrayList<String> pillagedEquipmentNames = new ArrayList<>();
		for (Equipment equipment : equipmentInventory) {
			if(pillager.getEquipedEquipment(equipment.getType()) == null || equipment.getBonus() > pillager.getEquipedEquipment(equipment.getType()).getBonus()) {
				pillagedEquipment.add(equipment);
				pillagedEquipmentNames.add(equipment.getName());
			}
		}
		
		itemInventory.removeAll(pillagedEquipment);
		equipmentInventory.removeAll(pillagedEquipment);
		bootsInventory.removeAll(pillagedEquipment);
		chestplateInventory.removeAll(pillagedEquipment);
		leggingsInventory.removeAll(pillagedEquipment);
		helmetInventory.removeAll(pillagedEquipment);
		shieldInventory.removeAll(pillagedEquipment);
		swordInventory.removeAll(pillagedEquipment);
		daggerInventory.removeAll(pillagedEquipment);
		bowInventory.removeAll(pillagedEquipment);
		hammerInventory.removeAll(pillagedEquipment);
		
		GameOntology.removeOwnedRelationshipBetweenAgentAndItem(ownerRef, pillagedEquipmentNames);
		
		ArrayList<BasePotion> pillagedPotions = new ArrayList<>();
		ArrayList<String> pillagedPotionNames = new ArrayList<>();
		for (BasePotion potion : potionInventory) {
			if(pillager.desiresPotion(potion.getType())) {
				pillagedPotions.add(potion);
				pillagedPotionNames.add(potion.getName());
			}
		}
		
		itemInventory.removeAll(pillagedPotions);
		potionInventory.removeAll(pillagedPotions);
		
		GameOntology.removeOwnedRelationshipBetweenAgentAndItem(ownerRef, pillagedPotionNames);
		
		pillager.collectLoot(new CollectableLoot(pillagedGold, null, pillagedEquipment, pillagedPotions));
	}

	@Override
	protected void logAquiredPotion(BasePotion potion) {
		ownerRef.addToLog("I obtained " + potion.getQuality() + " " + potion.getType().toString());
	}

	@Override
	protected void logAquiredEquipment(Equipment equipment) {
		ownerRef.addToLog("I obtained " + equipment.getQuality() + " " + equipment.getMaterial() + " " + equipment.getType());
	}

	public void setOwner(CityAgent owner) {
		ownerRef = owner;
	}
}
