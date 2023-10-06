package agents.heroAgents;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

import agents.behaviours.CombatBehaviour;
import agents.behaviours.CombatRogueBehaviour;
import agents.items.BaseItem.ItemQuality;
import agents.items.equipment.Equipment;
import agents.items.equipment.Equipment.EquipmentType;
import collectiables.materials.BaseMaterial.MaterialType;
import ontology.GameOntology;
import saveData.TileSaveData;

public class RogueAgent extends BaseHeroAgent{
	
	private boolean sneakAttackEnabled = false;
	private transient static BufferedImage agentImage;
	
	public RogueAgent() {
		super();
		agentImageName = "agent_rogue.png";
	}
	
	@Override
	protected void setup() {
		if(agentWasLoaded) {
			return;
		}
		
		heroType = HeroType.ROGUE;
		
		Random random = new Random();
		
		baseMaxHealth = random.nextInt(16)+65;
		currentHealth = baseMaxHealth;
		maxHealth = baseMaxHealth;
		attack = random.nextInt(9)+18;
		evasionChance = 15;
		critChance = 15;
		
		super.setup();
	}
	
	@Override
	protected void addStartingItems() {
		Random random = new Random();
		int chance = random.nextInt(10);
		int amount = 1;

		if(chance == 0) {
			amount++;
		}
		
		for(int i = 1; i<= amount; i++) {
			chance = random.nextInt(10);
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
			
			Equipment equipment = new Equipment(MaterialType.STONE, quality, EquipmentType.DAGGER);
			inventory.addEquipment(equipment);
			inventory.equipBestEquipment();
		}
		
		super.addStartingItems();
	}
	
	@Override
	protected void addCombatBehaviour() {
		calculatePathToTile(combatTargetAgent.getCurrentTile(), currentTile);
		addBehaviour(new CombatRogueBehaviour(this, combatTargetAgent));
	}
	
	@Override
	protected void levelUp() {
		int oldLevel = level;
		int oldHealth = baseMaxHealth;
		int oldAttack = attack;
		
		Random random = new Random();
		int healthIncrease = Math.round(baseMaxHealth * (random.nextInt(6)+7) * 0.01f);
		baseMaxHealth += healthIncrease;
		maxHealth = baseMaxHealth + inventory.getHealthBonusFromEquipment();
		currentHealth += healthIncrease;
		attack += Math.round(attack * (random.nextInt(6)+9) * 0.01f);
		level += 1;

		//System.out.println("LEVEL UP: " + getLocalName() + " is now level " + level);
		addToLog("I leveled up to level " + level);
		
		GameOntology.updateEntityAgentStats(this, oldLevel, oldHealth, oldAttack);
		
		neededXP = (int) Math.pow(2, level);
		gainXP(0);
		
	}
	
	@Override
	public void setBehaviour(BehaviourType behaviour) {
		if(this.behaviour != behaviour) {
			oldBehaviour = this.behaviour;
			this.behaviour = behaviour;
			//System.out.println(behaviour);
			
			if(behaviour == BehaviourType.COMBAT) {
				sneakAttackEnabled = true;
			}
		}
	}

	public boolean isSneakAttackEnabled() {
		return sneakAttackEnabled;
	}

	public void setSneakAttackEnabled(boolean sneakAttackEnabled) {
		this.sneakAttackEnabled = sneakAttackEnabled;
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
		agentImageName = "agent_rogue.png";
	}
}
