package agents.behaviours;

import java.util.ArrayList;
import java.util.Random;

import agents.BaseAgent;
import agents.BaseEntityAgent;
import agents.BaseEntityAgent.StatusEffect;
import agents.heroAgents.RogueAgent;
import agents.heroAgents.BaseHeroAgent;
import agents.heroAgents.BaseHeroAgent.HeroAbilityTrait;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;
import tiles.Tile;
import tiles.Tile.TileType;

public class CombatArcherBehaviour extends OneShotBehaviour{
	private transient BaseEntityAgent agentRef;
	private transient BaseAgent enemyAgentRef;
	private Tile moveToTile;
	private int moveAttemptCounter = 0;
	private boolean retreated = false;
	
	public CombatArcherBehaviour(BaseEntityAgent agent , BaseAgent combatTargetAgent) {
		agentRef = agent;
		enemyAgentRef = combatTargetAgent;
	}
	
	@Override
	public void action() {
		
		if(enemyAgentRef.getCurrentTile().getNeighbours().contains(agentRef.getCurrentTile())) {
			ArrayList<Tile> tilesToRetreatTo = new ArrayList<>();
			
			for (Tile tile : agentRef.getMovableToTiles()) {
				
				if(!enemyAgentRef.getCurrentTile().getNeighbours().contains(tile) && !tile.equals(enemyAgentRef.getCurrentTile())) {
					tilesToRetreatTo.add(tile);
				}	
			}
			
			if(tilesToRetreatTo.isEmpty()) {
				doAMeleeAttack();
				agentRef.removeBehaviour(this);
				return;
			}
			
			Random random = new Random();
			int moveTileIndex = random.nextInt(tilesToRetreatTo.size());
			
			moveToTile = tilesToRetreatTo.get(moveTileIndex);
			
			if(!moveToTile.canAgentMoveToTile(agentRef)) {
				if(moveAttemptCounter > 10) {
					return;
				}
				moveAttemptCounter++;
				action();
				return;
			}
			moveToTile.setLockedForMovementByAgent(agentRef);
			
			agentRef.addBehaviour(new WakerBehaviour(agentRef, 1){
				protected void handleElapsedTimeout() {
					if(agentRef.getCurrentTile().getTileType() == TileType.ICE && !((BaseHeroAgent) agentRef).hasAbilityTrait(HeroAbilityTrait.BURNING_BLOOD)) {
						Random random = new Random();
						int chance = random.nextInt(5);
						if(chance == 0) {
							retreated = true;
							agentRef.perceiveSurroundings();
							attemptToAttack();
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
						retreated = true;
						agentRef.perceiveSurroundings();
						attemptToAttack();
					}
					else {
						agentRef.addBehaviour(new CombatArcherBehaviour(agentRef, enemyAgentRef));
						agentRef.removeBehaviour(this);
						return;
					}
					agentRef.removeBehaviour(this);
				}
			});
		}
		else {
			attemptToAttack();
		}
		
		agentRef.removeBehaviour(this);
	}
	
	private void attemptToAttack() {
		
		if(agentRef.getPerceivedTiles().contains(enemyAgentRef.getCurrentTile())) {
			System.out.println(agentRef.getLocalName() + " attacks " + enemyAgentRef.getLocalName());
			enemyAgentRef.takeDamage(agentRef.getAttack(), agentRef);
		}
		else if(!retreated){
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
					if(agentRef.getCurrentTile().getTileType() == TileType.ICE && !((BaseHeroAgent) agentRef).hasAbilityTrait(HeroAbilityTrait.BURNING_BLOOD)) {
						Random random = new Random();
						int chance = random.nextInt(5);
						if(chance == 0) {
							if(agentRef.hasStatusEffect(StatusEffect.QUICK) && !agentRef.hasMovedTwice()) {
								agentRef.setMovedTwice(true);
								agentRef.perceiveSurroundings();
								agentRef.addBehaviour(new CombatArcherBehaviour(agentRef, enemyAgentRef));
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
						agentRef.addBehaviour(new CombatArcherBehaviour(agentRef, enemyAgentRef));
					}
					agentRef.removeBehaviour(this);
				}
			});
		}
	}
	
	private void doAMeleeAttack() {
		System.out.println(agentRef.getLocalName() + " attacks " + enemyAgentRef.getLocalName());
		enemyAgentRef.takeDamage(Math.round(agentRef.getAttack() * 0.33f), agentRef);
	}
}