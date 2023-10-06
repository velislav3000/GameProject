package agents.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import javax.security.auth.login.FailedLoginException;

import agents.heroAgents.BaseHeroAgent;
import agents.heroAgents.BaseHeroAgent.HeroAbilityTrait;
import agents.heroAgents.BaseHeroAgent.HeroPersonalityTrait;
import agents.heroAgents.BaseHeroAgent.HeroType;
import agents.items.CraftingRecipe.RecipeType;
import agents.items.equipment.Equipment;
import agents.items.equipment.Equipment.EquipmentType;
import agents.items.potions.BasePotion;
import agents.items.potions.BasePotion.PotionType;
import collectiables.CollectableLoot;
import collectiables.materials.BaseMaterial.MaterialType;
import ontology.GameOntology;

public class HeroInventory extends BaseInventory{

	private transient BaseHeroAgent ownerRef;
	private HashMap<MaterialType, Integer> materialInventory;
	
	private HashMap<PotionType, Integer> desiredNumberOfPotions;
	
	int attackBonusFromEquipment;
	int healthBonusFromEquipment;
	
	private Equipment equipedBoots = null;
	private Equipment equipedChestplate = null;
	private Equipment equipedLeggings = null;
	private Equipment equipedHelmet = null;
	private Equipment equipedShield = null;
	private Equipment equipedWeapon = null;
	private Equipment equipedSecondWeapon = null;
	
	public HeroInventory(BaseHeroAgent owner) {
		super();
		
		ownerRef = owner;
		
		materialInventory = new HashMap<>();
		for (int i = 0; i < MaterialType.values().length; i++) {
			materialInventory.put(MaterialType.values()[i], 0);
		}
		
		Random random = new Random();
		desiredNumberOfPotions = new HashMap<>();
		
		int desiredPotionAmount = random.nextInt(4)+1;
		if(owner.getPersonalityTrait() == HeroPersonalityTrait.BRAVE) {
			desiredPotionAmount += 2;
		}
		desiredNumberOfPotions.put(PotionType.HEALTH, desiredPotionAmount);
		
		desiredPotionAmount = random.nextInt(2)+1;
		if(owner.getPersonalityTrait() == HeroPersonalityTrait.COWARD) {
			desiredPotionAmount += 2;
		}
		desiredNumberOfPotions.put(PotionType.FLEEING, desiredPotionAmount);
		
		desiredPotionAmount = random.nextInt(3)+1;
		if(owner.getPersonalityTrait() == HeroPersonalityTrait.BRAVE) {
			desiredPotionAmount += 1;
		}
		desiredNumberOfPotions.put(PotionType.REGENARATION, desiredPotionAmount);
		
		desiredPotionAmount = random.nextInt(2)+1;
		if(owner.getPersonalityTrait() == HeroPersonalityTrait.BRAVE) {
			desiredPotionAmount += 1;
		}
		desiredNumberOfPotions.put(PotionType.SUPERHEALTH, desiredPotionAmount);
	}
	
	@Override
	protected void addOwnedRelationshipForItem(BaseItem item) {
		GameOntology.addOwnedRelationshipBetweenAgentAndItem(ownerRef, item.getName());
	}
	
	@Override
	protected void addOwnedRelationshipForItem(ArrayList<String> itemNames) {
		GameOntology.addOwnedRelationshipBetweenAgentAndItem(ownerRef, itemNames);
	}
	
