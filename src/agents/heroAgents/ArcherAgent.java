package agents.heroAgents;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

import agents.BaseEntityAgent.BehaviourType;
import agents.behaviours.CombatArcherBehaviour;
import agents.behaviours.CombatBehaviour;
import agents.behaviours.CraftingBehaviour;
import agents.behaviours.ExplorationBehaviour;
import agents.behaviours.FleeingBehaviour;
import agents.behaviours.GatherCollectibleBehaviour;
import agents.behaviours.IntentSenderHeroBehaviour;
import agents.behaviours.TradeInitiatorBehaviour;
import agents.heroAgents.BaseHeroAgent.HeroPersonalityTrait;
import agents.items.BaseItem.ItemQuality;
import agents.items.equipment.Equipment;
import agents.items.equipment.Equipment.EquipmentType;
import collectiables.materials.BaseMaterial.MaterialType;
import ontology.GameOntology;
import saveData.TileSaveData;

public class ArcherAgent extends BaseHeroAgent{
	
	private transient static BufferedImage agentImage;
	
	public ArcherAgent() {
		super();
		agentImageName = "agent_archer.png";
	}
	
	@Override
	protected void setup() {
		if(agentWasLoaded) {
			return;
		}
		
		heroType = HeroType.ARCHER;
		
		Random random = new Random();
		
		baseMaxHealth = random.nextInt(16)+70;
		currentHealth = baseMaxHealth;
		maxHealth = baseMaxHealth;
		attack = random.nextInt(7)+16;
		evasionChance = 10;
		critChance = 7;
		
		super.setup();
	}
	
	@Override
	protected void addCombatBehaviour() {
		calculatePathToTile(combatTargetAgent.getCurrentTile(), currentTile);
		addBehaviour(new CombatArcherBehaviour(this, combatTargetAgent));
	}
	
	@Override
	protected void addStartingItems() {
		Random random = new Random();
		int chance = random.nextInt(10);
		ItemQuality quality = ItemQuality.POOR;
		if(chance == 0) {
			quality = ItemQuality.GREAT;
		}
		else if(chance <= 3) {
			quality = ItemQuality.AVERAGE;
		}
		
		if(personalityTrait == HeroPersonalityTrait.BLOODTHIRSTY){
			quality = ItemQuality.values()[quality.ordinal()+1];
		}
		
		Equipment equipment = new Equipment(MaterialType.STONE, quality, EquipmentType.BOW);
		inventory.addEquipment(equipment);
		inventory.equipBestEquipment();
		
		super.addStartingItems();
	}
	
	@Override
	protected void levelUp() {
		int oldLevel = level;
		int oldHealth = baseMaxHealth;
		int oldAttack = attack;
		
		Random random = new Random();
		int healthIncrease = Math.round(baseMaxHealth * (random.nextInt(7)+8) * 0.01f);
		baseMaxHealth += healthIncrease;
		maxHealth = baseMaxHealth + inventory.getHealthBonusFromEquipment();
		currentHealth += healthIncrease;
		attack += Math.round(attack * (random.nextInt(7)+8) * 0.01f);
		level += 1;
		
		//System.out.println("LEVEL UP: " + getLocalName() + " is now level " + level);
		addToLog("I leveled up to level " + level);
		
		GameOntology.updateEntityAgentStats(this, oldLevel, oldHealth, oldAttack);
		
		neededXP = (int) Math.pow(2, level);
		gainXP(0);
		
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
		agentImageName = "agent_archer.png";
	}
}
