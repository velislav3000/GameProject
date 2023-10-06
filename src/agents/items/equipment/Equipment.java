package agents.items.equipment;

import java.io.Serializable;

import agents.items.BaseItem;
import agents.items.ItemUtils;
import collectiables.materials.BaseMaterial.MaterialType;
import ontology.GameOntology;

public class Equipment extends BaseItem{
	private int healthBonus = 0;
	private int attackBonus = 0;
	
	private static int equipmentId = 0;
	
	public enum EquipmentType{
		BOOTS,
		CHESTPLATE,
		HELMET,
		LEGGINGS,
		SHIELD,
		BOW,
		DAGGER,
		HAMMER,
		SWORD
	}
	
	private MaterialType material;
	private EquipmentType type;
	
	public Equipment(MaterialType material, ItemQuality quality, EquipmentType type) {
		this.material = material;
		this.quality = quality;
		this.type = type;
		
		setBaseStats();
		updateBonus();
		
		equipmentId++;
		name = type + "" + equipmentId;
		name = name.substring(0, 1) + name.substring(1).toLowerCase();
		
		setPrice();
		
		GameOntology.addEquipment(this);
	}
	
	private void setBaseStats() {
		healthBonus = 0;
		attackBonus = 0;
		
		switch (type) {
		case BOOTS: {
			healthBonus = 15;
			break;
		}
		case CHESTPLATE: {
			healthBonus = 45;
			break;
		}
		case LEGGINGS: {
			healthBonus = 37;
			break;
		}
		case HELMET: {
			healthBonus = 22;
			break;
		}
		case SHIELD: {
			healthBonus = 45;
			break;
		}
		case SWORD: {
			attackBonus = 10;
			break;
		}
		case DAGGER: {
			attackBonus = 6;
			break;
		}
		case BOW: {
			attackBonus = 9;
			break;
		}
		case HAMMER: {
			attackBonus = 20;
			break;
		}
		}
	}
	
	private void updateBonus() {
		float materialMultiplier = 1;
		switch (material) {
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
		
		float qualityMultiplier = 1;
		switch (quality) {
		case POOR: {
			qualityMultiplier = 0.5f;
			break;
		}
		case AVERAGE: {
			qualityMultiplier = 1;
			break;
		}
		case GREAT: {
			qualityMultiplier = 2;
			break;
		}
		case SUPREME: {
			qualityMultiplier = 4;
			break;
		}
		}
		
		healthBonus = Math.round(healthBonus * materialMultiplier * qualityMultiplier);
		attackBonus = Math.round(attackBonus * materialMultiplier * qualityMultiplier);
	}

	public int getHealthBonus() {
		return healthBonus;
	}

	public int getAttackBonus() {
		return attackBonus;
	}

	public MaterialType getMaterial() {
		return material;
	}

	public EquipmentType getType() {
		return type;
	}

	public static int getEquipmentId() {
		return equipmentId;
	}

	public int getBonus() {
		switch (type) {
		case BOOTS:
		case CHESTPLATE:
		case LEGGINGS:
		case HELMET:
		case SHIELD: {
			return getHealthBonus();
		}
		default:{
			return getAttackBonus();
		}
		}
	}

	@Override
	protected void setPrice() {
		price = ItemUtils.getBaseItemPrice(type.toString());
		float materialMultiplier = ItemUtils.getMaterialPriceMultiplier(material);
		float qualityMultiplier = ItemUtils.getQualityPriceMultiplier(quality);
		
		price = Math.round(price * materialMultiplier * qualityMultiplier);
	}
	
	
}
