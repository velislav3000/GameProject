package agents.behaviours.structure;

import java.util.HashMap;
import java.util.Random;

import agents.AgentUtils;
import agents.BaseAgent;
import agents.BaseEntityAgent.BehaviourType;
import agents.BaseStructureAgent.StructureBehaviourType;
import agents.heroAgents.BaseHeroAgent;
import agents.heroAgents.CityAgent;
import agents.heroAgents.BaseHeroAgent.HeroPersonalityTrait;
import agents.items.equipment.Equipment;
import agents.items.equipment.Equipment.EquipmentType;
import agents.items.potions.BasePotion;
import agents.items.potions.BasePotion.PotionType;
import collectiables.materials.BaseMaterial.MaterialType;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class MessageProcessingCityStructureBehaviour extends MessageProcessingStructureBehaviour{

	private transient CityAgent cityAgentRef;
	protected MessageTemplate mtUpdate = MessageTemplate.MatchConversationId("update");
	private MessageTemplate mtAcceptTrade = MessageTemplate.MatchConversationId("acceptTrade");
	private MessageTemplate mtTrade = MessageTemplate.and(MessageTemplate.MatchConversationId("trading"), MessageTemplate.MatchReplyWith("seller"));
	private MessageTemplate mtSellPotions = MessageTemplate.and(MessageTemplate.MatchConversationId("buyingPotion"), MessageTemplate.MatchReplyWith("seller"));
	private MessageTemplate mtBuyEquipment = MessageTemplate.and(MessageTemplate.MatchConversationId("sellingEquipment"), MessageTemplate.MatchReplyWith("seller"));
	
	//For Trading
	private transient HashMap<AID, Equipment> equipmentForSale;
	private transient HashMap<AID, BasePotion> potionsForSale;
	
	public MessageProcessingCityStructureBehaviour(CityAgent agent) {
		super(agent);
		cityAgentRef = agent;
		equipmentForSale = new HashMap<>();
		potionsForSale = new HashMap<>();
	}

	@Override
	public void action() {
		if(!cityAgentRef.isAlive()) {
			cityAgentRef.removeBehaviour(this);
			return;
		}
		
		if(cityAgentRef.getPaused()) {
			return;
		}
		
		ACLMessage msg = cityAgentRef.receive(mtUpdate);
		if(msg != null) {
			cityAgentRef.update();
		}
		
		msg = cityAgentRef.receive(mtSharingIntent);
		if(msg != null) {
			evaluateIntent(msg);
		}
		
		msg = cityAgentRef.receive(mtAcceptTrade);
		if(msg != null) {
			onAcceptedTradeRequest(msg);
		}
		
		msg = cityAgentRef.receive(mtTrade);
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
				cityAgentRef.addToLog(sender.getDisplayName() + " canceled the trade");
				equipmentForSale.remove(msg.getSender());
				cityAgentRef.setBehaviour(StructureBehaviourType.IDLE);
			}
		}
		
		msg = cityAgentRef.receive(mtSellPotions);
		if(msg != null) {
			if(msg.getPerformative() == ACLMessage.CFP) {
				answerPotionTradeInit(msg);
			}
			else if(msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
				answerAcceptedPotionOffer(msg);
			}
			else if(msg.getPerformative() == ACLMessage.CANCEL) {
				cityAgentRef.addToLog(msg.getSender().getLocalName() + " canceled the trade");
				cityAgentRef.setBehaviour(StructureBehaviourType.IDLE);
			}
		}
		
		msg = cityAgentRef.receive(mtBuyEquipment);
		if(msg != null) {
			answerEquipmentSale(msg);
		}
	}
	
	private void onAcceptedTradeRequest(ACLMessage msg) {
		cityAgentRef.setBehaviour(StructureBehaviourType.TRADING);
	}
	
	@Override
	protected void evaluateIntent(ACLMessage msg) {
		BaseAgent senderAgent = AgentUtils.getAgentByLocalName(msg.getSender().getLocalName());
		
		System.out.println(cityAgentRef.getLocalName() + ": Intent received from " + senderAgent.getLocalName());
		
		BehaviourType agentIntent = BehaviourType.valueOf(msg.getContent());

		cityAgentRef.addToLog("Accepted " + agentIntent + " intent from " + senderAgent.getDisplayName());
		
		switch (agentIntent) {
		case EXPLORATION: {
			if(cityAgentRef.getAgentsInCombatWith().isEmpty() && cityAgentRef.getBehaviour() != StructureBehaviourType.TRADING) {
				cityAgentRef.setBehaviour(StructureBehaviourType.IDLE);
			}
			break;
		}
		case TRADING: {
			if(cityAgentRef.getBehaviour() != StructureBehaviourType.COMBAT) {
				ACLMessage reply = msg.createReply();
				reply.setPerformative(ACLMessage.AGREE);
				reply.setConversationId("acceptTrade");
				
				cityAgentRef.send(reply);
				cityAgentRef.setBehaviour(StructureBehaviourType.TRADING);
			}
			break;
		}
		case COMBAT: {
			
			if(!cityAgentRef.getAgentsInCombatWith().contains(senderAgent)) {
				cityAgentRef.getAgentsInCombatWith().add(senderAgent);
			}
		}
		}
	}
	
	private void answerTradeInit(ACLMessage msg) {
		
		if(cityAgentRef.getBehaviour() != StructureBehaviourType.TRADING) {
			ACLMessage reply = msg.createReply();
			reply.setPerformative(ACLMessage.CANCEL);
			cityAgentRef.setBehaviour(StructureBehaviourType.IDLE);
			reply.setReplyWith("buyer");
			cityAgentRef.send(reply);
			System.out.println(cityAgentRef.getLocalName() + ": I cancel the trade.");
			BaseAgent sender = AgentUtils.getAgentByLocalName(msg.getSender().getLocalName());
			cityAgentRef.addToLog("I cancel the trade with " + sender.getDisplayName());
			return;
		}
		
		String msgContent = msg.getContent();
		
		EquipmentType type = EquipmentType.valueOf(msgContent.split(":")[0]);
		int bonusStat = Integer.parseInt(msgContent.split(":")[1]);
		
		Equipment equipmentForSale = cityAgentRef.getEquipmentWithBetterStats(type, bonusStat);
		
		ACLMessage reply = msg.createReply();
		
		if(equipmentForSale!=null) {
			BaseHeroAgent senderAgent = (BaseHeroAgent) AgentUtils.getAgentByLocalName(msg.getSender().getLocalName());
			
			this.equipmentForSale.put(senderAgent.getAID(), equipmentForSale);
			
			int discount = 0;
			
			if(senderAgent.getPersonalityTrait() == HeroPersonalityTrait.GREEDY) {
				discount += 15;
			}
			
			int equipmentPrice = (int) Math.round(equipmentForSale.getPrice() * (1 - discount/100.0));
			
			reply.setPerformative(ACLMessage.INFORM);
			
			reply.setContent(equipmentForSale.getType() + ":" + equipmentForSale.getMaterial() + ":" + equipmentForSale.getQuality() + ":" 
					+ equipmentForSale.getPrice() + ":" + equipmentPrice);
			
			System.out.println(cityAgentRef.getLocalName() + ": Here's a better weapon for " + equipmentPrice + "G");
			cityAgentRef.addToLog("I offer " + senderAgent.getLocalName() + " a " + equipmentForSale.getQuality() + " " + 
					equipmentForSale.getMaterial() + " " + equipmentForSale.getType() + " for " + equipmentPrice + " gold");
		}
		else {
			reply.setPerformative(ACLMessage.REFUSE);
			reply.setContent("nothingToSale");
			cityAgentRef.setBehaviour(StructureBehaviourType.IDLE);
			
			System.out.println(cityAgentRef.getLocalName() + ": Sorry, don't have any.");
			cityAgentRef.addToLog("I cancel the trade with " + msg.getSender().getLocalName() + " due to not having the item they want");
		}
		
		reply.setReplyWith("buyer");
		cityAgentRef.send(reply);
	}

	private void answerNewProposedOffer(ACLMessage msg) {
		System.out.println(cityAgentRef.getLocalName() + ": I cancel the trade.");
		BaseAgent sender = AgentUtils.getAgentByLocalName(msg.getSender().getLocalName());
		cityAgentRef.addToLog("I canceled the trade with " + sender.getDisplayName() + " due to not accepting bargaining");
	}
	
	private void answerAcceptedOffer(ACLMessage msg) {
		BaseHeroAgent senderAgent = (BaseHeroAgent) AgentUtils.getAgentByLocalName(msg.getSender().getLocalName());
		
		String[] msgContent = msg.getContent().split(":");
		
		int discount = 0;
		
		if(senderAgent.getPersonalityTrait() == HeroPersonalityTrait.GREEDY) {
			discount += 15;
		}
		
		int equipmentPrice = (int) Math.round(equipmentForSale.get(senderAgent.getAID()).getPrice() * (1 - discount/100.0));
		
		if(senderAgent.getGold() < equipmentPrice) {
			cityAgentRef.addGoldFromTrade(senderAgent.getGold());
			senderAgent.removeGold(senderAgent.getGold());
			
			for(int i = 1; i< msgContent.length; i ++) {
				MaterialType material = MaterialType.valueOf(msgContent[i].split("-")[0]);
				int amount = Integer.parseInt(msgContent[i].split("-")[1]);
				
				senderAgent.removeMaterial(material, amount);
				cityAgentRef.addMaterialFromTrade(material, amount);
			}
		}
		else {
			senderAgent.removeGold(equipmentPrice);
			cityAgentRef.addGoldFromTrade(equipmentPrice);
		}
		cityAgentRef.removeEquipment(equipmentForSale.get(senderAgent.getAID()));
		senderAgent.addEquipment(equipmentForSale.get(senderAgent.getAID()));
		
		equipmentForSale = null;
		equipmentPrice = 0;
		cityAgentRef.setBehaviour(StructureBehaviourType.IDLE);
		
		System.out.println(cityAgentRef.getLocalName() + ": Transaction complete.");
		cityAgentRef.addToLog("Trade with " + senderAgent.getDisplayName() + " ended");
		
		Random random = new Random();
		int chance = 0;
	}
	

	
	private void answerPotionTradeInit(ACLMessage msg) {
		if(cityAgentRef.getBehaviour() != StructureBehaviourType.TRADING) {
			ACLMessage reply = msg.createReply();
			reply.setPerformative(ACLMessage.CANCEL);
			cityAgentRef.setBehaviour(StructureBehaviourType.IDLE);
			reply.setReplyWith("buyer");
			cityAgentRef.send(reply);
			System.out.println(cityAgentRef.getLocalName() + ": I cancel the trade due to dealing with something else.");
			BaseAgent sender = AgentUtils.getAgentByLocalName(msg.getSender().getLocalName());
			cityAgentRef.addToLog("I cancel the trade with " + sender.getDisplayName());
			return;
		}
		
		String msgContent = msg.getContent();
		
		PotionType searchedPotionType = PotionType.valueOf(msgContent);
		
		BasePotion potionForSale = cityAgentRef.getPotion(searchedPotionType);
		
		if(potionsForSale.containsValue(potionForSale)) {
			potionForSale = null;
		}
		
		ACLMessage reply = msg.createReply();
		
		if(potionForSale!=null) {
			BaseHeroAgent senderAgent = (BaseHeroAgent) AgentUtils.getAgentByLocalName(msg.getSender().getLocalName());
			
			this.potionsForSale.put(senderAgent.getAID(), potionForSale);
			
			int discount = 0;
			
			if(senderAgent.getPersonalityTrait() == HeroPersonalityTrait.GREEDY) {
				discount += 15;
			}
			
			int potionPrice = (int) Math.round(potionForSale.getPrice() * (1 - discount/100.0));
			
			reply.setPerformative(ACLMessage.INFORM);
			
			reply.setContent(potionForSale.getQuality() + ":" + potionForSale.getType() + ":" + potionPrice);
			
			cityAgentRef.addToLog("I offer " + senderAgent.getLocalName() + " a " + potionForSale.getQuality() + " " 
					 + potionForSale.getType() + " for " + potionPrice + " gold");
		}
		else {
			reply.setPerformative(ACLMessage.REFUSE);
			reply.setContent("nothingToSale");
			cityAgentRef.setBehaviour(StructureBehaviourType.IDLE);
			
			System.out.println(cityAgentRef.getLocalName() + ": Sorry, don't have any.");
			cityAgentRef.addToLog("I cancel the trade with " + msg.getSender().getLocalName() + " due to not having the item they want");
		}
		
		reply.setReplyWith("buyer");
		cityAgentRef.send(reply);
	}

	private void answerAcceptedPotionOffer(ACLMessage msg) {
		BaseHeroAgent senderAgent = (BaseHeroAgent) AgentUtils.getAgentByLocalName(msg.getSender().getLocalName());
		
		String[] msgContent = msg.getContent().split(":");
		
		int discount = 0;
		
		if(senderAgent.getPersonalityTrait() == HeroPersonalityTrait.GREEDY) {
			discount += 15;
		}
		
		BasePotion soldPotion = potionsForSale.get(msg.getSender());
		
		int potionPrice = (int) Math.round(soldPotion.getPrice() * (1 - discount/100.0));
		
		if(senderAgent.getGold() < potionPrice) {
			cityAgentRef.addGoldFromTrade(senderAgent.getGold());
			senderAgent.removeGold(senderAgent.getGold());
			
			for(int i = 1; i< msgContent.length; i ++) {
				MaterialType material = MaterialType.valueOf(msgContent[i].split("-")[0]);
				int amount = Integer.parseInt(msgContent[i].split("-")[1]);
				
				senderAgent.removeMaterial(material, amount);
				cityAgentRef.addMaterialFromTrade(material, amount);
			}
		}
		else {
			senderAgent.removeGold(potionPrice);
			cityAgentRef.addGoldFromTrade(potionPrice);
		}
		cityAgentRef.removePotion(soldPotion);
		senderAgent.addPotion(soldPotion);
		potionsForSale.remove(msg.getSender());
		
		cityAgentRef.setBehaviour(StructureBehaviourType.IDLE);
		
		System.out.println(cityAgentRef.getLocalName() + ": Transaction complete.");
		cityAgentRef.addToLog("Trade with " + senderAgent.getDisplayName() + " ended");
	}
	


	private void answerEquipmentSale(ACLMessage msg) {
		if(cityAgentRef.getBehaviour() != StructureBehaviourType.TRADING) {
			ACLMessage reply = msg.createReply();
			reply.setPerformative(ACLMessage.CANCEL);
			cityAgentRef.setBehaviour(StructureBehaviourType.IDLE);
			reply.setReplyWith("buyer");
			cityAgentRef.send(reply);
			System.out.println(cityAgentRef.getLocalName() + ": I cancel the trade.");
			BaseAgent sender = AgentUtils.getAgentByLocalName(msg.getSender().getLocalName());
			cityAgentRef.addToLog("I cancel the trade with " + sender.getDisplayName());
			return;
		}
		
		BaseHeroAgent senderAgent = (BaseHeroAgent) AgentUtils.getAgentByLocalName(msg.getSender().getLocalName());
		
		Equipment equipment = null;
		try {
			equipment = (Equipment) msg.getContentObject();
		} catch (UnreadableException e) {
			e.printStackTrace();
		}
		
		int priceIncrease = 0;
		
		if(senderAgent.getPersonalityTrait() == HeroPersonalityTrait.GREEDY) {
			priceIncrease += 15;
		}
		
		int equipmentPrice = (int) Math.round(equipment.getPrice() * (1 + priceIncrease/100.0));
		
		if(equipmentPrice > cityAgentRef.getGold()) {
			ACLMessage reply = msg.createReply();
			reply.setPerformative(ACLMessage.CANCEL);
			cityAgentRef.setBehaviour(StructureBehaviourType.IDLE);
			reply.setReplyWith("buyer");
			cityAgentRef.send(reply);
			System.out.println(cityAgentRef.getLocalName() + ": I cancel the trade.");
			cityAgentRef.addToLog("I cancel the trade with " + msg.getSender().getLocalName() + " due to a lack of gold");
			return;
		}
		else {
			cityAgentRef.removeGold(equipmentPrice);
			senderAgent.addGold(equipmentPrice);
			senderAgent.removeEquipmentByName(equipment.getName());
			cityAgentRef.addEquipment(equipment);
			
			ACLMessage reply = msg.createReply();
			reply.setPerformative(ACLMessage.AGREE);
			cityAgentRef.setBehaviour(StructureBehaviourType.IDLE);
			reply.setReplyWith("buyer");
			cityAgentRef.send(reply);
			cityAgentRef.addToLog("Transaction with " + msg.getSender().getLocalName() + " was completed");
		}
	}
	
	@Override
	public void loadOwner(BaseAgent agent) {
		super.loadOwner(agent);
		cityAgentRef = (CityAgent) agent;
	}
}
