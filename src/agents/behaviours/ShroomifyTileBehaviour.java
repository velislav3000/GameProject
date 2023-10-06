package agents.behaviours;

import java.util.ArrayList;
import java.util.Random;

import agents.monsterAgents.ShroomerAgent;
import jade.core.behaviours.OneShotBehaviour;
import tiles.Tile;
import tiles.Tile.TileType;

public class ShroomifyTileBehaviour extends OneShotBehaviour{

	private ShroomerAgent agentRef;
	
	public ShroomifyTileBehaviour(ShroomerAgent agent) {
		agentRef = agent;
	}
	
	@Override
	public void action() {
		
		Tile currentTileRef = agentRef.getCurrentTile();
		
		if(currentTileRef.getTileType() != TileType.MUSHROOM) {
			currentTileRef.setTileType(TileType.MUSHROOM);
		}
		
		if(agentRef.isBossMonster()) {
			ArrayList<Tile> potentialTilesToShroomify = currentTileRef.getNeighbours();
			
			for(int i = potentialTilesToShroomify.size()-1 ; i >= 0; i--) {
				Tile tile = potentialTilesToShroomify.get(i);
				
				if(tile.getTileType() == TileType.WATER || tile.getTileType() == TileType.MOUNTAIN || tile.getTileType() == TileType.MUSHROOM) {
					potentialTilesToShroomify.remove(tile);
				}
			}
			
			if(potentialTilesToShroomify.size() > 0) {
				Random random = new Random();
				int index = random.nextInt(potentialTilesToShroomify.size());
				Tile tile = potentialTilesToShroomify.get(index);
				tile.setTileType(TileType.MUSHROOM);
				potentialTilesToShroomify.remove(tile);
				if(potentialTilesToShroomify.size() > 0) {
					index = random.nextInt(potentialTilesToShroomify.size());
					tile = potentialTilesToShroomify.get(index);
					tile.setTileType(TileType.MUSHROOM);
				}
			}
		}
		
		if(agentRef.isBossMonster()) {
			agentRef.setShroomifyCooldown(7);
		}
		else {
			agentRef.setShroomifyCooldown(10);
		}
		agentRef.removeBehaviour(this);
	}

}
