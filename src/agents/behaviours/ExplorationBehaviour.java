package agents.behaviours;

import java.util.ArrayList;
import java.util.Random;

import agents.BaseEntityAgent;
import agents.BaseEntityAgent.StatusEffect;
import agents.heroAgents.BaseHeroAgent;
import agents.heroAgents.BaseHeroAgent.HeroAbilityTrait;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;
import tiles.Tile;
import tiles.Tile.TileType;

public class ExplorationBehaviour extends OneShotBehaviour{

	private transient BaseEntityAgent agentRef;
	private int moveAttemptCounter = 0;
	private transient Tile moveToTile;
	
	public ExplorationBehaviour(BaseEntityAgent agent) {
		agentRef = agent;
	}
	
	@Override
	public void action() {
		ArrayList<Tile> possibleToMoveToTiles = agentRef.getMovableToTiles();
		
		if(agentRef.getOldTile() != null) {
			for (Tile tile : agentRef.getCurrentTile().getNeighboursOppositeOfTile(agentRef.getOldTile())) {
				if(possibleToMoveToTiles.contains(tile)) {
					possibleToMoveToTiles.add(tile);
					possibleToMoveToTiles.add(tile);
					if(tile.getTileType() != TileType.BURNING_SAND) {
						possibleToMoveToTiles.add(tile);
					}
				}
			}
				
			
			Tile oppositeOfOldTile = agentRef.getCurrentTile().getDirectOppositeNeighbourOfTile(agentRef.getOldTile());
			if(oppositeOfOldTile != null && possibleToMoveToTiles.contains(oppositeOfOldTile)
					&& oppositeOfOldTile.getTileType() != TileType.BURNING_SAND) {
				possibleToMoveToTiles.add(oppositeOfOldTile);
				possibleToMoveToTiles.add(oppositeOfOldTile);
			}
			
			if(agentRef.hasMovedTwice()) {
				possibleToMoveToTiles.remove(agentRef.getOldTile());
			}
		}
		
		Random random = new Random();
		int randomTileIndex = random.nextInt(possibleToMoveToTiles.size());
		
		if(!possibleToMoveToTiles.get(randomTileIndex).canAgentMoveToTile(agentRef)) {
			if(moveAttemptCounter > 10) {
				return;
			}
			moveAttemptCounter++;
			action();
			return;
		}
		
		moveToTile = possibleToMoveToTiles.get(randomTileIndex);
		moveToTile.setLockedForMovementByAgent(agentRef);
		
		agentRef.addBehaviour(new WakerBehaviour(agentRef, 1){
			protected void handleElapsedTimeout() {
				if(agentRef.getCurrentTile().getTileType() == TileType.ICE && 
						!(agentRef instanceof BaseHeroAgent && ((BaseHeroAgent) agentRef).hasAbilityTrait(HeroAbilityTrait.BURNING_BLOOD))) {
					Random random = new Random();
					int chance = random.nextInt(5);
					if(chance == 0) {
						if(agentRef.hasStatusEffect(StatusEffect.QUICK) && !agentRef.hasMovedTwice()) {
							agentRef.setMovedTwice(true);
							agentRef.perceiveSurroundings();
							agentRef.addBehaviour(new ExplorationBehaviour(agentRef));
						}
						agentRef.removeBehaviour(this);
						return;
					}
				}
				
				if(agentRef.equals(moveToTile.getLockedForMovementByAgent())){
					agentRef.setOldTile(agentRef.getCurrentTile());
					agentRef.getOldTile().setAgent(null);
					agentRef.setCurrentTile(moveToTile);
					agentRef.getCurrentTile().setAgent(agentRef);
					moveToTile.setLockedForMovementByAgent(null);
					
					if(moveToTile.getTileType() == TileType.BURNING_SAND) {
						int heatDamage = random.nextInt(6)+5;
						agentRef.takeHeatDamage(heatDamage);
					}
				}
				else {
					action();
					agentRef.removeBehaviour(this);
					return;
				}
				
				if(agentRef.hasStatusEffect(StatusEffect.QUICK) && !agentRef.hasMovedTwice()) {
					agentRef.setMovedTwice(true);
					agentRef.perceiveSurroundings();
					agentRef.addBehaviour(new ExplorationBehaviour(agentRef));
				}
				agentRef.removeBehaviour(this);
			}
		});
		
		agentRef.removeBehaviour(this);
		
	}

}
