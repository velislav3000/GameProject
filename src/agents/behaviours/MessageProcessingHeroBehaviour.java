package agents.behaviours;

import java.util.ArrayList;
import java.util.Random;

import agents.AgentUtils;
import agents.BaseAgent;
import agents.BaseEntityAgent.BehaviourType;
import agents.heroAgents.BaseHeroAgent;
import agents.heroAgents.BaseHeroAgent.HeroPersonalityTrait;
import agents.heroAgents.BaseHeroAgent.HeroType;
import agents.items.equipment.Equipment;
import agents.items.equipment.Equipment.EquipmentType;
import agents.items.potions.BasePotion.PotionType;
import collectiables.materials.BaseMaterial.MaterialType;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class MessageProcessingHeroBehaviour extends MessageProcessingBehaviour{

	private transient BaseHeroAgent heroAgentRef;
	
	private MessageTemplate mtAcceptTrade = MessageTemplate.MatchConversationId("acceptTrade");
	private MessageTemplate mtTrade = MessageTemplate.and(MessageTemplate.MatchConversationId("trading"), MessageTemplate.MatchReplyWith("seller"));
	
	//For Trading
	private Equipment equipmentForSale;
	private int equipmentPrice;
	
	public MessageProcessingHeroBehaviour(BaseHeroAgent agent) {
		super(agent);
		heroAgentRef = agent;
	}

	@Override
	public void action() {
		if(!heroAgentRef.isAlive()) {
			heroAgentRef.removeBehaviour(this);
			return;
		}
		
		if(heroAgentRef.getPaused()) {
			return;
		}
		
		ACLMessage msg = heroAgentRef.receive(mtUpdate);
		if(msg != null) {
			heroAgentRef.update();
		}
		
		msg = heroAgentRef.receive(mtSharingIntent);
		if(msg != null) {
			evaluateIntent(msg);
		}
		
		msg = heroAgentRef.receive(mtCallForHelp);
		if(msg != null) {
			respondToCallForHelp(msg);
		}
		
		msg = heroAgentRef.receive(mtAcceptTrade);
		if(msg != null) {
			onAcceptedTradeRequest(msg);
		}
			
		msg = heroAgentRef.receive(mtTrade);
		if(msg != null) {
			if(msg.getPerformative() == ACLMessage.CFP) {
				answerTradeInit(msg);
			}
			else if(msg.getPerformative() == ACLMessage.PROPOSE) {
				answerNewProposedOffer(msg);
			}
			else if(msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
				answerAcceptedOffer(msg);
			}
			else if(msg.getPerformative() == ACLMessage.CANCEL) {
				BaseAgent sender = AgentUtils.getAgentByLocalName(msg.getSender().getLocalName());
				heroAgentRef.addToLog(sender.getDisplayName() + " canceled the trade");
				equipmentForSale = null;
				equipmentPrice = 0;
				heroAgentRef.setIsTrading(false);
				heroAgentRef.setBehaviour(BehaviourType.IDLE);
			}
		}
	}
	
	private void onAcceptedTradeRequest(ACLMessage msg) {
		BaseAgent senderAgent = AgentUtils.getAgentByLocalName(msg.getSender().getLocalName());

		heroAgentRef.setTradeAgent(senderAgent);
		heroAgentRef.setBehaviour(BehaviourType.TRADING);
	}
	
	@Override
	protected void evaluateIntent(ACLMessage msg) {
		BaseAgent senderAgent = AgentUtils.getAgentByLocalName(msg.getSender().getLocalName());
		
		if(senderAgent == null) {
			return;
		}
		
		System.out.println(heroAgentRef.getLocalName() + ": Intent received from " + senderAgent.getLocalName());
		
		BehaviourType agentIntent = BehaviourType.valueOf(msg.getContent());

		heroAgentRef.addToLog("Accepted " + agentIntent + " intent from " + senderAgent.getDisplayName());
		
		switch (agentIntent) {
		case EXPLORATION: {
			if(heroAgentRef.getAgentsInCombatWith().isEmpty() && heroAgentRef.getBehaviour() != BehaviourType.TRADING && heroAgentRef.getBehaviour() != BehaviourType.TRADING_IDLE) {
				heroAgentRef.setBehaviour(BehaviourType.EXPLORATION);
				Random random = new Random();
				int chance = random.nextInt(20);
				
				heroAgentRef.addToLog("I talked with " + senderAgent.getDisplayName());
				
				if(chance == 0) {
					heroAgentRef.improveRelationshipWithAgent(senderAgent.getLocalName(), senderAgent.getDisplayName());
				}
				else if(chance == 1) {
					heroAgentRef.lowerRelationshipWithAgent(senderAgent.getLocalName(), senderAgent.getDisplayName());
				}
			}
			break;
		}
		case TRADING: {
			if(heroAgentRef.getBehaviour() != BehaviourType.COMBAT && heroAgentRef.getBehaviour() != BehaviourType.FLEEING 
					&& heroAgentRef.getBehaviour() != BehaviourType.TRADING && heroAgentRef.getBehaviour() != BehaviourType.TRADING_IDLE) {
				heroAgentRef.setBehaviour(BehaviourType.TRADING_IDLE);
				
				ACLMessage reply = msg.createReply();
				reply.setPerformative(ACLMessage.AGREE);
				reply.setConversationId("acceptTrade");
				
				heroAgentRef.send(reply);
				heroAgentRef.setBehaviour(BehaviourType.TRADING_IDLE);
			}
			break;
		}
		case COMBAT: {
			
			if(!heroAgentRef.getAgentsInCombatWith().contains(senderAgent)) {
				heroAgentRef.getAgentsInCombatWith().add(senderAgent);
			}
			
			if(heroAgentRef.getBehaviour() == BehaviourType.COMBAT) {
				if(shouldFlee() && canFlee()) {
					heroAgentRef.setBehaviour(BehaviourType.FLEEING);
				}
				else {
					heroAgentRef.setBehaviour(BehaviourType.COMBAT);
				}
			}
			else {
				if(heroAgentRef.getBehaviour() == BehaviourType.FLEEING) {
					if(canFlee() && shouldFlee()) {
						heroAgentRef.setBehaviour(BehaviourType.FLEEING);
					}
					else {
						heroAgentRef.setBehaviour(BehaviourType.COMBAT);
					}
				}
				else {
					if(shouldEnterCombat(senderAgent) || !canFlee()) {
						heroAgentRef.setBehaviour(BehaviourType.COMBAT);
					}
					else {
						heroAgentRef.setBehaviour(BehaviourType.FLEEING);
					}
				}
			}
			break;
		}
		
		}
	}
	
	@Override
	protected boolean shouldFlee() {
		float warriorHelthPercentForFleeing = 0.5f;
		float rogueHelthPercentForFleeing = 0.75f;
		float archerHelthPercentForFleeing = 0.6f;
		
		if(heroAgentRef.getPersonalityTrait() == HeroPersonalityTrait.BRAVE) {
			Random random = new Random();
			int chance = random.nextInt(4);
			if(chance == 0) {
				return false;
			}
			
			warriorHelthPercentForFleeing /= 2;
			rogueHelthPercentForFleeing /= 2;
			archerHelthPercentForFleeing /= 2;
		}
		else if(heroAgentRef.getPersonalityTrait() == HeroPersonalityTrait.COWARD) {
			warriorHelthPercentForFleeing *= 1.5;
			rogueHelthPercentForFleeing *= 1.5;
			archerHelthPercentForFleeing *= 1.5;
		}
		
		//System.out.println(heroAgentRef.getLocalName() + ": " + heroAgentRef.getCurrentHealth() + " <-> " + heroAgentRef.getMaxHealth()*warriorHelthPercentForFleeing);
		
		if(heroAgentRef.getHeroType() == HeroType.WARRIOR && heroAgentRef.getCurrentHealth() > heroAgentRef.getMaxHealth()*warriorHelthPercentForFleeing) {
			return false;
		}
		else if(heroAgentRef.getHeroType() == HeroType.ROGUE && heroAgentRef.getCurrentHealth() > heroAgentRef.getMaxHealth()*rogueHelthPercentForFleeing) {
			return false;
		}
		else if(heroAgentRef.getHeroType() == HeroType.ARCHER && heroAgentRef.getCurrentHealth() > heroAgentRef.getMaxHealth()*archerHelthPercentForFleeing) {
			return false;
		}
		else if(heroAgentRef.hasPotion(PotionType.SUPERHEALTH)){
			heroAgentRef.drinkBestPotion(PotionType.SUPERHEALTH);
		}
		else if(heroAgentRef.hasPotion(PotionType.REGENARATION)){
			heroAgentRef.drinkBestPotion(PotionType.REGENARATION);
		}
		else if(heroAgentRef.hasPotion(PotionType.HEALTH)){
			heroAgentRef.drinkBestPotion(PotionType.HEALTH);
		}
		
		ArrayList<BaseAgent> attackingAgents = new ArrayList<>();
		
		for (BaseAgent agent : heroAgentRef.getAgentsInCombatWith()) {
			if(heroAgentRef.equals(agent.getCombatTargetAgent())){
				attackingAgents.add(agent);
			}
		}
		
		if(attackingAgents.isEmpty()) {
			return false;
		}
		
		int agentHealth = heroAgentRef.getCurrentHealth();
		int agentAttack = heroAgentRef.getAttack();
		
		int enemyTotalHealth = 0;
		int enemyTotalAttack = 0;
		
		for (BaseAgent agent : attackingAgents) {
			enemyTotalHealth += agent.getCurrentHealth();
			enemyTotalAttack += agent.getAttack();
		}
		
		int turnsToKillEnemies = (int) Math.ceil(enemyTotalHealth/(agentAttack * 1.0));
		int turnsEnemiesToKillAgent = (int) Math.ceil(agentHealth/(enemyTotalAttack * 1.0));
		
		System.out.println(heroAgentRef.getLocalName() + ": " + turnsToKillEnemies + " - " + turnsEnemiesToKillAgent + " = " + (turnsEnemiesToKillAgent < turnsToKillEnemies));
		//System.out.println(heroAgentRef.getCurrentHealth());
		
		return turnsEnemiesToKillAgent < turnsToKillEnemies;
	}
	
	private void answerTradeInit(ACLMessage msg) {
		
		if(heroAgentRef.getBehaviour() != BehaviourType.TRADING && heroAgentRef.getBehaviour() != BehaviourType.TRADING_IDLE) {
			ACLMessage reply = msg.createReply();
			reply.setPerformative(ACLMessage.CANCEL);
			heroAgentRef.setIsTrading(false);
			heroAgentRef.setBehaviour(BehaviourType.IDLE);
			reply.setReplyWith("buyer");
			heroAgentRef.send(reply);
			System.out.println(heroAgentRef.getLocalName() + ": I cancel the trade.");
			BaseAgent sender = AgentUtils.getAgentByLocalName(msg.getSender().getLocalName());
			heroAgentRef.addToLog("I cancel the trade with " + sender.getDisplayName());
			return;
		}
		
		String msgContent = msg.getContent();
		
		EquipmentType type = EquipmentType.valueOf(msgContent.split(":")[0]);
		int bonusStat = Integer.parseInt(msgContent.split(":")[1]);
		
		equipmentForSale = heroAgentRef.getEquipmentWithBetterStats(type, bonusStat);
		
		ACLMessage reply = msg.createReply();
		
		if(equipmentForSale!=null) {
			Random random = new Random();
			int percentMarkupPrice = random.nextInt(46)+5;
			
			if(heroAgentRef.getPersonalityTrait() == HeroPersonalityTrait.GREEDY) {
				percentMarkupPrice += 15;
			}
			
			equipmentPrice = (int) Math.round(equipmentForSale.getPrice() * (1 + percentMarkupPrice/100.0));
			
			reply.setPerformative(ACLMessage.INFORM);
			
			reply.setContent(equipmentForSale.getType() + ":" + equipmentForSale.getMaterial() + ":" + equipmentForSale.getQuality()
								+ ":" + equipmentForSale.getPrice() + ":" + equipmentPrice);
			
			System.out.println(heroAgentRef.getLocalName() + ": Here's a better weapon for " + equipmentPrice + "G");
			heroAgentRef.addToLog("I offer " + msg.getSender().getLocalName() + " a " + equipmentForSale.getQuality() + " " + 
					equipmentForSale.getMaterial() + " " + equipmentForSale.getType() + " for " + equipmentPrice + " gold");
		}
		else {
			reply.setPerformative(ACLMessage.REFUSE);
			reply.setContent("nothingToSale");
			heroAgentRef.setIsTrading(false);
			heroAgentRef.setBehaviour(BehaviourType.IDLE);
			
			System.out.println(heroAgentRef.getLocalName() + ": Sorry, don't have any.");
			heroAgentRef.addToLog("I cancel the trade with " + msg.getSender().getLocalName() + " due to not having the item they want");
		}
		
		reply.setReplyWith("buyer");
		heroAgentRef.send(reply);
	}

	private void answerNewProposedOffer(ACLMessage msg) {
		
		if(heroAgentRef.getBehaviour() != BehaviourType.TRADING && heroAgentRef.getBehaviour() != BehaviourType.TRADING_IDLE) {
			System.out.println(heroAgentRef.getLocalName() + ": I cancel the trade.");
			return;
		}
		
		int proposedPrice = Integer.parseInt(msg.getContent().split(":")[0]);
		
		int reducedByPercentage = (int) Math.round(100.0 * proposedPrice/equipmentPrice);
		
		int chanceToAccept = reducedByPercentage;
		
		Random random = new Random();
		int chance = random.nextInt(100);
		
		BaseAgent sender = AgentUtils.getAgentByLocalName(msg.getSender().getLocalName());
		
		if(chance < chanceToAccept) {
			equipmentPrice = proposedPrice;
			answerAcceptedOffer(msg);
			System.out.println(heroAgentRef.getLocalName() + ": Ok, I agree.");
			heroAgentRef.addToLog("I accept " + sender.getDisplayName() + "'s new offer of " + proposedPrice + " gold");
		}
		else {
			System.out.println(heroAgentRef.getLocalName() + ": I cancel the trade.");
			heroAgentRef.addToLog("I cancel the trade with " + sender.getDisplayName() + " due to wanting me to sell for too low of a price");
			
			if(heroAgentRef.getPersonalityTrait() == HeroPersonalityTrait.GREEDY) {
				chance = random.nextInt(7);
			}
			else {
				chance = random.nextInt(20);
			}
			
			if(chance == 0) {
				heroAgentRef.lowerRelationshipWithAgent(sender.getLocalName(), sender.getDisplayName());
			}
		}
	}
	
	private void answerAcceptedOffer(ACLMessage msg) {
		BaseHeroAgent senderAgent = (BaseHeroAgent) AgentUtils.getAgentByLocalName(msg.getSender().getLocalName());
		
		String[] msgContent = msg.getContent().split(":");
		
		if(senderAgent.getGold() < equipmentPrice) {
			heroAgentRef.addGold(senderAgent.getGold());
			senderAgent.removeGold(senderAgent.getGold());
			
			for(int i = 1; i< msgContent.length; i ++) {
				MaterialType material = MaterialType.valueOf(msgContent[i].split("-")[0]);
				int amount = Integer.parseInt(msgContent[i].split("-")[1]);
				
				senderAgent.removeMaterial(material, amount);
				heroAgentRef.addMaterial(material, amount);
			}
		}
		else {
			senderAgent.removeGold(equipmentPrice);
			heroAgentRef.addGold(equipmentPrice);
		}
		heroAgentRef.removeEquipmentByName(equipmentForSale.getName());
		senderAgent.addEquipment(equipmentForSale);
		
		equipmentForSale = null;
		equipmentPrice = 0;
		heroAgentRef.setIsTrading(false);
		heroAgentRef.setBehaviour(BehaviourType.IDLE);
		
		System.out.println(heroAgentRef.getLocalName() + ": Transaction complete.");
		heroAgentRef.addToLog("Trade with " + senderAgent.getDisplayName() + " ended");
		
		Random random = new Random();
		int chance = 0;
		
		if(heroAgentRef.getPersonalityTrait() == HeroPersonalityTrait.GREEDY) {
			chance = random.nextInt(7);
		}
		else {
			chance = random.nextInt(20);
		}
		
		if(chance == 0) {
			heroAgentRef.improveRelationshipWithAgent(senderAgent.getLocalName(), senderAgent.getDisplayName());
		}
	}
	
	@Override
	public void loadOwner(BaseAgent agent) {
		super.loadOwner(agent);
		heroAgentRef = (BaseHeroAgent) agent;
	}
}