	public void equipBestEquipment() {
		HashMap<String, String> unequipedEquipmentNamesAndTypes = new HashMap<>();
		
		if(equipedBoots != null) {
			unequipedEquipmentNamesAndTypes.put(equipedBoots.getName(), "Boots");
			equipedBoots = null;
		}
		
		if(equipedChestplate != null) {
			unequipedEquipmentNamesAndTypes.put(equipedChestplate.getName(), "Chestplate");
			equipedChestplate = null;
		}
		
		if(equipedLeggings != null) {
			unequipedEquipmentNamesAndTypes.put(equipedLeggings.getName(), "Leggings");
			equipedLeggings = null;
		}
		
		if(equipedHelmet != null) {
			unequipedEquipmentNamesAndTypes.put(equipedHelmet.getName(), "Helmet");
			equipedHelmet = null;
		}
		
		if(equipedShield != null) {
			unequipedEquipmentNamesAndTypes.put(equipedShield.getName(), "Shield");
			equipedShield = null;
		}
		
		if(equipedWeapon != null) {
			unequipedEquipmentNamesAndTypes.put(equipedWeapon.getName(), "Weapon");
			equipedWeapon = null;
		}
		
		if(equipedSecondWeapon != null) {
			unequipedEquipmentNamesAndTypes.put(equipedSecondWeapon.getName(), "SecondWeapon");
			equipedSecondWeapon = null;
		}
		
		if(!unequipedEquipmentNamesAndTypes.isEmpty()) {
			GameOntology.removeEquipedRelationshipBetweenAgentAndEquipment(ownerRef.getLocalName(), unequipedEquipmentNamesAndTypes);
		}
		
		healthBonusFromEquipment = 0;
		attackBonusFromEquipment = 0;
		
		for (Equipment equipment : bootsInventory) {
			if(equipedBoots == null) {
				equipedBoots = equipment;
			}
			else if(equipedBoots.getHealthBonus() < equipment.getHealthBonus()) {
				equipedBoots = equipment;
			}
		}
		
		for (Equipment equipment : chestplateInventory) {
			if(equipedChestplate == null) {
				equipedChestplate = equipment;
			}
			else if(equipedChestplate.getHealthBonus() < equipment.getHealthBonus()) {
				equipedChestplate = equipment;
			}
		}
		
		for (Equipment equipment : leggingsInventory) {
			if(equipedLeggings == null) {
				equipedLeggings = equipment;
			}
			else if(equipedLeggings.getHealthBonus() < equipment.getHealthBonus()) {
				equipedLeggings = equipment;
			}
		}
		
		for (Equipment equipment : helmetInventory) {
			if(equipedHelmet == null) {
				equipedHelmet = equipment;
			}
			else if(equipedHelmet.getHealthBonus() < equipment.getHealthBonus()) {
				equipedHelmet = equipment;
			}
		}
		
		
		Equipment bestShield = null;
		Equipment bestSword = null;
		Equipment bestHammer = null;
		if(ownerRef.isHeroType(HeroType.WARRIOR)) {
			for (Equipment equipment : shieldInventory) {
				if(bestShield == null) {
					bestShield = equipment;
				}
				else if(bestShield.getHealthBonus() < equipment.getHealthBonus()) {
					bestShield = equipment;
				}
			}
			
			for (Equipment equipment : swordInventory) {
				if(bestSword == null) {
					bestSword = equipment;
				}
				else if(bestSword.getAttackBonus() < equipment.getAttackBonus()) {
					bestSword = equipment;
				}
			}
			
			for (Equipment equipment : hammerInventory) {
				if(bestHammer == null) {
					bestHammer = equipment;
				}
				else if(bestHammer.getAttackBonus() < equipment.getAttackBonus()) {
					bestHammer = equipment;
				}
			}
		}
		
		Equipment bestDagger = null;
		Equipment secondBestDagger = null;
		if(ownerRef.isHeroType(HeroType.ROGUE)) {
			for (Equipment equipment : daggerInventory) {
				if(bestDagger == null) {
					bestDagger = equipment;
				}
				else if(bestDagger.getAttackBonus() < equipment.getAttackBonus()) {
					secondBestDagger = bestDagger;
					bestDagger = equipment;
				}
			}
		}

		Equipment bestBow = null;
		if(ownerRef.isHeroType(HeroType.ARCHER)) {
			for (Equipment equipment : bowInventory) {
				if(bestBow == null) {
					bestBow = equipment;
				}
				else if(bestBow.getAttackBonus() < equipment.getAttackBonus()) {
					bestBow = equipment;
				}
			}
		}
		
		int shieldBonus = bestShield == null ? 0 : bestShield.getHealthBonus();
		int swordBonus = bestSword == null ? 0 : bestSword.getAttackBonus();
		int hammerBonus = bestHammer == null ? 0 : bestHammer.getAttackBonus();
		
		if(ownerRef.isHeroType(HeroType.WARRIOR)) {
			if(hammerBonus > swordBonus + shieldBonus/3) {
				equipedWeapon = bestHammer;
			}
			else {
				equipedWeapon = bestSword;
				equipedShield = bestShield;
			}
		}
		else if(ownerRef.isHeroType(HeroType.ROGUE)) {
			equipedWeapon = bestDagger;
			equipedSecondWeapon = secondBestDagger;
		}
		else if(ownerRef.isHeroType(HeroType.ARCHER)) {
			equipedWeapon = bestBow;
		}
		
		HashMap<String, String> equipedEquipmentNamesAndTypes = new HashMap<>();
		if(equipedBoots != null) {
			healthBonusFromEquipment += equipedBoots.getHealthBonus();
			equipedEquipmentNamesAndTypes.put(equipedBoots.getName(), "Boots");
		}
		if(equipedChestplate != null) {
			healthBonusFromEquipment += equipedChestplate.getHealthBonus();
			equipedEquipmentNamesAndTypes.put(equipedChestplate.getName(), "Chestplate");
		}
		if(equipedLeggings != null) {
			healthBonusFromEquipment += equipedLeggings.getHealthBonus();
			equipedEquipmentNamesAndTypes.put(equipedLeggings.getName(), "Leggings");
		}
		if(equipedHelmet != null) {
			healthBonusFromEquipment += equipedHelmet.getHealthBonus();
			equipedEquipmentNamesAndTypes.put(equipedHelmet.getName(), "Helmet");
		}
		if(equipedShield != null) {
			healthBonusFromEquipment += equipedShield.getHealthBonus();
			equipedEquipmentNamesAndTypes.put(equipedShield.getName(), "Shield");
		}
		if(equipedWeapon != null) {
			attackBonusFromEquipment += equipedWeapon.getAttackBonus();
			equipedEquipmentNamesAndTypes.put(equipedWeapon.getName(), "Weapon");
		}
		if(equipedSecondWeapon != null) {
			attackBonusFromEquipment += equipedSecondWeapon.getAttackBonus();
			equipedEquipmentNamesAndTypes.put(equipedSecondWeapon.getName(), "SecondWeapon");
		}
		GameOntology.addEquipedRelationshipBetweenAgentAndEquipment(ownerRef.getLocalName(), equipedEquipmentNamesAndTypes);
	}
	
