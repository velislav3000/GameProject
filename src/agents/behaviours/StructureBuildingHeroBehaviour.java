package agents.behaviours;

import java.util.ArrayList;
import java.util.Random;

import agents.AgentUtils;
import agents.BaseEntityAgent;
import agents.heroAgents.BaseHeroAgent;
import agents.items.HeroInventory;
import agents.items.ItemUtils;
import collectiables.materials.BaseMaterial.MaterialType;
import jade.core.behaviours.OneShotBehaviour;
import tiles.Tile;

public class StructureBuildingHeroBehaviour extends OneShotBehaviour {
	
	private transient BaseHeroAgent heroAgentRef;
	private transient HeroInventory inventoryRef;
	
	public StructureBuildingHeroBehaviour(BaseHeroAgent agent, HeroInventory inventory) {
		heroAgentRef = agent;
		inventoryRef = inventory;
	}

	@Override
	public void action() {
		ArrayList<Tile> tilesToBuildOn = new ArrayList<>();
		
		for (Tile tile : heroAgentRef.getCurrentTile().getNeighbours()) {
			if(tile.canAgentMoveToTile(heroAgentRef) && tile.getOwningCity() == null) {
				tilesToBuildOn.add(tile);
			}
		}
		
		Random random = new Random();
		int index = random.nextInt(tilesToBuildOn.size());
		
		Tile structureTile = tilesToBuildOn.get(index);
		
		if(heroAgentRef.getGold() < 1000) {
			int neededGold = 1000 - heroAgentRef.getGold();
			removeMaterialsNeededForBuildingCity(neededGold);
			
			heroAgentRef.removeGold(heroAgentRef.getGold());
		}
		else {
			heroAgentRef.removeGold(1000);
		}
		
		AgentUtils.spawnAgent("City", true, structureTile, heroAgentRef);
		
		heroAgentRef.removeBehaviour(this);
	}
	
	private void removeMaterialsNeededForBuildingCity(int neededValue) {
		
		for (MaterialType material : MaterialType.values()) {
			int materialAmount = inventoryRef.getMaterialTotal(material);
			int totalMaterialValue = materialAmount * ItemUtils.getBaseMaterialPrice(material);
			
			if(totalMaterialValue >= neededValue) {
				int neededMaterialAmount = Math.round(1.0F*neededValue / ItemUtils.getBaseMaterialPrice(material));
				inventoryRef.removeMaterial(material, neededMaterialAmount);
				return;
			}
			else {
				inventoryRef.removeMaterial(material, materialAmount);
				neededValue -= totalMaterialValue;
			}
		}
	}
}
