package collectiables;

import java.util.ArrayList;
import java.util.HashMap;

import agents.items.BaseItem;
import agents.items.equipment.Equipment;
import agents.items.potions.BasePotion;
import collectiables.materials.BaseMaterial.MaterialType;
import jade.util.leap.Serializable;

public class CollectableLoot implements Serializable{
	private int gold;
	private HashMap<MaterialType, Integer> materials;
	private ArrayList<Equipment> equipment;
	private ArrayList<BasePotion> potions;
	
	public CollectableLoot(int gold, HashMap<MaterialType, Integer> materials, ArrayList<Equipment> equipment,
			ArrayList<BasePotion> potions) {
		this.gold = gold;
		this.materials = materials;
		this.equipment = equipment;
		this.potions = potions;
	}

	public int getGold() {
		return gold;
	}

	public HashMap<MaterialType, Integer> getMaterials() {
		return materials;
	}

	public ArrayList<Equipment> getEquipment() {
		return equipment;
	}

	public ArrayList<BasePotion> getPotions() {
		return potions;
	}
}
