package agents.heroAgents;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

import agents.items.BaseItem.ItemQuality;
import agents.items.equipment.Equipment;
import agents.items.equipment.Equipment.EquipmentType;
import collectiables.materials.BaseMaterial.MaterialType;
import ontology.GameOntology;
import saveData.TileSaveData;

public class WarriorAgent extends BaseHeroAgent{
	
	private transient static BufferedImage agentImage;
	
	public WarriorAgent() {
		super();
		agentImageName = "agent_warrior.png";
	}
	
	@Override
	protected void setup() {
		if(agentWasLoaded) {
			return;
		}
		
		heroType = HeroType.WARRIOR;
		
		Random random = new Random();
		
		baseMaxHealth = random.nextInt(16)+95;
		currentHealth = baseMaxHealth;
		maxHealth = baseMaxHealth;
		attack = random.nextInt(9)+12;
		evasionChance = 5;
		critChance = 5;
		
		super.setup();
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
		
		Equipment equipment = new Equipment(MaterialType.STONE, quality, EquipmentType.SWORD);
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
		int healthIncrease = Math.round(baseMaxHealth * (random.nextInt(5)+11) * 0.01f);
		baseMaxHealth += healthIncrease;
		maxHealth = baseMaxHealth + inventory.getHealthBonusFromEquipment();
		currentHealth += healthIncrease;
		attack += Math.round(attack * (random.nextInt(5)+6) * 0.01f);
		level += 1;

		addToLog("I leveled up to level " + level);
		//System.out.println("LEVEL UP: " + getLocalName() + " is now level " + level);
		
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
		agentImageName = "agent_warrior.png";
	}
}