	@Override
	public int getMaterialTotal(MaterialType type) {
		return materialInventory.get(type);
	}
	
	@Override
	public void addMaterial(MaterialType type, int amount) {
		materialInventory.put(type, amount + materialInventory.get(type));
	}
	
	public boolean hasEnoughMaterial(MaterialType type, int amount) {
		int totalAmount = materialInventory.get(type);
		
		return totalAmount >= amount;
	}
	
	@Override
	public void removeMaterial(MaterialType type, int amount) {
		int totalAmount = materialInventory.get(type) - amount;
		materialInventory.put(type, totalAmount);
	}

	public int getAttackBonusFromEquipment() {
		return attackBonusFromEquipment;
	}

	public int getHealthBonusFromEquipment() {
		return healthBonusFromEquipment;
	}
	
	public Equipment getEquipedEquipment(EquipmentType type) {
		switch (type) {
		case BOOTS: {
			return equipedBoots;
		}
		case CHESTPLATE: {
			return equipedChestplate;
		}
		case LEGGINGS: {
			return equipedLeggings;
		}
		case HELMET: {
			return equipedHelmet;
		}
		case SHIELD: {
			return equipedShield;
		}
		default:{
			if(equipedSecondWeapon != null) {
				return equipedSecondWeapon;
			}
			else {
				return equipedWeapon;
			}
		}
		}
	}

