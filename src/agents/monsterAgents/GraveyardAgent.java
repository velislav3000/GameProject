package agents.monsterAgents;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import ontology.GameOntology;
import saveData.TileSaveData;

public class GraveyardAgent extends BaseMonsterStructureAgent{

	private transient static BufferedImage agentImage;
	
	public GraveyardAgent() {
		super();
		agentImageName = "agent_graveyard.png";
	}
	
	@Override
	protected void setup() {
		if(agentWasLoaded) {
			return;
		}
		
		maxHealth = 100;
		currentHealth = maxHealth;
		attack = 10;
		
		baseAgentSpawningCooldown = 20;
		super.setup();
	}

	@Override
	protected void levelUp() {
		currentXP -= neededXP;
		neededXP = (int) Math.pow(4, level);
		
		int oldLevel = level;
		int oldHealth = maxHealth;
		int oldAttack = attack;
		
		level += 1;
		maxHealth += 20;
		currentHealth += 20;
		attack += 4;
		
		GameOntology.updateStructureAgentStats(this, oldLevel, oldHealth, oldAttack, owner);
	}
	
	@Override
	public String getSpawnedUnitClassName() {
		return "Skeleton";
	}
	
	@Override
	public BufferedImage getImage() {
		if(agentImage == null) {
			try {
				agentImage = ImageIO.read(new File(agentImagePath, agentImageName));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return agentImage;
	}
	
	@Override
	public void loadAgent(TileSaveData save) {
		super.loadAgent(save);
		agentImageName = "agent_graveyard.png";
	}
}
