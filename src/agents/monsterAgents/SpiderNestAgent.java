package agents.monsterAgents;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import ontology.GameOntology;
import saveData.TileSaveData;

public class SpiderNestAgent extends BaseMonsterStructureAgent{

	private transient static BufferedImage agentImage;
	
	public SpiderNestAgent() {
		super();
		agentImageName = "agent_spider_nest.png";
	}
	
	@Override
	protected void setup() {
		if(agentWasLoaded) {
			return;
		}
		
		maxHealth = 125;
		currentHealth = maxHealth;
		attack = 7;
		
		baseAgentSpawningCooldown = 15;
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
		maxHealth += 25;
		currentHealth += 25;
		attack += 3;
		
		GameOntology.updateStructureAgentStats(this, oldLevel, oldHealth, oldAttack, owner);
	}
	
	@Override
	public String getSpawnedUnitClassName() {
		return "Spider";
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
		agentImageName = "agent_spider_nest.png";
	}
}
