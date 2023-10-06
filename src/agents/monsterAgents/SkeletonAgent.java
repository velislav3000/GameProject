package agents.monsterAgents;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

import javax.imageio.ImageIO;

import agents.BaseAgent;
import agents.BaseEntityAgent.BehaviourType;
import agents.behaviours.CombatBehaviour;
import agents.behaviours.CombatSkeletonBossBehaviour;
import agents.behaviours.MessageProcessingBehaviour;
import agents.behaviours.MessageProcessingSkeletonBehaviour;
import collectiables.CollectableLoot;
import collectiables.chests.BaseChest;
import collectiables.chests.GoldChest;
import collectiables.chests.IronChest;
import collectiables.chests.WoodenChest;
import ontology.GameOntology;
import saveData.TileSaveData;

public class SkeletonAgent extends BaseMonsterAgent{
	
	private transient static BufferedImage agentImage;
	private static String agentBossImageName;
	private transient static BufferedImage agentBossImage;
	private static String falseDeathAgentImageName;
	private transient static BufferedImage falseDeathAgentImage;
	private int resurectionCooldown;
	
	public SkeletonAgent() {
		super();
		agentImageName = "agent_skeleton.png";
		agentBossImageName = "agent_skeleton_boss.png";
		falseDeathAgentImageName = "agent_skeleton_skull.png";
	}
	
	@Override
	protected void setup() {
		if(agentWasLoaded) {
			return;
		}
		
		monsterType = MonsterType.SKELETON;
		
		Random random = new Random();
		
		baseMaxHealth = random.nextInt(16)+50;
		currentHealth = baseMaxHealth;
		maxHealth = baseMaxHealth;
		attack = random.nextInt(6)+5;
		evasionChance = 5;
		critChance = 5;
		
		baseDroppedGold = 50;
		
		super.setup();
	}
	
	@Override
	protected void addMessageProcessingBehavior() {
		this.addBehaviour(new MessageProcessingSkeletonBehaviour(this));
	}
	
	@Override
	public void update() {
		
		if(behaviour == BehaviourType.SPECIAL) {
			if(resurectionCooldown == 0) {
				isDead = false;
				currentHealth = maxHealth;
				setBehaviour(BehaviourType.IDLE);
				addToLog("I came back to life");
			}
			else {
				addToLog("I'll come back to life in " + resurectionCooldown + " turns");
				resurectionCooldown--;
			}
			return;
		}
		
		super.update();
	}
	
	@Override
	protected void addCombatBehaviour() {
		calculatePathToTile(combatTargetAgent.getCurrentTile(), currentTile);
		if(isBossMonster()) {
			addBehaviour(new CombatSkeletonBossBehaviour(this, combatTargetAgent));
		}
		else {
			addBehaviour(new CombatBehaviour(this, combatTargetAgent));
		}
	}
	
	@Override
	protected void levelUp() {
		int oldLevel = level;
		int oldHealth = baseMaxHealth;
		int oldAttack = attack;
		
		Random random = new Random();
		int healthIncrease = Math.round(baseMaxHealth * (random.nextInt(6)+5) * 0.01f);
		baseMaxHealth += healthIncrease;
		maxHealth = baseMaxHealth;
		currentHealth += healthIncrease;
		attack += Math.round(attack * (random.nextInt(6)+5) * 0.01f);
		level += 1;

		//System.out.println("LEVEL UP: " + getLocalName() + " is now level " + level);
		addToLog("I leveled up to level " + level);
		
		GameOntology.updateEntityAgentStats(this, oldLevel, oldHealth, oldAttack);
		
		neededXP = (int) Math.pow(2, level);
		gainXP(0);
		
	}
	
	@Override
	protected boolean didDefyDeath() {
		Random random = new Random();
		int chance = random.nextInt(5);
		if(chance == 0) {
			resurectionCooldown = 10;
			setBehaviour(BehaviourType.SPECIAL);
			isDead = true;
			return true;
		}
		return false;
	}
	
	@Override
	public boolean shouldBeRemoved() {
		return (!isAlive() && behaviour != BehaviourType.SPECIAL);
	}
	
	@Override
	public void setBehaviour(BehaviourType behaviour) {
		
		if(!isAlive() && this.behaviour == BehaviourType.SPECIAL) {
			return;
		}
		
		super.setBehaviour(behaviour);
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
		if(falseDeathAgentImage == null) {
			try {
				falseDeathAgentImage = ImageIO.read(new File(agentImagePath, falseDeathAgentImageName));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if(behaviour == BehaviourType.SPECIAL) {
			return falseDeathAgentImage;
		}
		if(isBossMonster()) {
			return agentBossImage;
		}
		return agentImage;
	}
	
	@Override
	public void drawIcon(Graphics g, int x, int y, int width, int height) {
		if(!isAlive() && behaviour != BehaviourType.SPECIAL) {
			return;
		}
		
		g.drawImage(this.getImage(), x, y, width, height, null);
		if(behaviour == BehaviourType.COMBAT || behaviour == BehaviourType.FLEEING) {
			float scale = width/132F;
			
			g.setColor(Color.GRAY);
			g.fillRect((int)(x+(28*scale)), y, (int)(76*scale), (int)(5*scale));
			g.setColor(Color.RED);
			g.fillRect((int)(x+(28*scale)), y, (int)(76*currentHealth/maxHealth*scale), (int)(5*scale));
		}
	}
	
	@Override
	public void loadAgent(TileSaveData save) {
		super.loadAgent(save);
		agentImageName = "agent_skeleton.png";
		agentBossImageName = "agent_skeleton_boss.png";
		falseDeathAgentImageName = "agent_skeleton_skull.png";
	}
}
