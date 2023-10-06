package agents.behaviours;

import java.util.ArrayList;
import java.util.Random;

import agents.BaseAgent;
import agents.BaseEntityAgent;
import agents.BaseEntityAgent.BehaviourType;
import agents.BaseEntityAgent.RelationshipType;
import agents.BaseStructureAgent;
import agents.heroAgents.ArcherAgent;
import agents.heroAgents.BaseHeroAgent;
import agents.heroAgents.BaseHeroAgent.HeroPersonalityTrait;
import agents.items.BaseItem.ItemQuality;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import ontology.GameOntology;
import tiles.Tile;

public class IntentSenderHeroBehaviour extends OneShotBehaviour{

	private transient BaseHeroAgent agentRef;
	private transient ArrayList<BaseAgent> otherAgents;
	
	public IntentSenderHeroBehaviour(BaseHeroAgent agent, ArrayList<BaseAgent> perceivedAgents) {
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
						agentRelations = RelationshipType.NEUTRAL;
					}
					else {
						agentRelations = RelationshipType.ENEMIES;
					}
				}
				else {
					continue;
				}
			}
			
			switch (agentRelations) {
			case ALLIES:{
				if(agentRef.isTradingEnabled() && agentRef.getBehaviour() != BehaviourType.TRADING && agentRef.getBehaviour() != BehaviourType.TRADING_IDLE) {
					message.setContent(BehaviourType.TRADING.toString());
				}
				else {
					message.setContent(BehaviourType.EXPLORATION.toString());
				}
				break;
			}
			case FRIENDLY: {
				Random random = new Random();
				int chance = random.nextInt(4);
				if(chance == 0 && agentRef.isTradingEnabled() && agentRef.getBehaviour() != BehaviourType.TRADING && agentRef.getBehaviour() != BehaviourType.TRADING_IDLE) {
					message.setContent(BehaviourType.TRADING.toString());
				}
				else {
					message.setContent(BehaviourType.EXPLORATION.toString());
				}
				break;
			}
			case NEUTRAL: {
				if(canReachAgent(agent) && shouldAttackAgent(agent, agentRelations)) {
					if(agent.isEntityAgent()) {
						agent.setRelationshipWithAgent(agentRef.getLocalName(), agentRef.getDisplayName(), RelationshipType.ENEMIES);
					}
					message.setContent(BehaviourType.COMBAT.toString());
					agentRef.setBehaviour(BehaviourType.COMBAT);
					if(!agentRef.getAgentsInCombatWith().contains(agent)) {
						agentRef.getAgentsInCombatWith().add(agent);
					}
				}
				else {
					Random random = new Random();
					int chance = 1;
					if(agent.isHeroStructureAgent()) {
						chance = random.nextInt(4);
					}
					else {
						chance = random.nextInt(10);
					}
					if(chance == 0 && agentRef.isTradingEnabled() && agentRef.getBehaviour() != BehaviourType.TRADING && agentRef.getBehaviour() != BehaviourType.TRADING_IDLE) {
						message.setContent(BehaviourType.TRADING.toString());
					}
					else {
						message.setContent(BehaviourType.EXPLORATION.toString());
					}
				}
				break;
			}
			case UNFRIENDLY: {
				if(canReachAgent(agent) && shouldAttackAgent(agent, agentRelations)) {
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
	
	private boolean shouldAttackAgent(BaseAgent agent, RelationshipType relation) {
		if(agent.isEntityAgent()) {
			ArrayList<Integer> equipmentBonuses = GameOntology.getTotalEquipmentBonuses(agentRef.getLocalName(), agent.getLocalName());
			
			int agentEquipmentBonuses = equipmentBonuses.get(0);
			
			int enemyEquipmentBonuses = equipmentBonuses.get(1);
			
			//System.out.println(agentRef.getLocalName() + ":" + agentEquipmentBonuses + " - " + enemyEquipmentBonuses);
			
			switch (relation) {
			case ENEMIES: {
				return agentEquipmentBonuses >= enemyEquipmentBonuses;
			}
			case UNFRIENDLY: {
				if(agentRef.getPersonalityTrait() == HeroPersonalityTrait.BLOODTHIRSTY){
					return agentEquipmentBonuses >= enemyEquipmentBonuses;
				}
				else if(agentRef.getPersonalityTrait() == HeroPersonalityTrait.COWARD){
					return agentEquipmentBonuses >= enemyEquipmentBonuses + 40;
				}
				return agentEquipmentBonuses >= enemyEquipmentBonuses + 20;
			}
			case NEUTRAL: {			
				if(agentRef.getPersonalityTrait() == HeroPersonalityTrait.BLOODTHIRSTY){
					Random random = new Random();
					int chance = random.nextInt(4);
					
					if(chance == 0) {
						return agentEquipmentBonuses >= enemyEquipmentBonuses;
					}
					else {
						return agentEquipmentBonuses >= enemyEquipmentBonuses + 20;
					}
				}
				else if(agentRef.getPersonalityTrait() == HeroPersonalityTrait.COWARD){
					return agentEquipmentBonuses >= enemyEquipmentBonuses + 100;
				}
				return agentEquipmentBonuses >= enemyEquipmentBonuses + 50;
			}
			default:
				return false;
			}
		}
		else {
			int agentDeadInNumTurns = agentRef.getCurrentHealth() / agent.getAttack();
			int enemyDeadInNumTurns = agent.getCurrentHealth() / agentRef.getAttack();
			
			switch (relation) {
			case ENEMIES: {
				return agentDeadInNumTurns > enemyDeadInNumTurns;
			}
			case UNFRIENDLY: {
				if(agentRef.getPersonalityTrait() == HeroPersonalityTrait.BLOODTHIRSTY){
					return agentDeadInNumTurns > enemyDeadInNumTurns;
				}
				else if(agentRef.getPersonalityTrait() == HeroPersonalityTrait.COWARD){
					return agentDeadInNumTurns > enemyDeadInNumTurns + 10;
				}
				return agentDeadInNumTurns > enemyDeadInNumTurns + 5;
			}
			case NEUTRAL: {			
				if(agentRef.getPersonalityTrait() == HeroPersonalityTrait.BLOODTHIRSTY){
					Random random = new Random();
					int chance = random.nextInt(4);
					
					if(chance == 0) {
						return agentDeadInNumTurns > enemyDeadInNumTurns;
					}
					else {
						return agentDeadInNumTurns> enemyDeadInNumTurns + 5 ;
					}
				}
				else if(agentRef.getPersonalityTrait() == HeroPersonalityTrait.COWARD){
					return agentDeadInNumTurns > enemyDeadInNumTurns + 15;
				}
				return agentDeadInNumTurns > enemyDeadInNumTurns + 10;
			}
			default:
				return false;
			}
		}
	}
	
	private boolean canReachAgent(BaseAgent otherAgent) {
		if(agentRef instanceof ArcherAgent) {
			boolean canReachAgent = agentRef.doesPathToTileExist(agentRef.getCurrentTile(), otherAgent.getCurrentTile());
			if(canReachAgent) {
				return true;
			}
			else {
				ArrayList<Tile> checkedTiles = new ArrayList<>();
				for (Tile tile : otherAgent.getCurrentTile().getNeighbours()) {
					for (Tile neighbourTile : tile.getNeighbours()) {
						
						if(checkedTiles.contains(neighbourTile)) {
							continue;
						}
						checkedTiles.add(neighbourTile);
						
						if(neighbourTile.canAgentMoveToTile(agentRef)) {
							if(neighbourTile.equals(agentRef.getCurrentTile())) {
								return true;
							}
							canReachAgent = agentRef.doesPathToTileExist(agentRef.getCurrentTile(), neighbourTile);
							if(canReachAgent) {
								return true;
							}
						}
					}
				}
				return false;
			}
		}
		else {
			return agentRef.doesPathToTileExist(agentRef.getCurrentTile(), otherAgent.getCurrentTile());
		}
	}

}
