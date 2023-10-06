package saveData;

import jade.util.leap.Serializable;

public class GlobalSaveData implements Serializable{
	private int spawnShroomersCooldown;
	private int agentCounter;
	
	public GlobalSaveData(int spawnShroomersCooldown, int agentCounter) {
		this.spawnShroomersCooldown = spawnShroomersCooldown;
		this.agentCounter = agentCounter;
	}

	public int getSpawnShroomersCooldown() {
		return spawnShroomersCooldown;
	}

	public int getAgentCounter() {
		return agentCounter;
	}
}