	public CollectableLoot extractAll() {
		
		if(materialInventory == null || equipmentInventory == null || potionInventory == null) {
			return null;
		}
		
		CollectableLoot loot = new CollectableLoot(gold, materialInventory, equipmentInventory, potionInventory);
		
		HashMap<String, String> equipmentNameAndType = new HashMap<>();
		ArrayList<String> itemNames = new ArrayList<>();
		
		for (Equipment equipment : equipmentInventory) {
			String equipmentType = "";
			if(equipment.getType() == EquipmentType.BOW || equipment.getType() == EquipmentType.DAGGER || equipment.getType() == EquipmentType.SWORD || equipment.getType() == EquipmentType.HAMMER) {
				equipmentType = "Weapon";
			}
			else {
				equipmentType = equipment.getType().toString();
				equipmentType = equipmentType.substring(0,1) + equipmentType.substring(1).toLowerCase();
			}
			
			if(equipment.equals(getEquipedEquipment(equipment.getType()))) {
				if(equipment.getType() == EquipmentType.DAGGER) {
					equipmentNameAndType.put(equipment.getName(), "SecondWeapon");
				}
				else {
					equipmentNameAndType.put(equipment.getName(), equipmentType);
				}
			}
			else if(equipment.getType() == EquipmentType.DAGGER && equipment.equals(equipedWeapon)) {
				equipmentNameAndType.put(equipment.getName(), equipmentType);
			}
			
			itemNames.add(equipment.getName());
			
		}
		
		for (BasePotion potion : potionInventory) {
			itemNames.add(potion.getName());
		}
		
		GameOntology.removeOwnedRelationshipBetweenAgentAndItem(ownerRef, itemNames);
		GameOntology.removeEquipedRelationshipBetweenAgentAndEquipment(ownerRef.getName(), equipmentNameAndType);
		
		materialInventory = null;
		itemInventory = null;
		equipmentInventory = null;
		bootsInventory = null;
		chestplateInventory = null;
		leggingsInventory = null;
		helmetInventory = null;
		shieldInventory = null;
		swordInventory = null;
		daggerInventory = null;
		bowInventory = null;
		hammerInventory = null;
		potionInventory = null;
		return loot;
	}
	
	public void drinkBestPotion(PotionType type) {
		BasePotion bestPotion = null;
		
		for (BasePotion potion : potionInventory) {
			if(potion.getType() == type) {
				if(bestPotion == null || bestPotion.getQuality().compareTo(potion.getQuality()) > 0) {
					bestPotion = potion;
				}
			}
		}
		
		bestPotion.activateEffect(ownerRef);
		potionInventory.remove(bestPotion);
		itemInventory.remove(bestPotion);
		
        ownerRef.addToLog("I drank " + bestPotion.getQuality() + " " + bestPotion.getType());
		
		GameOntology.removeObjectFromOntology(bestPotion.getName());
	}
	
	public boolean hasPotion(PotionType type) {
		boolean hasPotion = false;
		
		for (BasePotion potion : potionInventory) {
			if(potion.getType() == type) {
				hasPotion = true;
				break;
			}
		}
		
		return hasPotion;
	}
	
	public boolean canCraftSomethingUsefull() {
		return !getUsefullCraftingRecipes().isEmpty();
	}
	
	public ArrayList<CraftingRecipe> getUsefullCraftingRecipes(){
		ArrayList<CraftingRecipe> craftingRecipies = getCopyOfAllCraftingRecipies();
		
		for(int i = craftingRecipies.size()-1; i >= 0; i--) {
			
			CraftingRecipe craftingRecipe = craftingRecipies.get(i);
			
			if(!craftingRecipe.canUse(this)) {
				craftingRecipies.remove(craftingRecipe);
				continue;
			}
			
			if(craftingRecipe.getType() == RecipeType.EQUIPMENT_RECIPE) {
				
				if(ownerRef.getHeroType() == HeroType.WARRIOR) {
					if(craftingRecipe.getProducedEquipmentType() == EquipmentType.BOW || craftingRecipe.getProducedEquipmentType() == EquipmentType.DAGGER) {
						craftingRecipies.remove(craftingRecipe);
						continue;
					}
				}
				else if(ownerRef.getHeroType() == HeroType.ROGUE) {
					if(craftingRecipe.getProducedEquipmentType() == EquipmentType.BOW || craftingRecipe.getProducedEquipmentType() == EquipmentType.SWORD
							|| craftingRecipe.getProducedEquipmentType() == EquipmentType.SHIELD || craftingRecipe.getProducedEquipmentType() == EquipmentType.HAMMER) {
						craftingRecipies.remove(craftingRecipe);
						continue;
					}
				}
				else if(ownerRef.getHeroType() == HeroType.ARCHER) {
					if(craftingRecipe.getProducedEquipmentType() == EquipmentType.DAGGER || craftingRecipe.getProducedEquipmentType() == EquipmentType.SWORD
							|| craftingRecipe.getProducedEquipmentType() == EquipmentType.SHIELD || craftingRecipe.getProducedEquipmentType() == EquipmentType.HAMMER) {
						craftingRecipies.remove(craftingRecipe);
						continue;
					}
				}
				
				Equipment equipedEquipment = getEquipedEquipment(craftingRecipe.getProducedEquipmentType());
				
				if(!isFullyEquiped() && equipedEquipment != null && equipedEquipment.getMaterial() == craftingRecipe.getMainIngredient()) {
					craftingRecipies.remove(craftingRecipe);
					continue;
				}
				
				if((equipedWeapon.getType() == EquipmentType.HAMMER && (craftingRecipe.getProducedEquipmentType() == EquipmentType.SWORD || craftingRecipe.getProducedEquipmentType() == EquipmentType.SHIELD)) 
						|| (equipedWeapon.getType() == EquipmentType.SWORD && craftingRecipe.getProducedEquipmentType() == EquipmentType.HAMMER)) {
					
					if(!craftingRecipe.canCraftBetterEquipment_WarriorWeapon(getBestHammer(), getBestSword(), getBestShield())) {
						craftingRecipies.remove(craftingRecipe);
					}
				}
				else {
					if(!craftingRecipe.canCraftBetterEquipment(equipedEquipment)) {
						craftingRecipies.remove(craftingRecipe);
					}
				}
				
			}
			else {
				if(!desiresPotion(craftingRecipe.getProducedPotionType())){
					craftingRecipies.remove(craftingRecipe);
				}
			}
			
		}
		
		return craftingRecipies;
	}
	
