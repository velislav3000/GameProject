package agents.behaviours;

import java.util.Random;

import agents.AgentUtils;
import agents.BaseAgent;
import agents.BaseEntityAgent;
import agents.BaseEntityAgent.StatusEffect;
import jade.core.behaviours.WakerBehaviour;
import tiles.Tile;
import tiles.Tile.TileType;

public class CombatSkeletonBossBehaviour extends CombatBehaviour{

	public CombatSkeletonBossBehaviour(BaseEntityAgent agent, BaseAgent combatTargetAgent) {
		super(agent, combatTargetAgent);
	}

	@Override
	public void action() {
		if(agentRef.getCallAlliesCooldown() == 0) {
			callAlliesForHelp();
		}
		
		Tile enemyTile = enemyAgentRef.getCurrentTile();
		
		if(enemyTile.getNeighbours().contains(agentRef.getCurrentTile())) {	
			System.out.println(agentRef.getLocalName() + " attacks " + enemyAgentRef.getLocalName());
			
			enemyAgentRef.takeDamage(agentRef.getAttack(), agentRef);
			
			if(!enemyAgentRef.isAlive() && !enemyAgentRef.getLocalName().contains("Skeleton")) {
				AgentUtils.spawnAgent("Skeleton", false, enemyTile, agentRef);
				agentRef.addToLog("I spawned Skeleton" + AgentUtils.agentCounter);
			}
		}
		else {
			if(agentRef.getPathToTile().empty()) {
				return;
			}
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
								agentRef.addBehaviour(new CombatSkeletonBossBehaviour(agentRef, enemyAgentRef));
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
						agentRef.addBehaviour(new CombatSkeletonBossBehaviour(agentRef, enemyAgentRef));
					}
					agentRef.removeBehaviour(this);
				}
			});
		}
		agentRef.removeBehaviour(this);
	}
}
