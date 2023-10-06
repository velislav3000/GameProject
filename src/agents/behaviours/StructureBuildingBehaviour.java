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

public class StructureBuildingBehaviour extends OneShotBehaviour{

	private transient BaseEntityAgent agentRef;
	
	public StructureBuildingBehaviour(BaseEntityAgent agent) {
		agentRef = agent;
	}
	
	@Override
	public void action() {
		ArrayList<Tile> tilesToBuildOn = new ArrayList<>();
		
		for (Tile tile : agentRef.getCurrentTile().getNeighbours()) {
			if(tile.canAgentMoveToTile(agentRef) && tile.getOwningCity() == null) {
				tilesToBuildOn.add(tile);
			}
		}
		
		Random random = new Random();
		int index = random.nextInt(tilesToBuildOn.size());
		
		Tile structureTile = tilesToBuildOn.get(index);
		
		if(agentRef.getLocalName().contains("Skeleton")) {
			AgentUtils.spawnAgent("Graveyard", false, structureTile, agentRef);
		}
		else {
			AgentUtils.spawnAgent("SpiderNest", false, structureTile, agentRef);
		}
		
		agentRef.removeBehaviour(this);
	}
}
