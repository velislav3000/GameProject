package agents.behaviours;

import java.util.ArrayList;
import java.util.Random;

import agents.BaseAgent;
import agents.BaseEntityAgent;
import agents.BaseStructureAgent;
import agents.BaseEntityAgent.BehaviourType;
import agents.BaseEntityAgent.RelationshipType;
import agents.heroAgents.BaseHeroAgent;
import agents.monsterAgents.BaseMonsterAgent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import ontology.GameOntology;

public class IntentSenderMonsterBehaviour extends OneShotBehaviour{

	private transient BaseEntityAgent agentRef;
	private transient ArrayList<BaseAgent> otherAgents;
	
	public IntentSenderMonsterBehaviour(BaseEntityAgent agent, ArrayList<BaseAgent> perceivedAgents) {
		agentRef = agent;
		otherAgents = perceivedAgents;
	}

	@Override
	public void action() {
		//System.out.println(agentRef.getLocalName() + ": Sending Intent");
		for(BaseAgent agent: otherAgents) {
			ACLMessage message = new ACLMessage(ACLMessage.INFORM);
			message.addReceiver(agent.getAID());

			RelationshipType agentRelations = agentRef.getRelationshipWithAgent(agent);
			
			if(agentRelations == null) {
				if(agent.isStructureAgent()) {
					if(agent.isHeroStructureAgent()) {
						agentRelations = RelationshipType.ENEMIES;
					}
					else if(agent.equals(agentRef.getSpawnStructure())) {
						agentRelations = RelationshipType.ALLIES;	
					}
					else {
						agentRelations = RelationshipType.NEUTRAL;
					}
				}
				else {
					continue;
				}
			}
			
			//System.out.println(agentRef.getLocalName() + " is " + agentRelations + " with " + agent.getLocalName());
			
			switch (agentRelations) {
			case ALLIES:{
				message.setContent(BehaviourType.EXPLORATION.toString());
				break;
			}
			case FRIENDLY: {
				message.setContent(BehaviourType.EXPLORATION.toString());
				break;
			}
			case NEUTRAL: {
				Random random = new Random();
				int chance = random.nextInt(10);
				if(canReachAgent(agent) && chance == 0) {
					agent.setRelationshipWithAgent(agentRef.getLocalName(), agentRef.getDisplayName(), RelationshipType.ENEMIES);
					message.setContent(BehaviourType.COMBAT.toString());
					agentRef.setBehaviour(BehaviourType.COMBAT);
					if(!agentRef.getAgentsInCombatWith().contains(agent)) {
						agentRef.getAgentsInCombatWith().add(agent);
					}
				}
				else {
					message.setContent(BehaviourType.EXPLORATION.toString());
				}
				break;
			}
			case UNFRIENDLY: {
				if(canReachAgent(agent)) {
					agent.setRelationshipWithAgent(agentRef.getLocalName(), agentRef.getDisplayName(), RelationshipType.ENEMIES);
					message.setContent(BehaviourType.COMBAT.toString());
					agentRef.setBehaviour(BehaviourType.COMBAT);
					if(!agentRef.getAgentsInCombatWith().contains(agent)) {
						agentRef.getAgentsInCombatWith().add(agent);
					}
				}
				else {
					message.setContent(BehaviourType.EXPLORATION.toString());
				}
				break;
			}
			case ENEMIES: {
				if(canReachAgent(agent)) {
					message.setContent(BehaviourType.COMBAT.toString());
					if(agentRef.getBehaviour() != BehaviourType.FLEEING) {
						agentRef.setBehaviour(BehaviourType.COMBAT);
					}
					if(!agentRef.getAgentsInCombatWith().contains(agent)) {
						agentRef.getAgentsInCombatWith().add(agent);
					}
				}
				else {
					message.setContent(BehaviourType.EXPLORATION.toString());
				}
				break;
			}
			}

			agentRef.addToLog("Sending " + message.getContent() + " intent to " + agent.getDisplayName());
			message.setConversationId("sharingIntent");
			agentRef.send(message);
			//System.out.println(agentRef.getLocalName() + ": Intent Sent to " + agent.getLocalName());
		}
		agentRef.setAlreadyCommunicated(true);
		agentRef.update();
		agentRef.removeBehaviour(this);
	}
	
	private boolean canReachAgent(BaseAgent otherAgent) {
		return agentRef.doesPathToTileExist(agentRef.getCurrentTile(), otherAgent.getCurrentTile());
	}

}