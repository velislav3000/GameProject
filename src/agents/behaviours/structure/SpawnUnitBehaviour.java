package agents.behaviours.structure;

import java.util.ArrayList;
import java.util.Random;

import agents.AgentUtils;
import agents.BaseStructureAgent;
import jade.core.behaviours.OneShotBehaviour;
import tiles.Tile;
import tiles.Tile.TileType;

public class SpawnUnitBehaviour extends OneShotBehaviour{

	private transient BaseStructureAgent agentRef;
	
	public SpawnUnitBehaviour(BaseStructureAgent agent) {
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
		
		String spawnedUnitClass = agentRef.getSpawnedUnitClassName();
		boolean isHeroAgent = agentRef.isHeroStructureAgent();
		
		agentRef.addToLog("I spawned " + spawnedUnitClass + AgentUtils.agentCounter);
		
		AgentUtils.spawnAgent(spawnedUnitClass, isHeroAgent, spawnTile, agentRef);
		
		agentRef.setAgentSpawningCooldown();
		agentRef.gainXP(1);
		
		agentRef.removeBehaviour(this);
	}

}