	private Equipment getBestShield() {
		Equipment bestShield = null;
		for (Equipment equipment : shieldInventory) {
			if(bestShield == null) {
				bestShield = equipment;
			}
			else if(bestShield.getHealthBonus() < equipment.getHealthBonus()) {
				bestShield = equipment;
			}
		}
		return bestShield;
	}

	private Equipment getBestSword() {
		Equipment bestSword = null;
		for (Equipment equipment : swordInventory) {
			if(bestSword == null) {
				bestSword = equipment;
			}
			else if(bestSword.getAttackBonus() < equipment.getAttackBonus()) {
				bestSword = equipment;
			}
		}
		return bestSword;
	}

	private Equipment getBestHammer() {
		Equipment bestHammer = null;
		for (Equipment equipment : hammerInventory) {
			if(bestHammer == null) {
				bestHammer = equipment;
			}
			else if(bestHammer.getAttackBonus() < equipment.getAttackBonus()) {
				bestHammer = equipment;
			}
		}
		return bestHammer;
	}
	
	public boolean desiresPotion(PotionType type) {
		
		int potionAmount = 0;
		
		for (BasePotion potion : potionInventory) {
			if(potion.getType() == type) {
				potionAmount++;
			}
		}
		
		if(potionAmount < desiredNumberOfPotions.get(type)) {
			return true;
		}
		return false;
	}
	
	private boolean isFullyEquiped() {
		if(equipedBoots == null) {
			return false;
		}
		if(equipedChestplate == null) {
			return false;
		}
		if(equipedLeggings == null) {
			return false;
		}
		if(equipedHelmet == null) {
			return false;
		}
		if(equipedWeapon == null) {
			return false;
		}
		
		if(ownerRef.getHeroType() == HeroType.WARRIOR && equipedWeapon.getType() == EquipmentType.SWORD && equipedShield == null) {
			return false;
		}
		
		if(ownerRef.getHeroType() == HeroType.ROGUE && equipedSecondWeapon == null) {
			return false;
		}
		
		return true;
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
		
		betterEquipment.remove(equipedBoots);
		betterEquipment.remove(equipedChestplate);
		betterEquipment.remove(equipedHelmet);
		betterEquipment.remove(equipedLeggings);
		betterEquipment.remove(equipedShield);
		betterEquipment.remove(equipedWeapon);
		betterEquipment.remove(equipedSecondWeapon);

		if(betterEquipment.isEmpty()) {
			return null;
		}
		
		Random random = new Random();
		int randomIndex = random.nextInt(betterEquipment.size());
		
		return betterEquipment.get(randomIndex);
	}

