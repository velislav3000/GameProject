package agents.items;

import java.util.ArrayList;
import java.util.HashMap;

import agents.items.BaseItem.ItemQuality;
import agents.items.equipment.Equipment.EquipmentType;
import agents.items.potions.BasePotion.PotionType;
import collectiables.materials.BaseMaterial.MaterialType;
import database.DBConnector;
import ontology.GameOntology;

public class ItemUtils {

	public static ArrayList<CraftingRecipe> craftingRecipies;
	private static HashMap<String, Integer> baseItemPrices;
	private static HashMap<MaterialType, Float> materialPriceMultipliers;
	private static HashMap<ItemQuality, Float> qualityPriceMultipliers;
	private static HashMap<MaterialType, Integer> baseMaterialPrices;
	
	public static void initPrices() {
		
		DBConnector.initDatabase();
		
		if(baseItemPrices == null) {
			baseItemPrices = new HashMap<>();
			for (EquipmentType type : EquipmentType.values()) {
				int price = DBConnector.getItemBasePrice(type.toString());
				baseItemPrices.put(type.toString(), price);
			}
			
			for (PotionType type : PotionType.values()) {
				int price = DBConnector.getItemBasePrice(type.toString());
				baseItemPrices.put(type.toString(), price);
			}
		}
		
		if(materialPriceMultipliers == null) {
			materialPriceMultipliers = new HashMap<>();
			
			MaterialType[] materials = {MaterialType.STONE, MaterialType.IRON, MaterialType.COBALT, MaterialType.MYTHRIL, MaterialType.ADAMANTITE};
			
			for (MaterialType material : materials) {
				float priceMultiplier = DBConnector.getMaterialPriceMultiplier(material.toString());
				materialPriceMultipliers.put(material, priceMultiplier);
			}
		}
		
		if(qualityPriceMultipliers == null) {
			qualityPriceMultipliers = new HashMap<>();
			
			for (ItemQuality quality : ItemQuality.values()) {
				float priceMultiplier = DBConnector.getQualityPriceMultiplier(quality.toString());
				qualityPriceMultipliers.put(quality, priceMultiplier);
			}
		}
		
		if(baseMaterialPrices == null) {
			baseMaterialPrices = new HashMap<>();
			
			for (MaterialType material : MaterialType.values()) {
				int price = DBConnector.getMaterialBasePrice(material.toString());
				baseMaterialPrices.put(material, price);
			}
		}
		
		System.out.println("done");
		
	}
	
	public static void initCraftingRecipies() {
		if(craftingRecipies == null) {
			craftingRecipies = GameOntology.getAllCraftingRecipes();
		}
	}
	
	public static int getBaseItemPrice(String itemType) {
		return baseItemPrices.get(itemType);
	}
	
	public static float getMaterialPriceMultiplier(MaterialType material) {
		return materialPriceMultipliers.get(material);
	}
	
	public static float getQualityPriceMultiplier(ItemQuality quality) {
		return qualityPriceMultipliers.get(quality);
	}
	
	public static int getBaseMaterialPrice(MaterialType material) {
		return baseMaterialPrices.get(material);
	}
}
