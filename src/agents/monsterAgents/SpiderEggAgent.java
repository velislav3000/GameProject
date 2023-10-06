package agents.monsterAgents;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;

import agents.AgentUtils;
import agents.BaseAgent;
import agents.BaseStructureAgent;
import agents.BaseEntityAgent.RelationshipType;
import agents.behaviours.MessageProcessingBehaviour;
import agents.behaviours.MessageProcessingSpiderEggBehaviour;
import agents.monsterAgents.BaseMonsterAgent.MonsterType;
import collectiables.CollectableLoot;
import collectiables.chests.BaseChest;
import collectiables.chests.GoldChest;
import collectiables.chests.IronChest;
import collectiables.chests.WoodenChest;
import collectiables.materials.BaseMaterial.MaterialType;
import ontology.GameOntology;
import saveData.TileSaveData;

public class SpiderEggAgent extends BaseMonsterAgent{

	private int turnsUntillHatch;
	private SpiderAgent motherAgent;
	
	public SpiderEggAgent() {
		super();
		agentImageName = "agent_spider_egg.png";
	}
	
	@Override
	protected void setup() {
		if(agentWasLoaded) {
			return;
		}
		
		displayName = getLocalName();
		
		monsterType = MonsterType.SPIDER_EGG;
		
		baseMaxHealth = 5;
		currentHealth = baseMaxHealth;
		maxHealth = baseMaxHealth;
		attack = 0;
		evasionChance = 0;
		critChance = 0;
		
		baseDroppedGold = 5;
		turnsUntillHatch = 3;
		
		traits = new ArrayList<>();
		
		Object[] args = getArguments();
		String spawnerName = args[0].toString();
		
		motherAgent = null;
		
		if(!spawnerName.contentEquals("null")) {
			motherAgent = (SpiderAgent) AgentUtils.getAgentByLocalName(spawnerName);
		}
		
		AgentUtils.getSpawnTile(spawnerName).setAgent(this);
		this.setCurrentTile(AgentUtils.getSpawnTile(spawnerName));

		GameOntology.addMonsterAgent(this);
		
		setRelationshipWithAgent(motherAgent.getLocalName(), motherAgent.getDisplayName(), RelationshipType.ALLIES);

		this.addBehaviour(new MessageProcessingSpiderEggBehaviour(this));
		
		AgentUtils.agentsArray.add(this);
		
		behaviour = BehaviourType.IDLE;
	}
	
	@Override
	public void update() {
		if(!isAlive()) {
			return;
		}
		
		if(turnsUntillHatch == 0) {
			spawnSpider();
		}
		else {
			addToLog("Turns to hatch: " + turnsUntillHatch);
			turnsUntillHatch--;
		}
		System.out.println(this.getLocalName() + ": Hatch in " + turnsUntillHatch + " turns");
	}
	
	private void spawnSpider() {
		AgentUtils.spawnAgent("Spider", false, currentTile, motherAgent);
		doDelete();
	}
	
	@Override
	public void takeDamage(int damage, BaseAgent attacker) {
		if(!isAlive()) {
			return;	
		}
		
		isDead = true;
		//System.out.println(this.getLocalName() + " dies");
		if(attacker.isAlive()) {
			attacker.addToLog("I destroyed " + this.getDisplayName());
		}
		addToLog("I died");
		
		currentTile.deleteAgent();
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
		agentImageName = "agent_spider_egg.png";
	}
}
