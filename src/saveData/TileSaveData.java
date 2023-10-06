package saveData;

import java.util.ArrayList;

import agents.BaseAgent;
import agents.BaseEntityAgent;
import agents.BaseStructureAgent;
import agents.heroAgents.BaseHeroAgent;
import agents.heroAgents.CityAgent;
import agents.items.CityInventory;
import agents.items.HeroInventory;
import agents.items.equipment.Equipment;
import collectiables.CollectableLoot;
import jade.core.behaviours.Behaviour;
import jade.util.leap.Serializable;

public class TileSaveData implements Serializable{
	private String tileType = "";
	private String collectableClass = "";
	private int materialReplenishCounter;
	private String chestName;
	private CollectableLoot chestLoot;
	private BaseAgent agent;
	private ArrayList<Behaviour> behaviours;
	private ArrayList<String> perceivedAgentNames;
	private ArrayList<String> agentsInCombatWithNames;
	private String combatTargetAgentName = "";
	
	//EntityAgent
	private String spawnStructureName = "";
	private ArrayList<String> ownedStructureNames;
	
	//HeroAgent
	private String tradeAgentName = "";
	private HeroInventory heroInventory;
	
	//StructureAgent
	private String ownerName = "";
	
	//CityAgent
	private CityInventory cityInventory;
	
	public TileSaveData(String tileType, String collectableClass, int materialReplenishCounter, String chestName, CollectableLoot chestLoot, BaseAgent agent) {
		this.tileType = tileType;
		this.collectableClass = collectableClass;
		this.materialReplenishCounter = materialReplenishCounter;
		this.chestName = chestName;
		this.chestLoot = chestLoot;
		if(agent != null) {
			setAgent(agent);
		}
	}

	private void setAgent(BaseAgent agent) {
		this.agent = agent;
		this.behaviours = agent.getBehaviours();
		perceivedAgentNames = agent.getPerceivedAgentNames();
		agentsInCombatWithNames = agent.getAgentsInCombatWithNames();
		if(agent.getCombatTargetAgent() != null) {
			combatTargetAgentName = agent.getCombatTargetAgent().getLocalName();
		}
		
		if(agent.isEntityAgent()) {
			BaseEntityAgent entityAgent = (BaseEntityAgent) agent;
			if(entityAgent.getSpawnStructure() != null) {
				spawnStructureName = entityAgent.getSpawnStructure().getLocalName();
			}
			ownedStructureNames = entityAgent.getOwnedStructureNames();
			
			if(agent.isHeroAgent()) {
				BaseHeroAgent heroAgent = (BaseHeroAgent) entityAgent;
				if(heroAgent.getTradeAgent() != null) {
					tradeAgentName = heroAgent.getTradeAgent().getLocalName();
				}
				heroInventory = heroAgent.getInventory();
			}
		}
		else if(agent.isStructureAgent()) {
			BaseStructureAgent structureAgent = (BaseStructureAgent) agent;
			if(structureAgent.getOwner() != null) {
				ownerName = structureAgent.getOwner().getLocalName();
			}
			
			if(structureAgent.isHeroStructureAgent()) {
				CityAgent cityAgent = (CityAgent) structureAgent;
				cityInventory = cityAgent.getInventory();
			}
		}
	}

	public String getTileType() {
		return tileType;
	}

	public String getCollectableClass() {
		return collectableClass;
	}

	public BaseAgent getAgent() {
		return agent;
	}

	public ArrayList<Behaviour> getBehaviours() {
		return behaviours;
	}

	public ArrayList<String> getPerceivedAgentNames() {
		return perceivedAgentNames;
	}

	public ArrayList<String> getAgentsInCombatWithNames() {
		return agentsInCombatWithNames;
	}

	public String getCombatTargetAgentName() {
		return combatTargetAgentName;
	}

	public String getSpawnStructureName() {
		return spawnStructureName;
	}

	public ArrayList<String> getOwnedStructureNames() {
		return ownedStructureNames;
	}

	public String getTradeAgentName() {
		return tradeAgentName;
	}

	public HeroInventory getHeroInventory() {
		return heroInventory;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public CityInventory getCityInventory() {
		return cityInventory;
	}
	
	public int getMaterialReplenishCounter() {
		return materialReplenishCounter;
	}

	public String getChestName() {
		return chestName;
	}

	public CollectableLoot getChestLoot() {
		return chestLoot;
	}
}
