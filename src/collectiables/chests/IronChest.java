package collectiables.chests;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javax.imageio.ImageIO;

import agents.items.BaseItem;
import agents.items.BaseItem.ItemQuality;
import agents.items.equipment.Equipment;
import agents.items.equipment.Equipment.EquipmentType;
import agents.items.potions.BasePotion;
import agents.items.potions.HealthPotion;
import agents.items.potions.RegenarationPotion;
import collectiables.CollectableLoot;
import collectiables.chests.BaseChest.ChestType;
import collectiables.materials.BaseMaterial.MaterialType;

public class IronChest extends BaseChest{
	
	protected static BufferedImage image;
	
	public IronChest() {
		super();
		
		imageName = "chest_iron.png";
		chestType = ChestType.IRON_CHEST;
		
		name = "IronChest" + chestId;
		
		Random random = new Random();
		int gold = random.nextInt(400);
		
		HashMap<MaterialType, Integer> materials = new HashMap<>();
		materials.put(MaterialType.IRON, random.nextInt(25));
		materials.put(MaterialType.COBALT, random.nextInt(12));
		materials.put(MaterialType.MYTHRIL, random.nextInt(5));
		
		ArrayList<Equipment> equipmentArray = new ArrayList<>();
		int chance = random.nextInt(10);
		if(chance == 0) {
			EquipmentType type = getRandomEquipmentType();
			ItemQuality quality = getRandomItemQuality();
			MaterialType material = getRandomMaterialType();
			
			Equipment equipment = new Equipment(material, quality, type);
			equipmentArray.add(equipment);
		}

		ArrayList<BasePotion> potions = new ArrayList<>();
		chance = random.nextInt(10);
		if(chance == 0) {
			for (int i = 0; i < 3; i++) {
				ItemQuality quality = getRandomItemQuality();
				HealthPotion potion = new HealthPotion(quality);
				potions.add(potion);
			}
		}
		else if(chance == 1) {
			ItemQuality quality = getRandomItemQuality();
			RegenarationPotion potion = new RegenarationPotion(quality);
			potions.add(potion);
		}
		
		loot = new CollectableLoot(gold, materials, equipmentArray, potions);

		ArrayList<BaseItem> items = new ArrayList<>();
		items.addAll(equipmentArray);
		items.addAll(potions);
		addToOntology(items);
	}
	
	public IronChest(String name, CollectableLoot loot) {
		super(name, loot);
		
		imageName = "chest_iron.png";
		chestType = ChestType.IRON_CHEST;
	}
	
	@Override
	public BufferedImage getImage() {
		if(image == null) {
			try {
				image = ImageIO.read(new File(iconPath, imageName));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return image;
	}
	
	private EquipmentType getRandomEquipmentType() {
		Random random = new Random();
		int id = random.nextInt(9);
		switch (id) {
		case 0: {
			return EquipmentType.BOOTS;
		}
		case 1: {
			return EquipmentType.CHESTPLATE;
		}
		case 2: {
			return EquipmentType.LEGGINGS;
		}
		case 3: {
			return EquipmentType.HELMET;
		}
		case 4: {
			return EquipmentType.SHIELD;
		}
		case 5: {
			return EquipmentType.SWORD;
		}
		case 6: {
			return EquipmentType.BOW;
		}
		case 7: {
			return EquipmentType.DAGGER;
		}
		case 8: {
			return EquipmentType.HAMMER;
		}
		}
		return EquipmentType.BOOTS;
	}
	
	private ItemQuality getRandomItemQuality() {
		Random random = new Random();
		int id = random.nextInt(100);
		
		if(id < 5) {
			return ItemQuality.SUPREME;
		}
		else if(id < 20) {
			return ItemQuality.GREAT;
		}
		else if(id < 50) {
			return ItemQuality.AVERAGE;
		}
		return ItemQuality.POOR;
	}
	
	private MaterialType getRandomMaterialType() {
		Random random = new Random();
		int id = random.nextInt(10);
		if(id == 0) {
			return MaterialType.MYTHRIL;
		}
		else if (id < 4 ){
			return MaterialType.COBALT;
		}
		else {
			return MaterialType.IRON;
		}
	}
}