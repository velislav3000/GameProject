package agents.items;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import agents.BaseEntityAgent;
import agents.behaviours.FleeingBehaviour;
import agents.heroAgents.BaseHeroAgent.HeroAbilityTrait;
import agents.items.BaseItem.ItemQuality;
import agents.items.equipment.Equipment;
import agents.items.equipment.Equipment.EquipmentType;
import agents.items.potions.BasePotion;
import agents.items.potions.BasePotion.PotionType;
import agents.items.potions.FleeingPotion;
import agents.items.potions.HealthPotion;
import agents.items.potions.RegenarationPotion;
import agents.items.potions.SuperHealthPotion;
import collectiables.materials.BaseMaterial.MaterialType;

public class CraftingRecipe {
	private HashMap<MaterialType, Integer> ingredients;
	private EquipmentType producedEquipmentType;
	private PotionType producedPotionType;
	
	public enum RecipeType{
		EQUIPMENT_RECIPE,
		POTION_RECIPE
	}
	
	private RecipeType type;

	public CraftingRecipe(HashMap<MaterialType, Integer> ingredients, EquipmentType producedEquipmentType) {
		this.ingredients = ingredients;
		this.producedEquipmentType = producedEquipmentType;
		this.type = RecipeType.EQUIPMENT_RECIPE;
	}
	
	public CraftingRecipe(HashMap<MaterialType, Integer> ingredients, PotionType producedPotionType) {
		this.ingredients = ingredients;
		this.producedPotionType = producedPotionType;
		this.type = RecipeType.POTION_RECIPE;
	}
	
	public boolean canUse(HeroInventory inventoryRef) {
		boolean canUse = true;
		
		for (MaterialType type : ingredients.keySet()) {
			if(inventoryRef.getMaterialTotal(type) < ingredients.get(type)) {
				canUse = false;
				break;
			}
		}
		
		return canUse;
	}
	
	public boolean canUse(CityInventory inventoryRef) {
		boolean canUse = true;
		
		for (MaterialType type : ingredients.keySet()) {
			if(inventoryRef.getImrpovingMaterialTotal(type) < ingredients.get(type)) {
				canUse = false;
				break;
			}
		}
		
		return canUse;
	}
	
	public boolean use(HeroInventory inventoryRef) {
		if(canUse(inventoryRef)) {
			
			boolean isResourceful = inventoryRef.doesOwnerHaveAbilityTrait(HeroAbilityTrait.RESOURCEFUL);
			boolean shouldUseHalfTheMaterials = false;
			
			if(isResourceful) {
				Random random = new Random();
				int chance = random.nextInt(4);
				
				if(chance == 0) {
					shouldUseHalfTheMaterials = true;
				}
			}
			
			MaterialType material = null;
			for (MaterialType type : ingredients.keySet()) {
				if(material == null) {
					material = type;
				}
				int materialAmount = ingredients.get(type);
				
				if(shouldUseHalfTheMaterials) {
					materialAmount = materialAmount/2;
				}
				
				inventoryRef.removeMaterial(type, materialAmount);
			}
			
			ItemQuality quality = ItemQuality.POOR;
			Random random = new Random();
			int chance = random.nextInt(20);
			if(chance == 0) {
				quality = ItemQuality.SUPREME;
			}
			else if(chance <= 3) {
				quality = ItemQuality.GREAT;
			}
			else if(chance <= 9) {
				quality = ItemQuality.AVERAGE;
			}
			
			if(type == RecipeType.EQUIPMENT_RECIPE) {
				
				if(inventoryRef.doesOwnerHaveAbilityTrait(HeroAbilityTrait.BLACKSMITH) && quality != ItemQuality.SUPREME) {
					quality = ItemQuality.values()[quality.ordinal()+1];
				}
				else if(inventoryRef.doesOwnerHaveAbilityTrait(HeroAbilityTrait.FORGE_DISASTER) && quality != ItemQuality.POOR) {
					quality = ItemQuality.values()[quality.ordinal()-1];
				}
				
				Equipment equipment = new Equipment(material, quality, producedEquipmentType);
				inventoryRef.addEquipment(equipment);
				inventoryRef.equipBestEquipment();
			}
			else {
				
				if(inventoryRef.doesOwnerHaveAbilityTrait(HeroAbilityTrait.ALCHEMIST) && quality != ItemQuality.SUPREME) {
					quality = ItemQuality.values()[quality.ordinal()+1];
				}
				else if(inventoryRef.doesOwnerHaveAbilityTrait(HeroAbilityTrait.HERB_WASTER) && quality != ItemQuality.POOR) {
					quality = ItemQuality.values()[quality.ordinal()-1];
				}
				
				BasePotion potion = null;
				
				switch (producedPotionType) {
				case HEALTH: {
					potion = new HealthPotion(quality);
					break;
				}
				case FLEEING: {
					potion = new FleeingPotion(quality);
					break;
				}
				case REGENARATION: {
					potion = new RegenarationPotion(quality);
					break;
				}
				case SUPERHEALTH: {
					potion = new SuperHealthPotion(quality);
					break;
				}
				}
				
				inventoryRef.addPotion(potion);
			}
			
			return true;
		}
		return false;
	}
	
