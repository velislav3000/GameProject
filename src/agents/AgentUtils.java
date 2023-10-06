package agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import collectiables.materials.BaseMaterial;
import jade.core.IMTPException;
import jade.core.NameClashException;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import tiles.Tile;
import ui.infoDialogs.AgentInfoDialog;

public class AgentUtils {
	public static AgentInfoDialog infoFrame;
	
	public static UpdaterAgent updater;
	
	public static ContainerController agentContainer;
	public static int agentCounter = 0;
	public static ArrayList<BaseAgent> agentsArray;
	private static HashMap<String, Tile> spawnTiles = new HashMap<>();

	public static ArrayList<BaseMaterial> materialsArray;
	
	public static BaseAgent getAgentByLocalName(String name) {
		for(BaseAgent agent : agentsArray) {
			if(agent.getLocalName().equals(name)) {
				return agent;
			}
		}
		return null;
	}
	
	public static void spawnAgent(String agentClassName, boolean isHeroAgent, Tile spawnTile) {
		spawnAgent(agentClassName, isHeroAgent, spawnTile, (BaseAgent)null);
	}
	
	public static void spawnAgent(String agentClassName, boolean isHeroAgent, Tile spawnTile, BaseAgent spawner) {
		
		String agentPackage = "heroAgents";
		if(isHeroAgent) {
			agentPackage = "heroAgents";
		}
		else {
			agentPackage = "monsterAgents";
		}

		String spawnerName = spawner == null ? "null" : spawner.getLocalName();
		spawnTiles.put(spawnerName, spawnTile);
		
		try {
			AgentController agc = agentContainer.createNewAgent(agentClassName + getNextAgentIndex(), "agents." + agentPackage + "." + agentClassName + "Agent", new Object[] {spawnerName});
			agc.start();
			
	        System.out.println("Agent Spawned---" + agc.getName().split("@")[0] + "---" + spawnerName + "---" + spawnTiles.get(spawnerName));
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
	}
	
	public static void spawnAgent(String agentClassName, boolean isHeroAgent, Tile spawnTile, String spawnerName) {
		String agentPackage = "heroAgents";
		if(isHeroAgent) {
			agentPackage = "heroAgents";
		}
		else {
			agentPackage = "monsterAgents";
		}
		
		spawnTiles.put(spawnerName, spawnTile);
		
		try {
			AgentController agc = agentContainer.createNewAgent(agentClassName + getNextAgentIndex(), "agents." + agentPackage + "." + agentClassName + "Agent", new Object[] {spawnerName});
			agc.start();
			
	        System.out.println("Agent Spawned---" + agc.getName().split("@")[0] + "---" + spawnerName + "---" + spawnTiles.get(spawnerName));
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
	}
	
	private synchronized static int getNextAgentIndex() {
		agentCounter++;
		return agentCounter;
	}
	
	public static Tile getSpawnTile(String spawnerName) {
		return spawnTiles.get(spawnerName);
	}

	public static void createUpdaterAgent() {
		AgentController agc;
		try {
			agc = agentContainer.createNewAgent("Updater", "agents.UpdaterAgent", new Object[] {});
			agc.start();
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
	}

	public static void destroyAll() {
		updater.doDelete();
		for (BaseAgent agent : agentsArray) {
			agent.doDelete();
		}
		agentCounter = 0;
		
		try {
			agentContainer.kill();
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
	}
}
