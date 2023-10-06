package agents.behaviours.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import agents.heroAgents.BaseHeroAgent;
import agents.heroAgents.BaseHeroAgent.HeroType;
import agents.heroAgents.CityAgent;
import agents.items.BaseItem;
import agents.items.CityInventory;
import agents.items.equipment.Equipment;
import agents.items.equipment.Equipment.EquipmentType;
import agents.items.potions.BasePotion;
import collectiables.CollectableLoot;
import collectiables.materials.BaseMaterial.MaterialType;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import tiles.Tile;

public class GatherCollectibleStructureBehaviour extends OneShotBehaviour{

	private transient CityAgent agentRef;
	private transient ArrayList<Tile> tilesWithMaterials;
	
	public GatherCollectibleStructureBehaviour(CityAgent agent, ArrayList<Tile> tilesWithMaterials) {
		agentRef = agent;
		this.tilesWithMaterials = tilesWithMaterials;
	}
	
	@Override
	public void action() {
		int totalGold = 0;
		HashMap<MaterialType, Integer> totalMaterials = new HashMap<>();
		
		for (int i = 0; i < MaterialType.values().length; i++) {
			totalMaterials.put(MaterialType.values()[i], 0);
		}
		
		ArrayList<Equipment> totalEquipment = new ArrayList<>();
		ArrayList<BasePotion> totalPotions = new ArrayList<>();
		
		for (Tile tile : tilesWithMaterials) {
			CollectableLoot loot = tile.collectCollectable();
			
			totalGold += loot.getGold();
			if(loot.getMaterials() != null) {
				for (MaterialType mat : loot.getMaterials().keySet()) {
					totalMaterials.put(mat, totalMaterials.get(mat) + loot.getMaterials().get(mat));
				}
			}
			
			if(loot.getEquipment() != null) {
				for (Equipment item : loot.getEquipment()) {
					totalEquipment.add((Equipment) item);
				}
			}
			
			if(loot.getPotions() != null) {
				for (BasePotion item : loot.getPotions()) {
					totalPotions.add((BasePotion) item);
				}
			}
			
		}
		
		if(agentRef.getOwner() != null) {
			
			BaseHeroAgent owner = (BaseHeroAgent) agentRef.getOwner();
			
			int goldToKeep = totalGold/2;
			int goldForOwner = totalGold - goldToKeep;
			
			HashMap<MaterialType, Integer> materialsToKeep = new HashMap<>();
			HashMap<MaterialType, Integer> materialsForOwner = new HashMap<>();
			
			for (MaterialType mat : totalMaterials.keySet()) {
				materialsToKeep.put(mat, totalMaterials.get(mat)/2);
				materialsForOwner.put(mat, totalMaterials.get(mat) - materialsToKeep.get(mat));
			}
			
			ArrayList<Equipment> equipmentToKeep = new ArrayList<>();
			ArrayList<Equipment> equipmentForOwner = new ArrayList<>();
			ArrayList<BasePotion> potionsToKeep = new ArrayList<>();
			ArrayList<BasePotion> potionsForOwner = new ArrayList<>();
			
			for (BasePotion potion : totalPotions) {
				if(owner.desiresPotion(potion.getType())) {
					potionsForOwner.add(potion);
				}
				else {
					potionsToKeep.add(potion);
				}
			}
			
			HashMap<EquipmentType, Equipment> bestEquipment = new HashMap<>();
			
			for (Equipment equipment : totalEquipment) {
				
				if((equipment.getType() == EquipmentType.SWORD || equipment.getType() == EquipmentType.SHIELD || 
						equipment.getType() == EquipmentType.HAMMER) && owner.getHeroType() != HeroType.WARRIOR) {
					
					continue;
				}
				
				if(equipment.getType() == EquipmentType.DAGGER && owner.getHeroType() != HeroType.ROGUE) {
					continue;
				}
				
				if(equipment.getType() == EquipmentType.BOW && owner.getHeroType() != HeroType.ARCHER) {
					continue;
				}
				
				if(!bestEquipment.containsKey(equipment.getType())) {
					if(owner.getEquipedEquipment(equipment.getType()) != null) {
						bestEquipment.put(equipment.getType(), owner.getEquipedEquipment(equipment.getType()));
					}
					else {
						bestEquipment.put(equipment.getType(), equipment);
						continue;
					}
				}
				
				if(equipment.getBonus() > bestEquipment.get(equipment.getType()).getBonus()) {
					bestEquipment.put(equipment.getType(), equipment);
				}
			}
			
			agentRef.collectLoot(new CollectableLoot(goldToKeep, materialsToKeep, equipmentToKeep, potionsToKeep));
			owner.collectLoot(new CollectableLoot(goldForOwner, materialsForOwner, equipmentForOwner, potionsForOwner));
		}
		else {
			agentRef.collectLoot(new CollectableLoot(totalGold, totalMaterials, totalEquipment, totalPotions));
		}
		
		agentRef.removeBehaviour(this);
	}

}