	public void removeEquipmentByName(String equipmentName) {
		Equipment removedEquipment = null;
		
		for(Equipment equipment : equipmentInventory) {
			if(equipment.getName().contentEquals(equipmentName)) {
				removedEquipment = equipment;
				break;
			}
		}
		
		if(removedEquipment == null) {
			return;
		}
		
		itemInventory.remove(removedEquipment);
		equipmentInventory.remove(removedEquipment);
		bootsInventory.remove(removedEquipment);
		chestplateInventory.remove(removedEquipment);
		leggingsInventory.remove(removedEquipment);
		helmetInventory.remove(removedEquipment);
		shieldInventory.remove(removedEquipment);
		swordInventory.remove(removedEquipment);
		daggerInventory.remove(removedEquipment);
		bowInventory.remove(removedEquipment);
		hammerInventory.remove(removedEquipment);
		
		GameOntology.removeOwnedRelationshipBetweenAgentAndItem(ownerRef, removedEquipment.getName());
	}
	
	@Override
	protected int getAllMaterialsValue() {
		int totalValue = 0;
		
		for (int i = 0; i < MaterialType.values().length; i++) {
			MaterialType material = MaterialType.values()[i];
			totalValue += materialInventory.get(material) * ItemUtils.getBaseMaterialPrice(material);
		}
		
		return totalValue;
	}

	public boolean isMissingPotions() {
		HashMap<PotionType, Integer> numberOfPotions = new HashMap<>();
		numberOfPotions.put(PotionType.HEALTH, 0);
		numberOfPotions.put(PotionType.FLEEING, 0);
		numberOfPotions.put(PotionType.REGENARATION, 0);
		numberOfPotions.put(PotionType.SUPERHEALTH, 0);
		
		for(BasePotion potion : potionInventory) {
			numberOfPotions.put(potion.getType(), numberOfPotions.get(potion.getType()) + 1);
		}
		
		for(PotionType type : desiredNumberOfPotions.keySet()) {
			if(numberOfPotions.get(type) < desiredNumberOfPotions.get(type)) {
				return true;
			}
		}
		
		return false;
	}

	public PotionType getMissingPotionType() {
		HashMap<PotionType, Integer> numberOfPotions = new HashMap<>();
		numberOfPotions.put(PotionType.HEALTH, 0);
		numberOfPotions.put(PotionType.FLEEING, 0);
		numberOfPotions.put(PotionType.REGENARATION, 0);
		numberOfPotions.put(PotionType.SUPERHEALTH, 0);
		
		for(BasePotion potion : potionInventory) {
			numberOfPotions.put(potion.getType(), numberOfPotions.get(potion.getType()) + 1);
		}
		
		ArrayList<PotionType> missingPotionTypes = new ArrayList<>();
		
		for(PotionType type : desiredNumberOfPotions.keySet()) {
			if(numberOfPotions.get(type) < desiredNumberOfPotions.get(type)) {
				missingPotionTypes.add(type);
			}
		}

		Random random = new Random();
		int index = random.nextInt(missingPotionTypes.size());
		return missingPotionTypes.get(index);
	}

	public Equipment getRandomUnusedEquipment() {
		ArrayList<Equipment> nonEquipedEquipment = new ArrayList<>();
		
		nonEquipedEquipment.addAll(equipmentInventory);
		nonEquipedEquipment.remove(equipedBoots);
		nonEquipedEquipment.remove(equipedHelmet);
		nonEquipedEquipment.remove(equipedChestplate);
		nonEquipedEquipment.remove(equipedLeggings);
		nonEquipedEquipment.remove(equipedShield);
		nonEquipedEquipment.remove(equipedWeapon);
		nonEquipedEquipment.remove(equipedSecondWeapon);
		
		if(nonEquipedEquipment.isEmpty()) {
			return null;
		}
		
		Random random = new Random();
		int index = random.nextInt(nonEquipedEquipment.size());
		
		return nonEquipedEquipment.get(index);
	}

	@Override
	protected void logAquiredPotion(BasePotion potion) {
		ownerRef.addToLog("I obtained " + potion.getQuality() + " " + potion.getType().toString());
	}

	@Override
	protected void logAquiredEquipment(Equipment equipment) {
		ownerRef.addToLog("I obtained " + equipment.getQuality() + " " + equipment.getMaterial() + " " + equipment.getType());
	}

	public boolean doesOwnerHaveAbilityTrait(HeroAbilityTrait trait) {
		return ownerRef.hasAbilityTrait(trait);
	}

	public void setOwner(BaseHeroAgent owner) {
		this.ownerRef = owner;
	}
}
