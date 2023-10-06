package agents.behaviours.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import agents.AgentUtils;
import agents.BaseAgent;
import agents.BaseEntityAgent;
import agents.BaseStructureAgent;
import agents.BaseStructureAgent.StructureBehaviourType;
import agents.behaviours.BehaviourLoader;
import agents.BaseEntityAgent.BehaviourType;
import agents.heroAgents.BaseHeroAgent;
import agents.heroAgents.CityAgent;
import agents.heroAgents.BaseHeroAgent.HeroPersonalityTrait;
import agents.heroAgents.BaseHeroAgent.HeroType;
import agents.items.equipment.Equipment;
import agents.items.equipment.Equipment.EquipmentType;
import agents.items.potions.BasePotion;
import agents.items.potions.BasePotion.PotionType;
import collectiables.materials.BaseMaterial.MaterialType;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import ontology.GameOntology;
import tiles.Tile;

public class MessageProcessingStructureBehaviour extends CyclicBehaviour implements BehaviourLoader{

	private transient BaseStructureAgent agentRef;
	protected MessageTemplate mtUpdate = MessageTemplate.MatchConversationId("update");
	protected MessageTemplate mtSharingIntent = MessageTemplate.MatchConversationId("sharingIntent");

	public MessageProcessingStructureBehaviour(BaseStructureAgent agent) {
		agentRef = agent;
	}
		
	@Override
	public void action() {
		if(!agentRef.isAlive()) {
			agentRef.removeBehaviour(this);
			return;
		}
		
		if(agentRef.getPaused()) {
			return;
		}
		
		ACLMessage msg = agentRef.receive(mtUpdate);
		if(msg != null) {
			agentRef.update();
		}
		
		msg = agentRef.receive(mtSharingIntent);
		if(msg != null) {
			evaluateIntent(msg);
		}
		
	}

	protected void evaluateIntent(ACLMessage msg) {
		BaseAgent senderAgent = AgentUtils.getAgentByLocalName(msg.getSender().getLocalName());
		
		System.out.println(agentRef.getLocalName() + ": Intent received from " + senderAgent.getLocalName());
		
		BehaviourType agentIntent = BehaviourType.valueOf(msg.getContent());

		agentRef.addToLog("Accepted " + agentIntent + " intent from " + senderAgent.getDisplayName());
		
		switch (agentIntent) {
		case EXPLORATION: {
			if(agentRef.getAgentsInCombatWith().isEmpty()) {
				agentRef.setBehaviour(StructureBehaviourType.IDLE);
			}
			break;
		}
		case COMBAT: {
			
			if(!agentRef.getAgentsInCombatWith().contains(senderAgent)) {
				agentRef.getAgentsInCombatWith().add(senderAgent);
			}
		}
		}
	}

	@Override
	public void loadOwner(BaseAgent agent) {
		agentRef = (BaseStructureAgent) agent;
	}
}