	public boolean use(CityInventory inventoryRef) {
		if(canUse(inventoryRef)) {
			
			MaterialType material = null;
			for (MaterialType type : ingredients.keySet()) {
				if(material == null) {
					material = type;
				}
				inventoryRef.removeMaterial(type, ingredients.get(type));
			}
			
			ItemQuality quality = ItemQuality.POOR;
			Random random = new Random();
			int chance = random.nextInt(20);
			if(chance == 0) {
				quality = ItemQuality.SUPREME;
			}
			else if(chance <= 3) {
				quality = ItemQuality.GREAT;
			}
			else if(chance <= 9) {
				quality = ItemQuality.AVERAGE;
			}
			
			if(type == RecipeType.EQUIPMENT_RECIPE) {
				Equipment equipment = new Equipment(material, quality, producedEquipmentType);
				inventoryRef.addCraftedEquipment(equipment);
			}
			else {
				
				BasePotion potion = null;
				
				switch (producedPotionType) {
				case HEALTH: {
					potion = new HealthPotion(quality);
					break;
				}
				case FLEEING: {
					potion = new FleeingPotion(quality);
					break;
				}
				case REGENARATION: {
					potion = new RegenarationPotion(quality);
					break;
				}
				case SUPERHEALTH: {
					potion = new SuperHealthPotion(quality);
					break;
				}
				}
				
				inventoryRef.addCraftedPotion(potion);
			}
			
			return true;
		}
		return false;
	}

	public boolean canCraftBetterEquipment(Equipment equipedEquipment) {
		if(type == RecipeType.POTION_RECIPE) {
			return false;
		}
		
		if(equipedEquipment == null) {
			return true;
		}
		
		MaterialType mainMaterial = null;
		
		for(MaterialType mat : ingredients.keySet()) {
			if(mat!= MaterialType.STRING) {
				mainMaterial = mat;
			}
		}
		
		if(mainMaterial.ordinal() > equipedEquipment.getMaterial().ordinal()) {
			return true;
		}
		else if(mainMaterial.ordinal() == equipedEquipment.getMaterial().ordinal() && equipedEquipment.getQuality().ordinal() < ItemQuality.GREAT.ordinal()) {
			return true;
		}
		
		return false;
	}
	
	public boolean canCraftBetterEquipment_WarriorWeapon(Equipment bestHammer, Equipment bestSword, Equipment bestShield) {
		if(type == RecipeType.POTION_RECIPE) {
			return false;
		}
		
		if(bestHammer == null && bestSword == null && bestShield == null) {
			return true;
		}
		
		MaterialType mainMaterial = null;
		
		for(MaterialType mat : ingredients.keySet()) {
			if(mat!= MaterialType.STRING) {
				mainMaterial = mat;
			}
		}
		
		float materialMultiplier = 1;
		switch (mainMaterial) {
		case STONE: {
			materialMultiplier = 1;
			break;
		}
		case IRON: {
			materialMultiplier = 3;
			break;
		}
		case COBALT: {
			materialMultiplier = 5;
			break;
		}
		case MYTHRIL: {
			materialMultiplier = 8;
			break;
		}
		case ADAMANTITE: {
			materialMultiplier = 12;
			break;
		}
		}
		
		if(producedEquipmentType == EquipmentType.HAMMER) {
			
			if(bestSword == null) {
				return true;
			}
			
			if(bestShield == null && mainMaterial.ordinal() > bestSword.getMaterial().ordinal()) {
				return true;
			}
			
			int baseHammerAttack = 20;
			if(baseHammerAttack*materialMultiplier > bestSword.getAttackBonus() + bestShield.getHealthBonus()/3) {
				return true;
			}
		}
		else if(producedEquipmentType == EquipmentType.SWORD) {
			if(bestHammer == null) {
				return true;
			}
			
			if(bestShield == null && mainMaterial.ordinal() <= bestHammer.getMaterial().ordinal()) {
				return false;
			}
			
			int baseSwordAttack = 10;
			if(baseSwordAttack*materialMultiplier + bestShield.getHealthBonus()/3 > bestHammer.getAttackBonus()) {
				return true;
			}
			
		}
		else if(producedEquipmentType == EquipmentType.SHIELD) {
			if(bestHammer == null) {
				return true;
			}
			
			if(bestSword == null && mainMaterial.ordinal() <= bestHammer.getMaterial().ordinal()) {
				return false;
			}
			
			int baseShieldBonus = 45;
			if(bestSword.getAttackBonus() + baseShieldBonus*materialMultiplier/3 > bestHammer.getAttackBonus()) {
				return true;
			}
			
		}
		
		return false;
	}

	public EquipmentType getProducedEquipmentType() {
		return producedEquipmentType;
	}

	public RecipeType getType() {
		return type;
	}

	public PotionType getProducedPotionType() {
		return producedPotionType;
	}

	public MaterialType getMainIngredient() {
		for (MaterialType mat : ingredients.keySet()) {
			if(mat != MaterialType.STRING) {
				return mat;
			}
		}
		return null;
	}
}
