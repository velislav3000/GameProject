package agents.behaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import agents.AgentUtils;
import agents.BaseAgent;
import agents.BaseEntityAgent;
import agents.BaseEntityAgent.BehaviourType;
import agents.BaseEntityAgent.RelationshipType;
import agents.heroAgents.BaseHeroAgent;
import agents.heroAgents.BaseHeroAgent.HeroPersonalityTrait;
import agents.heroAgents.BaseHeroAgent.HeroType;
import agents.items.equipment.Equipment;
import agents.items.equipment.Equipment.EquipmentType;
import agents.items.potions.BasePotion.PotionType;
import collectiables.materials.BaseMaterial.MaterialType;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import ontology.GameOntology;
import tiles.Tile;

public class MessageProcessingBehaviour extends CyclicBehaviour implements BehaviourLoader{

	private transient BaseEntityAgent agentRef;

	protected MessageTemplate mtUpdate = MessageTemplate.MatchConversationId("update");
	protected MessageTemplate mtSharingIntent = MessageTemplate.MatchConversationId("sharingIntent");
	protected MessageTemplate mtCallForHelp = MessageTemplate.MatchConversationId("requestingHelp");
	
	public MessageProcessingBehaviour(BaseEntityAgent agent) {
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
		
		msg = agentRef.receive(mtCallForHelp);
		if(msg != null) {
			respondToCallForHelp(msg);
		}
	}

	protected void evaluateIntent(ACLMessage msg) {
		BaseAgent senderAgent = AgentUtils.getAgentByLocalName(msg.getSender().getLocalName());
		
		if(senderAgent == null) {
			return;
		}
		
		//System.out.println(agentRef.getLocalName() + ": Intent received from " + senderAgent.getLocalName());
		
		BehaviourType agentIntent = BehaviourType.valueOf(msg.getContent());

		agentRef.addToLog("Accepted " + agentIntent + " intent from " + senderAgent.getDisplayName());
		
		switch (agentIntent) {
		case EXPLORATION: {
			if(agentRef.getAgentsInCombatWith().isEmpty()) {
				agentRef.setBehaviour(BehaviourType.EXPLORATION);
			}
			break;
		}
		case COMBAT: {
			
			if(!agentRef.getAgentsInCombatWith().contains(senderAgent)) {
				agentRef.getAgentsInCombatWith().add(senderAgent);
			}
			
			if(agentRef.getBehaviour() == BehaviourType.COMBAT) {
				if(shouldFlee() && canFlee()) {
					agentRef.setBehaviour(BehaviourType.FLEEING);
				}
				else {
					agentRef.setBehaviour(BehaviourType.COMBAT);
				}
			}
			else {
				if(agentRef.getBehaviour() == BehaviourType.FLEEING) {
					if(canFlee() && shouldFlee()) {
						agentRef.setBehaviour(BehaviourType.FLEEING);
					}
					else {
						agentRef.setBehaviour(BehaviourType.COMBAT);
					}
				}
				else {
					if(shouldEnterCombat(senderAgent) || !canFlee()) {
						agentRef.setBehaviour(BehaviourType.COMBAT);
					}
					else {
						agentRef.setBehaviour(BehaviourType.FLEEING);
					}
				}
			}
			break;
		}
		
		}
	}
	
	protected boolean shouldEnterCombat(BaseAgent combatAgent) {
		if(agentRef.isMonsterAgent()) {
			return true;
		}
		
		ArrayList<BaseAgent> attackingAgents = new ArrayList<>();
		
		for (BaseAgent agent : agentRef.getAgentsInCombatWith()) {
			if(agentRef.equals(agent.getCombatTargetAgent())){
				attackingAgents.add(agent);
			}
			else if(agent.isStructureAgent() && agent.getAgentsInCombatWith().contains(agentRef)) {
				attackingAgents.add(agent);
			}
		}
		
		if(!attackingAgents.contains(combatAgent)) {
			attackingAgents.add(combatAgent);
		}
		
		int agentEquipmentBonuses = GameOntology.getTotalEquipmentBonuses(agentRef.getLocalName());
		
		int enemyEquipmentBonuses = 0;
		for (BaseAgent agent : attackingAgents) {
			if(agent.isHeroAgent()) {
				enemyEquipmentBonuses += GameOntology.getTotalEquipmentBonuses(agent.getLocalName());
			}
			else {
				enemyEquipmentBonuses += agent.getAttack() - agentRef.getBaseAttack();
			}
		}
		
		//System.out.println(agentRef.getLocalName() + ":" + agentEquipmentBonuses + " - " + enemyEquipmentBonuses);
		
		return agentEquipmentBonuses >= enemyEquipmentBonuses;
	}
	
	protected boolean shouldFlee() {
		if(agentRef.getCurrentHealth() > agentRef.getMaxHealth()*0.2) {
			return false;
		}
		
		ArrayList<BaseAgent> attackingAgents = new ArrayList<>();
		
		for (BaseAgent agent : agentRef.getAgentsInCombatWith()) {
			if(agentRef.equals(agent.getCombatTargetAgent())){
				attackingAgents.add(agent);
			}
		}
		
		if(attackingAgents.isEmpty()) {
			return false;
		}
		
		int agentHealth = agentRef.getCurrentHealth();
		int agentAttack = agentRef.getAttack();
		
		int enemyTotalHealth = 0;
		int enemyTotalAttack = 0;
		
		for (BaseAgent agent : attackingAgents) {
			enemyTotalHealth += agent.getCurrentHealth();
			enemyTotalAttack += agent.getAttack();
		}
		
		int turnsToKillEnemies = (int) Math.ceil(enemyTotalHealth/(agentAttack * 1.0));
		int turnsEnemiesToKillAgent = (int) Math.ceil(agentHealth/(enemyTotalAttack * 1.0));
		
		//System.out.println(agentRef.getLocalName() + ": " + turnsToKillEnemies + " - " + turnsEnemiesToKillAgent + " = " + (turnsEnemiesToKillAgent < turnsToKillEnemies));
		//System.out.println(agentRef.getCurrentHealth());
		
		return turnsEnemiesToKillAgent < turnsToKillEnemies;
	}
	
	protected boolean canFlee() {
		for(Tile tile: agentRef.getMovableToTiles()) {
			int attackersNextToTile = 0;
			for (BaseAgent agent : agentRef.getAgentsInCombatWith()) {
				if(agentRef.equals(agent.getCombatTargetAgent())){
					if(agent.getCurrentTile().getNeighbours().contains(tile)) {
						attackersNextToTile++;	
					}
				}
			}
			
			if(attackersNextToTile == 0) {
				return true;
			}
		}
		return false;
	}
	
	protected void respondToCallForHelp(ACLMessage msg) {
		if(agentRef.getBehaviour() == BehaviourType.COMBAT || agentRef.getBehaviour() == BehaviourType.FLEEING || 
				agentRef.getBehaviour() == BehaviourType.TRADING || agentRef.getBehaviour() == BehaviourType.TRADING_IDLE) {
			
			return;
		}
		
		BaseAgent targetAgent = AgentUtils.getAgentByLocalName(msg.getContent());
		
		if(shouldEnterCombat(targetAgent)) {
			agentRef.getAgentsInCombatWith().add(targetAgent);
			agentRef.setBehaviour(BehaviourType.COMBAT);
		}
	}

	@Override
	public void loadOwner(BaseAgent agent) {
		agentRef = (BaseEntityAgent) agent;
	}
}
