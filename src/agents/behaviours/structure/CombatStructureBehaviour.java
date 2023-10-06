package agents.behaviours.structure;

import java.util.ArrayList;
import java.util.Random;

import agents.BaseAgent;
import agents.BaseEntityAgent;
import agents.BaseStructureAgent;
import agents.BaseEntityAgent.RelationshipType;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import ontology.GameOntology;
import tiles.Tile;

public class CombatStructureBehaviour extends OneShotBehaviour{

	private transient BaseStructureAgent agentRef;
	
	public CombatStructureBehaviour(BaseStructureAgent agent) {
		agentRef = agent;
	}
	
	@Override
	public void action() {
		
		if(agentRef.getCallAlliesCooldown() == 0) {
			callAlliesForHelp();
		}
		
		for (BaseAgent agent : agentRef.getAgentsInCombatWith()) {
			agent.takeDamage(agentRef.getAttack(), agentRef);
		}
		agentRef.removeBehaviour(this);
	}

	private transient ArrayList<BaseEntityAgent> allies;
	private transient ArrayList<Tile> checkedTilesForAgents;
	
	private void callAlliesForHelp() {
		if(agentRef.getOwner() == null) {
			return;
		}
		
		allies = new ArrayList<>();
		checkedTilesForAgents = new ArrayList<>();
		getNearbyAllies(agentRef.getCurrentTile(), 8);
		
		Random random = new Random();
		
		for (BaseEntityAgent agent : allies) {
			ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
			msg.addReceiver(agent.getAID());
			msg.setConversationId("requestingHelp");
			
			int index = random.nextInt(agentRef.getAgentsInCombatWith().size());
			
			msg.setContent(agentRef.getAgentsInCombatWith().get(index).getLocalName());
			
			agentRef.send(msg);
		}
		
		agentRef.setCallAlliesCooldown(5);
	}
	
	private void getNearbyAllies(Tile tile, int tileLevel) {
		if(checkedTilesForAgents.contains(tile) || tileLevel == 0) {
			return;
		}
		
		checkedTilesForAgents.add(tile);
		
		if(tile.getAgent() != null && tile.getAgent().isEntityAgent()) {
			
			if(agentRef.getOwner().equals(tile.getAgent())) {
				allies.add((BaseEntityAgent) tile.getAgent());
			}
			else {
				RelationshipType relation = GameOntology.getRelationshipWithAgent(agentRef.getOwner().getLocalName(), tile.getAgent().getLocalName());
				
				if(relation == RelationshipType.ALLIES ) {
					allies.add((BaseEntityAgent) tile.getAgent());
				}
			}
		}
		
		for (Tile tile2 : tile.getNeighbours()) {
			getNearbyAllies(tile2, tileLevel - 1);
		}
	}
	
}
