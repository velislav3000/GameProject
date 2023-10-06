package agents;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import agents.BaseEntityAgent.RelationshipType;
import agents.behaviours.BehaviourLoader;
import agents.heroAgents.BaseHeroAgent;
import agents.heroAgents.CityAgent;
import agents.monsterAgents.BaseMonsterAgent;
import agents.monsterAgents.BaseMonsterStructureAgent;
import collectiables.CollectableLoot;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import ontology.GameOntology;
import saveData.TileSaveData;
import tiles.Tile;

public abstract class BaseAgent extends Agent{
	
	protected transient boolean agentWasLoaded;
	
	protected String displayName;
	
	protected transient Tile currentTile;
	
	protected transient ArrayList<Tile> perceivedTiles;
	
	protected transient ArrayList<BaseAgent> perceivedAgents;
	protected transient ArrayList<BaseAgent> agentsInCombatWith;
	protected transient BaseAgent combatTargetAgent;
	
	protected int level = 1;
	protected int maxHealth;
	protected int currentHealth;
	protected int attack;
	protected int critChance;
	protected int evasionChance;
	
	protected boolean isDead = false;
	
	protected boolean isPaused;
	
	protected static String agentImagePath = "";
	protected static BufferedImage agentImage;
	protected static String agentImageName = "agent_temp.png";
	
	protected String log = "";
	
	protected int callAlliesCooldown = 0;
	
	protected transient ArrayList<Behaviour> behavioursToSave = new ArrayList<>();
	
	public BaseAgent() {
		if(agentImagePath.isEmpty()) {
			try {
				agentImagePath = (new File(".").getCanonicalPath() + "/res/agentIcons");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	protected void setup() {
		perceivedAgents = new ArrayList<>();
		agentsInCombatWith = new ArrayList<>();
		
		displayName = getLocalName();
	}

	public abstract void update();
	
	protected abstract void updateAgentsInCombatWith();
	
	protected abstract void regenarateHealth();
	
	public abstract void takeDamage(int damage, BaseAgent attacker);
	
	protected abstract CollectableLoot extractOwnLoot();
	
	protected abstract void onDeath();
	
	public abstract void gainXP(int xp);
	
	protected abstract void levelUp();
	
	public abstract void drawIcon(Graphics g, int x, int y, int width, int height);

	public boolean isEntityAgent() {
		return BaseEntityAgent.class.isAssignableFrom(this.getClass());
	}
	
	public boolean isStructureAgent() {
		return BaseStructureAgent.class.isAssignableFrom(this.getClass());
	}
	
	public boolean isHeroAgent() {
		return BaseHeroAgent.class.isAssignableFrom(this.getClass());
	}
	
	public boolean isMonsterAgent() {
		return BaseMonsterAgent.class.isAssignableFrom(this.getClass());
	}
	
	public boolean isHeroStructureAgent() {
		return CityAgent.class.isAssignableFrom(this.getClass());
	}
	
	public boolean isMonsterStructureAgent() {
		return BaseMonsterStructureAgent.class.isAssignableFrom(this.getClass());
	}
	
	public boolean isHeroFactionAgent() {
		return isHeroAgent() || isHeroStructureAgent();
	}
	
	public boolean isMonsterFactionAgent() {
		return isMonsterAgent() || isMonsterStructureAgent();
	}

	public Tile getCurrentTile() {
		return currentTile;
	}

	public void setCurrentTile(Tile currentTile) {
		this.currentTile = currentTile;
	}
	
	public BaseAgent getCombatTargetAgent() {
		return combatTargetAgent;
	}

	public void setCombatTargetAgent(BaseEntityAgent combatTargetAgent) {
		this.combatTargetAgent = combatTargetAgent;
	}
	
	public ArrayList<BaseAgent> getAgentsInCombatWith() {
		return agentsInCombatWith;
	}

	public void setAgentsInCombatWith(ArrayList<BaseAgent> agentsInCombatWith) {
		this.agentsInCombatWith = agentsInCombatWith;
	}
	
	public int getMaxHealth() {
		return maxHealth;
	}
	
	public int getCurrentHealth() {
		return currentHealth;
	}

	public int getAttack() {
		return attack;
	}

	public int getCritChance() {
		return critChance;
	}

	public int getEvasionChance() {
		return evasionChance;
	}

	public int getLevel() {
		return level;
	}
	
	public boolean getPaused() {
		return isPaused;
	}
	
	public void setPaused(boolean isPaused) {
		this.isPaused = isPaused;
	}
	
	public String getLog() {
		return log;
	}
	
	public void addToLog(String text) {
		if(log.contentEquals("")) {
			log = text;
		}
		else {
			log += "\n" + text;
		}
	}
	
	public int getCallAlliesCooldown() {
		return callAlliesCooldown;
	}
	
	public void setCallAlliesCooldown(int value) {
		callAlliesCooldown = value;
	}
	
	public abstract RelationshipType getRelationshipWithAgent(BaseAgent agent);

	public abstract void setRelationshipWithAgent(String name, String displayName, RelationshipType relation);

	public abstract void collectLoot(CollectableLoot loot); 
	
	@Override
	public void doDelete() {
		GameOntology.removeObjectFromOntology(getLocalName());
		
		super.doDelete();
	}

	public boolean shouldBeRemoved() {
		return !isAlive();
	}

	public int getDroppedXP() {
		return (int) Math.pow(2, level-1);
	}

	public ArrayList<Behaviour> getBehaviours() {
		return behavioursToSave;
	}
	
	@Override
	public void addBehaviour(Behaviour behaviour) {
		if(behaviour instanceof BehaviourLoader) {
			behavioursToSave.add(behaviour);
		}
		super.addBehaviour(behaviour);
	}
	
	@Override
	public void removeBehaviour(Behaviour behaviour) {
		super.removeBehaviour(behaviour);
	}
	
	public ArrayList<String> getPerceivedAgentNames(){
		ArrayList<String> names = new ArrayList<>();
		
		for (BaseAgent agent : perceivedAgents) {
			names.add(agent.getLocalName());
		}
		
		return names;
	}

	public ArrayList<String> getAgentsInCombatWithNames() {
		ArrayList<String> names = new ArrayList<>();
		
		for (BaseAgent agent : agentsInCombatWith) {
			names.add(agent.getLocalName());
		}
		
		return names;
	}
	
	public void loadAgent(TileSaveData save) {
		perceivedAgents = new ArrayList<>();
		agentsInCombatWith = new ArrayList<>();
		behavioursToSave = save.getBehaviours();
		
		for (String name : save.getAgentsInCombatWithNames()) {
			agentsInCombatWith.add(AgentUtils.getAgentByLocalName(name));
		}
		
		combatTargetAgent = AgentUtils.getAgentByLocalName(save.getCombatTargetAgentName());
		
		for (Behaviour b : save.getBehaviours()) {
			if(b instanceof BehaviourLoader) {
				((BehaviourLoader)b).loadOwner(this);
			}
		}
		
		agentWasLoaded = true;
		
		try {
			AgentController agc = getContainerController().getAgent(getLocalName());
			agc.start();
		} catch (ControllerException e) {
			e.printStackTrace();
		}
		
		if(agentImagePath.isEmpty()) {
			try {
				agentImagePath = (new File(".").getCanonicalPath() + "/res/agentIcons");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		isPaused = false;
		
		if(!AgentUtils.agentsArray.contains(this)) {
			AgentUtils.agentsArray.add(this);
		}
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	public abstract BufferedImage getImage();
}
