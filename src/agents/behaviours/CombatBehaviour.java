package agents.behaviours;

import java.util.ArrayList;
import java.util.Random;

import agents.BaseAgent;
import agents.BaseEntityAgent;
import agents.BaseEntityAgent.RelationshipType;
import agents.BaseEntityAgent.StatusEffect;
import agents.heroAgents.BaseHeroAgent;
import agents.heroAgents.BaseHeroAgent.HeroAbilityTrait;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import ontology.GameOntology;
import tiles.Tile;
import tiles.Tile.TileType;

public class CombatBehaviour extends OneShotBehaviour{
	protected transient BaseEntityAgent agentRef;
	protected transient BaseAgent enemyAgentRef;
	protected transient Tile moveToTile;
	
	public CombatBehaviour(BaseEntityAgent agent , BaseAgent combatTargetAgent) {
		agentRef = agent;
		enemyAgentRef = combatTargetAgent;
	}
	
	@Override
	public void action() {
		if(agentRef.getCallAlliesCooldown() == 0) {
			callAlliesForHelp();
		}
		
		if(enemyAgentRef.getCurrentTile().getNeighbours().contains(agentRef.getCurrentTile())) {	
			System.out.println(agentRef.getLocalName() + " attacks " + enemyAgentRef.getLocalName());
			enemyAgentRef.takeDamage(agentRef.getAttack(), agentRef);
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
					if(agentRef.getCurrentTile().getTileType() == TileType.ICE && 
							!(agentRef instanceof BaseHeroAgent && ((BaseHeroAgent) agentRef).hasAbilityTrait(HeroAbilityTrait.BURNING_BLOOD))) {
						Random random = new Random();
						int chance = random.nextInt(5);
						if(chance == 0) {
							if(agentRef.hasStatusEffect(StatusEffect.QUICK) && !agentRef.hasMovedTwice()) {
								agentRef.setMovedTwice(true);
								agentRef.perceiveSurroundings();
								agentRef.addBehaviour(new CombatBehaviour(agentRef, enemyAgentRef));
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
						agentRef.addBehaviour(new CombatBehaviour(agentRef, enemyAgentRef));
					}
					agentRef.removeBehaviour(this);
				}
			});
		}
		agentRef.removeBehaviour(this);
	}
	
	private transient ArrayList<BaseEntityAgent> allies;
	private transient ArrayList<Tile> checkedTilesForAgents;
	
	protected void callAlliesForHelp() {
		allies = new ArrayList<>();
		checkedTilesForAgents = new ArrayList<>();
		getNearbyAllies(agentRef.getCurrentTile(), 8);
		
		for (BaseEntityAgent agent : allies) {
			if(agent == null) {
				continue;
			}
			
			ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
			msg.addReceiver(agent.getAID());
			msg.setConversationId("requestingHelp");
			
			msg.setContent(agentRef.getCombatTargetAgent().getLocalName());
			
			agentRef.send(msg);
		}
		
		agentRef.setCallAlliesCooldown(5);
	}
	
	private void getNearbyAllies(Tile tile, int tileLevel) {
		if(checkedTilesForAgents.contains(tile) || tileLevel == 0) {
			return;
		}
		
		checkedTilesForAgents.add(tile);
		
		if(tile.getAgent() != null && tile.getAgent() != agentRef && tile.getAgent().isEntityAgent()) {
			RelationshipType relation = GameOntology.getRelationshipWithAgent(agentRef.getLocalName(), tile.getAgent().getLocalName());
			
			if(relation == RelationshipType.ALLIES ) {
				allies.add((BaseEntityAgent) tile.getAgent());
			}
		}
		
		for (Tile tile2 : tile.getNeighbours()) {
			getNearbyAllies(tile2, tileLevel - 1);
		}
	}
}
