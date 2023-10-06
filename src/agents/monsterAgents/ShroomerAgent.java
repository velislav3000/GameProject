package agents.monsterAgents;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

import agents.BaseEntityAgent.BehaviourType;
import agents.BaseEntityAgent.StatusEffect;
import agents.behaviours.ExplorationBehaviour;
import agents.behaviours.FleeingBehaviour;
import agents.behaviours.IntentSenderMonsterBehaviour;
import agents.behaviours.ShroomifyTileBehaviour;
import agents.behaviours.StructureBuildingBehaviour;
import agents.monsterAgents.BaseMonsterAgent.MonsterType;
import ontology.GameOntology;
import saveData.TileSaveData;

public class ShroomerAgent extends BaseMonsterAgent{

	private int shroomifyCooldown;
	
	private transient static BufferedImage agentImage;
	private static String agentBossImageName;
	private transient static BufferedImage agentBossImage;
	
	public ShroomerAgent() {
		super();
		agentImageName = "agent_shroomer.png";
		agentBossImageName = "agent_shroomer_boss.png";
	}
	
	@Override
	protected void setup() {
		if(agentWasLoaded) {
			return;
		}
		
		monsterType = MonsterType.SHROOMER;
		
		Random random = new Random();
		
		baseMaxHealth = random.nextInt(16)+15;
		currentHealth = baseMaxHealth;
		maxHealth = baseMaxHealth;
		attack = random.nextInt(5)+6;
		evasionChance = 20;
		critChance = 1;
		
		super.setup();

		if(isBossMonster()) {
			shroomifyCooldown = 7;
		}
		else {
			shroomifyCooldown = 10;
		}
	}
	
	@Override
	public void update() {
		if(!isAlive()) {
			return;
		}
		
		if(!alreadyCommunicated) {
			regenarateHealth();
			looseHealth();

			if(hasStatusEffect(StatusEffect.FROZEN)) {
				updateStatusEffects();
				return;
			}
			
			updateStatusEffects();
			movedTwice = false;

			if(callAlliesCooldown > 0) {
				callAlliesCooldown--;
			}
			
			if(shroomifyCooldown > 0) {
				shroomifyCooldown--;
			}
		}
		
		perceiveSurroundings();
		perceiveNearbyAgents();
		updateAgentsInCombatWith();
		
		if(!perceivedAgents.isEmpty() && alreadyCommunicated == false) {
			alreadyCommunicated = true;
			addBehaviour(new IntentSenderMonsterBehaviour(this, perceivedAgents));
			return;
		}
		
		alreadyCommunicated = false;
		
		if(shroomifyCooldown == 0) {
			setBehaviour(BehaviourType.SPECIAL);
			addBehaviour(new ShroomifyTileBehaviour(this));
			return;
		}
		
		if(behaviour == BehaviourType.COMBAT && combatTargetAgent != null) {
				addCombatBehaviour();
				return;
		}
		
		if(behaviour == BehaviourType.FLEEING && isTargetedByOtherAgents()){
			addBehaviour(new FleeingBehaviour(this));
			return;
		}
		
		if(movableToTiles.isEmpty()) {
			this.setBehaviour(BehaviourType.IDLE);
		}
		else {
			this.setBehaviour(BehaviourType.EXPLORATION);
		}
		
		switch (behaviour) {
		case IDLE: {
			break;
		}
		case EXPLORATION: {
			addBehaviour(new ExplorationBehaviour(this));
			break;
		}
		}
	}
	
	@Override
	protected void levelUp() {
		int oldLevel = level;
		int oldHealth = baseMaxHealth;
		int oldAttack = attack;
		
		Random random = new Random();
		int healthIncrease = Math.round(baseMaxHealth * (random.nextInt(16)+5) * 0.01f);
		baseMaxHealth += healthIncrease;
		maxHealth = baseMaxHealth;
		currentHealth += healthIncrease;
		attack += Math.round(attack * (random.nextInt(4)+2) * 0.01f);
		level += 1;
		
		//System.out.println("LEVEL UP: " + getLocalName() + " is now level " + level);
		addToLog("I leveled up to level " + level);
		
		GameOntology.updateEntityAgentStats(this, oldLevel, oldHealth, oldAttack);
		
		neededXP = (int) Math.pow(2, level);
		gainXP(0);
		
	}
	
	public void setShroomifyCooldown(int shroomifyCooldown) {
		this.shroomifyCooldown = shroomifyCooldown;
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
		if(agentBossImage == null) {
			try {
				agentBossImage = ImageIO.read(new File(agentImagePath, agentBossImageName));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if(isBossMonster()) {
			return agentBossImage;
		}
		return agentImage;
	}
	
	@Override
	public void loadAgent(TileSaveData save) {
		super.loadAgent(save);
		agentImageName = "agent_shroomer.png";
		agentBossImageName = "agent_shroomer_boss.png";
	}
}
