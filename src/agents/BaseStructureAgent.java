package agents;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

import agents.BaseEntityAgent.BehaviourType;
import agents.BaseEntityAgent.RelationshipType;
import agents.BaseStructureAgent.StructureBehaviourType;
import agents.behaviours.structure.MessageProcessingStructureBehaviour;
import agents.heroAgents.BaseHeroAgent;
import collectiables.CollectableLoot;
import jade.core.Agent;
import ontology.GameOntology;
import saveData.TileSaveData;
import tiles.Tile;

public abstract class BaseStructureAgent extends BaseAgent{
	
	public enum StructureType {
		CITY,
		GRAVEYARD,
		SPIDER_NEST
	}
	
	public enum StructureBehaviourType{
		IDLE,
		SPAWN_UNIT,
		GATHER_MATERIALS,
		CRAFTING,
		COMBAT,
		TRADING
	}
	
	protected StructureBehaviourType behaviour;
	protected StructureBehaviourType oldBehaviour;
	
	protected transient BaseEntityAgent owner;
	
	protected int baseAgentSpawningCooldown;
	protected int agentSpawningCooldown;
	
	public BaseStructureAgent() {
		super();
	}
	
	@Override
	protected void setup() {
		super.setup();
		
		evasionChance = 0;
		critChance = 0;
		
		setAgentSpawningCooldown();
		
		Object[] args = getArguments();
		String ownerName = args[0].toString();
		
		if(!ownerName.contentEquals("null")) {
			owner = (BaseEntityAgent) AgentUtils.getAgentByLocalName(ownerName);
			owner.addOwnedStructure(this);
		}

		AgentUtils.getSpawnTile(ownerName).setAgent(this);
		this.setCurrentTile(AgentUtils.getSpawnTile(ownerName));
		
		GameOntology.addStructureAgent(this);
		
		AgentUtils.agentsArray.add(this);
	}
	
	public void drawIcon(Graphics g, int x, int y, int width, int height) {
		if(!isAlive()) {
			return;
		}
		
		g.drawImage(this.getImage(), x, y, width, height, null);
		if(behaviour == StructureBehaviourType.COMBAT) {
			float scale = width/132F;
			
			g.setColor(Color.GRAY);
			g.fillRect((int)(x+(28*scale)), y, (int)(76*scale), (int)(5*scale));
			g.setColor(Color.RED);
			g.fillRect((int)(x+(28*scale)), y, (int)(76*currentHealth/maxHealth*scale), (int)(5*scale));
		}
	}
	
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

	public BaseEntityAgent getOwner() {
		return (BaseEntityAgent) owner;
	}
	
	@Override
	public void gainXP(int xp) {
		
	}
	
	protected abstract boolean canLevelUp();

	@Override
	protected void regenarateHealth() {
		if(behaviour != StructureBehaviourType.COMBAT) {
			currentHealth = (int) Math.min(maxHealth, currentHealth + maxHealth*0.05);
		}
	}
	
	@Override
	protected void updateAgentsInCombatWith() {
		for(int i = agentsInCombatWith.size()-1; i>=0; i--) {
			BaseAgent agent = agentsInCombatWith.get(i);
			
			if(agent == null || !agent.isAlive()) {
				agentsInCombatWith.remove(agent);
				continue;
			}
			
			boolean isPerceived = false;
			
			if(perceivedTiles == null) {
				return;
			}
			
			for(Tile tile : perceivedTiles) {
				if(agent.equals(tile.getAgent())) {
					isPerceived = true;
					break;
				}
			}
			
			if(!isPerceived) {
				agentsInCombatWith.remove(agent);
			}
		}
	}

	@Override
	public void takeDamage(int damage, BaseAgent attacker) {
		if(!isAlive()) {
			return;	
		}
		
		Random random = new Random();
		int chance = random.nextInt(100)+1;
		if(chance <= attacker.getCritChance()) {
			currentHealth -= damage*2;
			System.out.println("CRITICAL HIT! " + this.getLocalName() + " takes " + damage*2 + " damage");
			addToLog("I took " + damage*2 + " damage from " + attacker.getDisplayName());
			attacker.addToLog("I attacked " + this.getLocalName() + " for " + damage*2 + "damage");
		}
		else {
			currentHealth -= damage;
			System.out.println(this.getLocalName() + " takes " + damage + " damage");
			addToLog("I took " + damage + " damage from " + attacker.getDisplayName());
			attacker.addToLog("I attacked " + this.getLocalName() + " for " + damage + "damage");
		}
		
		if(currentHealth<0) {
			isDead = true;
			System.out.println(this.getLocalName() + " dies");
			if(attacker.isAlive()) {
				attacker.gainXP(getDroppedXP());
				attacker.addToLog("I killed " + this.getDisplayName());
			}
			addToLog("I died");
			currentTile.deleteAgent();
		}
	}

	@Override
	protected CollectableLoot extractOwnLoot() {
		return null;
	}

	@Override
	protected void onDeath() {
		
	}
	
	public StructureBehaviourType getBehaviour() {
		return behaviour;
	}

	public void setBehaviour(StructureBehaviourType behaviour) {
		if(this.behaviour != behaviour) {
			oldBehaviour = this.behaviour;
			this.behaviour = behaviour;
			addToLog("My behaviour is " + behaviour.toString());
		}
	}
	
	public abstract String getSpawnedUnitClassName();
	
	public void setAgentSpawningCooldown() {
		agentSpawningCooldown = baseAgentSpawningCooldown;
	}
	
	public void setAgentSpawningCooldown(int cooldown) {
		agentSpawningCooldown = cooldown;
	}
	
	@Override
	public void setRelationshipWithAgent(String name, String displayName, RelationshipType relation) {
		if(owner != null) {
			owner.addToLog("Me and " + displayName + " are now " + relation);
			GameOntology.setRelationshipBetweenAgents(owner.getLocalName(), name, relation);
		}
	}
	
	@Override
	public void loadAgent(TileSaveData save) {
		super.loadAgent(save);
		
		owner = (BaseEntityAgent) AgentUtils.getAgentByLocalName(save.getOwnerName());
	}
}
