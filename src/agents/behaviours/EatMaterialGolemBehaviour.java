package agents.behaviours;

import java.util.Random;

import agents.BaseEntityAgent.StatusEffect;
import agents.heroAgents.BaseHeroAgent;
import agents.monsterAgents.GolemAgent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;
import tiles.Tile;
import tiles.Tile.TileType;

public class EatMaterialGolemBehaviour extends OneShotBehaviour {

	private transient GolemAgent agentRef;
	private transient Tile moveToTile;
	
	public EatMaterialGolemBehaviour(GolemAgent agent) {
		agentRef = agent;
	}

	@Override
	public void action() {
		if(agentRef.getCurrentTile().equals(agentRef.getTileWithMaterial())) {
			agentRef.eatMaterial();
			agentRef.setTileWithMaterial(null);
		}
		else {
			moveToTile = agentRef.getPathToTile().pop();
			if(!moveToTile.canAgentMoveToTile(agentRef)) {
				agentRef.getPathToTile().add(moveToTile);
				return;
			}
			moveToTile.setLockedForMovementByAgent(agentRef);
			
			agentRef.addBehaviour(new WakerBehaviour(agentRef, 1){
				protected void handleElapsedTimeout() {
					if(agentRef.getCurrentTile().getTileType() == TileType.ICE) {
						Random random = new Random();
						int chance = random.nextInt(5);
						if(chance == 0) {
							if(agentRef.hasStatusEffect(StatusEffect.QUICK) && !agentRef.hasMovedTwice()) {
								agentRef.setMovedTwice(true);
								agentRef.perceiveSurroundings();
								agentRef.addBehaviour(new EatMaterialGolemBehaviour(agentRef));
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
					}
					else {
						agentRef.getPathToTile().add(moveToTile);
					}
					
					if(agentRef.hasStatusEffect(StatusEffect.QUICK) && !agentRef.hasMovedTwice()) {
						agentRef.setMovedTwice(true);
						agentRef.perceiveSurroundings();
						agentRef.addBehaviour(new EatMaterialGolemBehaviour(agentRef));
					}
					agentRef.removeBehaviour(this);
				}
			});
			
		}
		agentRef.removeBehaviour(this);
	}

}