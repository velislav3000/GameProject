package agents.behaviours;

import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

import agents.BaseAgent;
import agents.BaseEntityAgent;
import agents.BaseEntityAgent.BehaviourType;import agents.BaseStructureAgent;
import agents.heroAgents.BaseHeroAgent;
import agents.heroAgents.BaseHeroAgent.HeroPersonalityTrait;
import agents.items.HeroInventory;
import agents.items.BaseItem.ItemQuality;
import agents.items.ItemUtils;
import agents.items.equipment.Equipment;
import agents.items.equipment.Equipment.EquipmentType;
import agents.items.potions.BasePotion.PotionType;
import collectiables.materials.BaseMaterial.MaterialType;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import ontology.GameOntology;

public class TradeInitiatorBehaviour extends Behaviour{

	private int step = 0;
	private MessageTemplate mt;
	private transient BaseHeroAgent agentRef;
	private transient BaseAgent sellerAgent;
	private HashMap<MaterialType, Integer> materialsToSell;
	
	public TradeInitiatorBehaviour(BaseHeroAgent agent, BaseAgent seller) {
		agentRef = agent;
		sellerAgent = seller;
		materialsToSell = new HashMap<>();
	}
	
	@Override
	public void action() {
		switch (step) {
		case 0: {
			agentRef.addToLog("Started trading with " + sellerAgent.getDisplayName());
			ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
			cfp.addReceiver(sellerAgent.getAID());
			
			if(sellerAgent.isHeroStructureAgent()) {
				Random random = new Random();
				int chance = random.nextInt(3);
				
				if(chance == 0 && !agentRef.isMissingPotions()) {
					chance = random.nextInt(2)+1;
					
					if(chance == 2 && agentRef.getRandomUnusedEquipment() == null) {
						chance = 1;
					}
				}
				else if(chance == 2 && agentRef.getRandomUnusedEquipment() == null) {
					chance = random.nextInt(2);
					
					if(chance == 0 && !agentRef.isMissingPotions()) {
						chance = 1;
					}
				}
				
				if(chance == 0) {
					
					String potionType = agentRef.getMissingPotionType().toString();
					
					cfp.setContent(potionType);
					cfp.setConversationId("buyingPotion");
					cfp.setReplyWith("seller");
					
					mt = MessageTemplate.and(MessageTemplate.MatchConversationId("buyingPotion"), MessageTemplate.MatchReplyWith("buyer"));
					agentRef.send(cfp);
					
					agentRef.addToLog("Asked " + sellerAgent.getDisplayName() + " for a " + potionType + " potion");
					
					step = 2;
					break;
				}
				else if(chance == 1) {
						String tradeDesire = agentRef.getTradeDesireString();
						
						cfp.setContent(tradeDesire);
						cfp.setConversationId("trading");
						cfp.setReplyWith("seller");
						
						mt = MessageTemplate.and(MessageTemplate.MatchConversationId("trading"), MessageTemplate.MatchReplyWith("buyer"));
						agentRef.send(cfp);
						
						System.out.println(agentRef.getLocalName() +": Looking for better " + tradeDesire.split(":")[0]);
						agentRef.addToLog("Asked " + sellerAgent.getDisplayName() + " for a better " + tradeDesire.split(":")[0]);
						
						step++;
				}
				else {
					Equipment equipmentToSell = agentRef.getRandomUnusedEquipment();
					
					try {
						cfp.setContentObject(equipmentToSell);
					} catch (IOException e) {
						e.printStackTrace();
					}
					cfp.setConversationId("sellingEquipment");
					cfp.setReplyWith("seller");
					
					mt = MessageTemplate.and(MessageTemplate.MatchConversationId("sellingEquipment"), MessageTemplate.MatchReplyWith("buyer"));
					agentRef.send(cfp);
					
					agentRef.addToLog("Tried to sell " + equipmentToSell.getName() + " to " + sellerAgent.getDisplayName() + " for " + equipmentToSell.getPrice() + " gold");
					
					step = 3;
				}	
			}
			else {
				String tradeDesire = agentRef.getTradeDesireString();
				
				cfp.setContent(tradeDesire);
				cfp.setConversationId("trading");
				cfp.setReplyWith("seller");
				
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("trading"), MessageTemplate.MatchReplyWith("buyer"));
				agentRef.send(cfp);
				
				System.out.println(agentRef.getLocalName() +": Looking for better " + tradeDesire.split(":")[0]);
				agentRef.addToLog("Asked " + sellerAgent.getDisplayName() + " for a better " + tradeDesire.split(":")[0]);
				
				step++;
			}
			
			break;
		}
		case 1: {
			ACLMessage msg = agentRef.receive(mt);
			
			if(msg != null) {
				
				if(msg.getPerformative() == ACLMessage.INFORM) {
					
					if(agentRef.getBehaviour() != BehaviourType.TRADING) {
						ACLMessage reply = msg.createReply();
						reply.setPerformative(ACLMessage.CANCEL);
						System.out.println(agentRef.getLocalName() + ": I cancel the trade.");
						agentRef.addToLog("I cancel the trade with " + sellerAgent.getDisplayName());
						reply.setReplyWith("seller");
						agentRef.send(reply);
						step = 10;
						return;
					}
					
					String msgContent = msg.getContent();
					
					EquipmentType equipmentType = EquipmentType.valueOf(msgContent.split(":")[0]);
					MaterialType materialType = MaterialType.valueOf(msgContent.split(":")[1]);
					ItemQuality itemQuality = ItemQuality.valueOf(msgContent.split(":")[2]);
					int basePrice = Integer.parseInt(msgContent.split(":")[3]);
					int sellerPrice = Integer.parseInt(msgContent.split(":")[4]);
					
					agentRef.addToLog(sellerAgent.getDisplayName() + " has a " + itemQuality + " " + materialType + " " + equipmentType + " for " + sellerPrice + " gold");
					
					int acceptedPrice = getAcceptablePrice(equipmentType, materialType, itemQuality, basePrice, sellerPrice);
					
					if(acceptedPrice > agentRef.getGold()) {
						setMaterialsToBeSold(acceptedPrice - agentRef.getGold());
					}
					
					ACLMessage reply = msg.createReply();
					
					if(acceptedPrice == -1) {
						reply.setPerformative(ACLMessage.CANCEL);
						System.out.println(agentRef.getLocalName() + ": I cancel the trade.");
						agentRef.addToLog("I cancel the trade with " + sellerAgent.getDisplayName() + " due to him asking for too big of a price");
						
						Random random = new Random();
						int chance = 0;
						
						if(agentRef.getPersonalityTrait() == HeroPersonalityTrait.GREEDY) {
							chance = random.nextInt(7);
						}
						else {
							chance = random.nextInt(20);
						}
						
						if(chance == 0) {
							agentRef.lowerRelationshipWithAgent(sellerAgent.getLocalName(), sellerAgent.getDisplayName());
						}
					}
					else if(acceptedPrice == sellerPrice) {
						reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
						reply.setContent(setReplyContent(acceptedPrice));
						System.out.println(agentRef.getLocalName() + ": I accept your offer.");
						agentRef.addToLog("I accepted " + sellerAgent.getDisplayName() + "'s price");
					}
					else {
						reply.setPerformative(ACLMessage.PROPOSE);
						reply.setContent(setReplyContent(acceptedPrice));
						System.out.println(agentRef.getLocalName() + ": How about this price - " + acceptedPrice + "G");
						agentRef.addToLog("I offered " + acceptedPrice + " for " + sellerAgent.getDisplayName() + "'s item");
					}
					
					reply.setReplyWith("seller");
					agentRef.send(reply);
					step = 10;
					
				}
				else {
					agentRef.addToLog(sellerAgent.getDisplayName() + " canceled the trade");
					step = 10;
				}
				
			}
			
			break;
		}
		case 2: {
			ACLMessage msg = agentRef.receive(mt);
			
			if(msg != null) {
				
				if(msg.getPerformative() == ACLMessage.INFORM) {
					
					if(agentRef.getBehaviour() != BehaviourType.TRADING) {
						ACLMessage reply = msg.createReply();
						reply.setPerformative(ACLMessage.CANCEL);
						System.out.println(agentRef.getLocalName() + ": I cancel the trade.");
						agentRef.addToLog("I cancel the trade with " + sellerAgent.getDisplayName());
						reply.setReplyWith("seller");
						agentRef.send(reply);
						step = 10;
						return;
					}
					
					String msgContent = msg.getContent();
					
					ItemQuality itemQuality = ItemQuality.valueOf(msgContent.split(":")[0]);
					PotionType potionType = PotionType.valueOf(msgContent.split(":")[1]);
					int price = Integer.parseInt(msgContent.split(":")[2]);
					
					agentRef.addToLog(sellerAgent.getDisplayName() + " has a " + itemQuality + " " + potionType + " potion for " + price + " gold");
					
					ACLMessage reply = msg.createReply();
					
					if(price > agentRef.getTotalGold()) {
						reply.setPerformative(ACLMessage.CANCEL);
						System.out.println(agentRef.getLocalName() + ": I cancel the trade.");
						agentRef.addToLog("I cancel the trade with " + sellerAgent.getDisplayName() + " due to him asking for too big of a price");
					}
					else{
						reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
						reply.setContent(setReplyContent(price));
						System.out.println(agentRef.getLocalName() + ": I accept your offer.");
						agentRef.addToLog("I accepted " + sellerAgent.getDisplayName() + "'s price");
					}
					
					reply.setReplyWith("seller");
					agentRef.send(reply);
					step = 10;
					
				}
				else {
					agentRef.addToLog(sellerAgent.getDisplayName() + " canceled the trade");
					step = 10;
				}
				
			}
			
			break;
		}
		case 3:{
			ACLMessage msg = agentRef.receive(mt);
			
			if(msg != null) {
				if(msg.getPerformative() == ACLMessage.AGREE) {
					agentRef.addToLog(sellerAgent.getDisplayName() + " accepted the trade");
				}
				else {
					agentRef.addToLog(sellerAgent.getDisplayName() + " canceled the trade");
				}
				step = 10;
			}
		}
		
		}
	}

	private String setReplyContent(int acceptedPrice) {
		if(acceptedPrice == agentRef.getGold()) {
			return acceptedPrice + "";
		}
		
		String contentString = acceptedPrice + "";
		
		for (MaterialType material : materialsToSell.keySet()) {
			contentString += ":" + material.toString() + "-" + materialsToSell.get(material);
		}
		
		return contentString;
	}

	@Override
	public boolean done() {
		if(step == 10) {
			agentRef.setTradeAgent(null);
			agentRef.setIsTrading(false);
			if(sellerAgent.isHeroStructureAgent()) {
				agentRef.startTradeCooldown(4);
			}
			else {
				agentRef.startTradeCooldown(7);
			}
			agentRef.setBehaviour(BehaviourType.IDLE);
			return true;
		}
		return false;
	}
	

	private int getAcceptablePrice(EquipmentType equipmentType, MaterialType materialType, ItemQuality itemQuality,
			int basePrice, int sellerPrice) {
		int chanceToAgree = 0;
		int chanceToRefuse = 0;
		int chanceToReduceInHalf = 0;
		int chanceToReduceToQuarter = 0;
		
		int sellerPriceIncrease = (int) Math.round((1.0*sellerPrice/basePrice-1)*100);
		int acceptablePriceIncrease = getAcceptablePriceIncrease(equipmentType, materialType, itemQuality);
		
		if(agentRef.getPersonalityTrait() == HeroPersonalityTrait.GREEDY) {
			acceptablePriceIncrease = Math.round(acceptablePriceIncrease * 0.9f);
		}
		
		int agentTotalGold = agentRef.getTotalGold();
		
		if(basePrice > agentTotalGold) {
			return -1;
		}
		
		if(agentTotalGold < sellerPrice/4) {
			Random random = new Random();
			int chance = random.nextInt(100);
			if(chance < 5) {
				return agentTotalGold;
			}
			return -1;
		}
		
		if(acceptablePriceIncrease > sellerPriceIncrease) {
			chanceToAgree = 75;
			chanceToReduceInHalf = 20;
			chanceToReduceToQuarter = 5;
		}
		else if(acceptablePriceIncrease == sellerPriceIncrease) {
			chanceToAgree = 60;
			chanceToReduceInHalf = 30;
			chanceToReduceToQuarter = 10;
		}
		else {
			chanceToAgree = (int) Math.max(((1 - (1.0 * sellerPriceIncrease - acceptablePriceIncrease)/acceptablePriceIncrease) * 60), 0);
			
			chanceToReduceInHalf = (int) Math.max(((1 - (1.0 * sellerPriceIncrease - acceptablePriceIncrease*2)/acceptablePriceIncrease*2) * 20), 0);
			
			chanceToReduceToQuarter = (int) Math.max(((1 - (1.0 * sellerPriceIncrease - acceptablePriceIncrease*4)/acceptablePriceIncrease*4) * 5), 0);
			
			chanceToRefuse = 20;
		}
		
		if(agentTotalGold < sellerPrice) {
			chanceToReduceInHalf +=  chanceToAgree*2/3;
			chanceToReduceToQuarter += chanceToAgree/3;
			chanceToAgree = 0;
		}
		
		if(agentTotalGold < sellerPrice/2) {
			chanceToReduceToQuarter += chanceToReduceInHalf;
			chanceToReduceInHalf = 0;
		}

		int totalChance = chanceToAgree + chanceToRefuse + chanceToReduceInHalf + chanceToReduceToQuarter;
		
		Random random = new Random();
		int chance = random.nextInt(totalChance);
		if(chance < chanceToAgree) {
			return sellerPrice;
		}
		
		totalChance -= chanceToAgree;
		chance = random.nextInt(totalChance);
		if(chance < chanceToReduceInHalf) {
			return (int) (basePrice * (1 + (1.0*sellerPriceIncrease/2)/100));
		}
		
		totalChance -= chanceToReduceInHalf;
		chance = random.nextInt(totalChance);
		if(chance < chanceToReduceToQuarter) {
			return (int) (basePrice * (1 + (1.0*sellerPriceIncrease/4)/100));
		}
				
		return -1;
	}
	
	private int getAcceptablePriceIncrease(EquipmentType equipmentType, MaterialType materialType,
			ItemQuality itemQuality) {
		
		Equipment equipedEquipment = agentRef.getEquipedEquipment(equipmentType);
		
		if(equipedEquipment != null) {
			if(materialType.equals(equipedEquipment.getMaterial())) {
				if(itemQuality.ordinal() == equipedEquipment.getQuality().ordinal() + 1) {
					return 7;
				}
				else {
					return 10;
				}
			}
			else if(materialType.ordinal() == equipedEquipment.getMaterial().ordinal() + 1){
				switch (itemQuality) {
				case POOR:{
					return 12;
				}
				case AVERAGE:{
					return 15;
				}
				default:
					return 20;
				}
			}
			else if(materialType.ordinal() == equipedEquipment.getMaterial().ordinal() + 2){
				switch (itemQuality) {
				case POOR:{
					return 23;
				}
				case AVERAGE:{
					return 27;
				}
				default:
					return 30;
				}
			}
			else {
				switch (itemQuality) {
				case POOR:{
					return 32;
				}
				case AVERAGE:{
					return 35;
				}
				default:
					return 40;
				}
			}
		}
		
		return 0;
	}
	
	private void setMaterialsToBeSold(int neededValue) {
		
		for (MaterialType material : MaterialType.values()) {
			int materialAmount = agentRef.getMaterialTotal(material);
			int totalMaterialValue = materialAmount * ItemUtils.getBaseMaterialPrice(material);
			
			if(totalMaterialValue >= neededValue) {
				int neededMaterialAmount = Math.round(1.0F*neededValue / ItemUtils.getBaseMaterialPrice(material));
				materialsToSell.put(material, neededMaterialAmount);
				return;
			}
			else {
				materialsToSell.put(material, materialAmount);
				neededValue -= totalMaterialValue;
			}
		}
	}

}
