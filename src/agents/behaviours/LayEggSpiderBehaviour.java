package agents.behaviours;

import java.util.ArrayList;
import java.util.Random;

import agents.AgentUtils;
import agents.monsterAgents.SpiderAgent;
import jade.core.behaviours.OneShotBehaviour;
import tiles.Tile;
import tiles.Tile.TileType;

public class LayEggSpiderBehaviour extends OneShotBehaviour{

	private transient SpiderAgent agentRef;
	
	public LayEggSpiderBehaviour(SpiderAgent agent) {
		agentRef = agent;
	}
	
	@Override
	public void action() {
		ArrayList<Tile> possibleSpawnLocations = new ArrayList<>();
		
		for (Tile tile : agentRef.getCurrentTile().getNeighbours()) {
			if(tile.getTileType() != TileType.MOUNTAIN && tile.getTileType() != TileType.WATER && tile.getAgent() == null) {
				possibleSpawnLocations.add(tile);
			}
		}
		
		if(possibleSpawnLocations.size() <= 0) {
			agentRef.removeBehaviour(this);
			return;
		}
		
		Random random = new Random();
		int chance = random.nextInt(possibleSpawnLocations.size());
		
		Tile spawnTile = possibleSpawnLocations.get(chance);
		
		agentRef.addToLog("I spawned SpiderEgg" + AgentUtils.agentCounter);
		
		AgentUtils.spawnAgent("SpiderEgg", false, spawnTile, agentRef);
		
		agentRef.setLayEggCooldown(10);

		agentRef.removeBehaviour(this);
	}

}
