package agents;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

import javax.imageio.ImageIO;

import org.semanticweb.owlapi.model.OWLLiteral;

import agents.behaviours.CombatBehaviour;
import agents.behaviours.CraftingBehaviour;
import agents.behaviours.ExplorationBehaviour;
import agents.behaviours.FleeingBehaviour;
import agents.behaviours.GatherCollectibleBehaviour;
import agents.behaviours.IntentSenderHeroBehaviour;
import agents.behaviours.MessageProcessingBehaviour;
import agents.behaviours.TradeInitiatorBehaviour;
import agents.heroAgents.BaseHeroAgent;
import agents.heroAgents.CityAgent;
import agents.items.equipment.Equipment;
import agents.items.equipment.Equipment.EquipmentType;
import agents.items.potions.BasePotion.PotionType;
import agents.items.potions.HealthPotion;
import agents.monsterAgents.BaseMonsterAgent;
import agents.items.potions.FleeingPotion;
import agents.items.HeroInventory;
import agents.items.BaseItem;
import agents.items.BaseItem.ItemQuality;
import collectiables.CollectableLoot;
import collectiables.chests.BaseChest.ChestType;
import collectiables.materials.BaseMaterial.MaterialType;
import jade.core.Agent;
import ontology.GameOntology;
import saveData.TileSaveData;
import tiles.Tile;
import ui.StatusEffectImagesUtils;
import tiles.Tile.TileType;

public abstract class BaseEntityAgent extends BaseAgent{
	protected transient Tile oldTile;
	
	protected transient ArrayList<Tile> movableToTiles;
	
	protected int forgetCombatTargetTimer;
	
	protected transient Stack<Tile> pathToTile;
	
	protected boolean alreadyCommunicated = false;
	
	protected int currentXP = 0;
	protected int neededXP = 2;
	protected int baseMaxHealth;
	
	
	public enum StatusEffect{
		QUICK,
		REGENARATION,
		POISON,
		FROZEN,
		BURNING
	}
	
	protected boolean movedTwice;
	
	protected HashMap<StatusEffect, Integer> statusEffects;
	
	public enum RelationshipType{
		ALLIES,
		FRIENDLY,
		NEUTRAL,
		UNFRIENDLY,
		ENEMIES
	}
	
	public enum BehaviourType{
		IDLE,
		EXPLORATION,
		GATHER_MATERIAL,
		COMBAT,
		FLEEING,
		CRAFTING,
		TRADING,
		TRADING_IDLE,
		STRUCTURE_BUILDING,
		SPECIAL
	}
	protected BehaviourType behaviour;
	protected BehaviourType oldBehaviour;
	
	protected transient BaseStructureAgent spawnStructure;
	protected String spawnerName = "";
	
	protected transient ArrayList<BaseStructureAgent> ownedStructures;
	
	public BaseEntityAgent() {
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
		super.setup();

		Object[] args = getArguments();
		spawnerName = args[0].toString();
		
		if(!spawnerName.contentEquals("null") && AgentUtils.getAgentByLocalName(spawnerName) != null) {
			if(AgentUtils.getAgentByLocalName(spawnerName).isStructureAgent()) {
				spawnStructure = (BaseStructureAgent) AgentUtils.getAgentByLocalName(spawnerName);
			}
		}
		
		AgentUtils.getSpawnTile(spawnerName).setAgent(this);
		this.setCurrentTile(AgentUtils.getSpawnTile(spawnerName));
		
		statusEffects = new HashMap<>();
		ownedStructures = new ArrayList<>();
	}

	protected void addCombatBehaviour() {
		calculatePathToTile(combatTargetAgent.getCurrentTile(), currentTile);
		addBehaviour(new CombatBehaviour(this, combatTargetAgent));
	}

	public void perceiveSurroundings() {
		perceivedTiles = currentTile.getNeighbours();
		
		if(currentTile.getTileType() == TileType.GRASS || currentTile.getTileType() == TileType.SAND || currentTile.getTileType() == TileType.SNOW
				|| currentTile.getTileType() == TileType.BURNING_SAND || currentTile.getTileType() == TileType.ICE) {
			ArrayList<Tile> blockedFromPerceptionTiles = new ArrayList<>();
			for (Tile tile : perceivedTiles) {
				if(tile.getTileType() == TileType.MOUNTAIN || tile.getTileType() == TileType.FOREST || tile.getTileType() == TileType.MUSHROOM) {
					for (Tile t : tile.getNeighboursOppositeOfTile(currentTile)) {
						if(!blockedFromPerceptionTiles.contains(t)) {
							blockedFromPerceptionTiles.add(t);
						}
					}
				}
			}
			
			for(int i=0; i<currentTile.getNeighbours().size(); i++) {
				ArrayList<Tile> tempTiles = perceivedTiles.get(i).getNeighbours();
				for(Tile t : tempTiles) {
					if(!perceivedTiles.contains(t) && !blockedFromPerceptionTiles.contains(t)) {
						perceivedTiles.add(t);
					}
				}
			}
		}
		
		movableToTiles = currentTile.getNeighbours();
		ArrayList<Tile> tempTiles = new ArrayList<>();
		for (int i = movableToTiles.size()-1; i >= 0; i--) {
			if(!movableToTiles.get(i).canAgentMoveToTile(this)) {
				tempTiles.add(movableToTiles.get(i));
			}
		}
		
		for (Tile tile : tempTiles) {
			movableToTiles.remove(tile);
		}
		
		
	}
	
	public void calculatePathToTile(Tile startTile, Tile endTile) {
		pathToTile = new Stack<>();

		Set<Tile> alreadyVisited = new HashSet<>();
		HashMap<Tile, Tile> connections = new HashMap<>();
		
		Queue<Tile> queue = new ArrayDeque<>();
		queue.add(startTile);

		Tile currentTile = null;
		int counter = 0;
		
		boolean foundPath = false;
        while (!queue.isEmpty()) {
            currentTile = queue.remove();
            counter++;
            if(currentTile.equals(endTile)) {
            	foundPath = true;
            	break;
            }
            else {
            	if(counter > 1000) {
            		return;
            	}
            	
            	alreadyVisited.add(currentTile);
            	for (Tile tile : currentTile.getNeighbours()) {
					if(tile.canAgentMoveToTile(this) && !alreadyVisited.contains(tile) && !queue.contains(tile)) {
						queue.add(tile);
						if(!connections.containsKey(tile)) {
							connections.put(tile, currentTile);
						}
					}
				}
            }
        }
        if(!foundPath) {
        	return;
        }
        
        Stack<Tile> reversedPath = new Stack<>();
        
        while(!currentTile.equals(startTile)) {
        	currentTile = connections.get(currentTile);
        	reversedPath.add(currentTile);
        }
        
        while(!reversedPath.empty()) {
        	pathToTile.add(reversedPath.pop());
        }
	}
	
	public boolean doesPathToTileExist(Tile startTile, Tile endTile) {
		Set<Tile> alreadyVisited = new HashSet<>();
		
		Queue<Tile> queue = new ArrayDeque<>();
		queue.add(startTile);

		Tile currentTile = null;
		int counter = 0;
		
        while (!queue.isEmpty()) {
            currentTile = queue.remove();
            counter++;
            if(currentTile.equals(endTile)) {
            	return true;
            }
            else {
            	if(counter > 1000) {
            		return false;
            	}
            	
            	alreadyVisited.add(currentTile);
            	for (Tile tile : currentTile.getNeighbours()) {
            		if(tile.equals(endTile)) {
            			return true;
            		}
            		else if(tile.canAgentMoveToTile(this) && !alreadyVisited.contains(tile) && !queue.contains(tile)) {
						queue.add(tile);
					}
				}
            }
        }
        return false;
	}
	
	public void perceiveNearbyAgents() {
		perceivedAgents = new ArrayList<>();
		
		for (Tile tile : perceivedTiles) {
			BaseAgent agent = tile.getAgent();
			if(agent != null && agent.isAlive() && !agent.equals(this)) {
				perceivedAgents.add(agent);
				this.addToLog("I see " + agent.getDisplayName());
				
				if(getRelationshipWithAgent(agent) == null) {
					if(this.isHeroFactionAgent() && agent.isHeroFactionAgent()) {
						if(agent.isStructureAgent()) {
							if(((BaseStructureAgent) agent).getOwner() != null) {
								setRelationshipWithAgent(((BaseStructureAgent) agent).getOwner().getLocalName(),
										((BaseStructureAgent) agent).getOwner().getDisplayName(), RelationshipType.NEUTRAL);
							}
						}
						else {
							setRelationshipWithAgent(agent.getLocalName(), agent.getDisplayName(), RelationshipType.NEUTRAL);
						}
					}
					else if(this.isMonsterFactionAgent() && agent.isMonsterFactionAgent()) {
						if(agent.isStructureAgent()) {
							if(((BaseStructureAgent) agent).getOwner() != null) {
								setRelationshipWithAgent(((BaseStructureAgent) agent).getOwner().getLocalName(),
										((BaseStructureAgent) agent).getOwner().getDisplayName(), RelationshipType.NEUTRAL);
							}
						}
						else {
							BaseMonsterAgent monsterAgent1 = (BaseMonsterAgent) this;
							BaseMonsterAgent monsterAgent2 = (BaseMonsterAgent) agent;
							if(monsterAgent1.isBossMonster() && monsterAgent2.isBossMonster()) {
								setRelationshipWithAgent(agent.getLocalName(), agent.getDisplayName(), RelationshipType.ENEMIES);
							}
							else if(monsterAgent1.isBossMonster() || monsterAgent2.isBossMonster()) {
								setRelationshipWithAgent(agent.getLocalName(), agent.getDisplayName(), RelationshipType.ALLIES);
							}
							else {
								setRelationshipWithAgent(agent.getLocalName(), agent.getDisplayName(), RelationshipType.NEUTRAL);
							}
						}
					}
					else {
						if(agent.isStructureAgent()) {
							if(((BaseStructureAgent) agent).getOwner() != null) {
								setRelationshipWithAgent(((BaseStructureAgent) agent).getOwner().getLocalName(),
										((BaseStructureAgent) agent).getOwner().getDisplayName(), RelationshipType.ENEMIES);
							}
						}
						else {
							setRelationshipWithAgent(agent.getLocalName(), agent.getDisplayName(), RelationshipType.ENEMIES);
						}
					}
				}
			}
		}
		
		perceivedAgents.remove(this);
	}
	
	@Override
	protected void updateAgentsInCombatWith() {
		for(int i = agentsInCombatWith.size()-1; i>=0; i--){
			if(agentsInCombatWith.get(i) == null || !agentsInCombatWith.get(i).isAlive()) {
				agentsInCombatWith.remove(i);
			}
		}
		
		if(combatTargetAgent!= null && !combatTargetAgent.isAlive()) {
			agentsInCombatWith.remove(combatTargetAgent);
			combatTargetAgent = null;
			forgetCombatTargetTimer = 5;
		}
		
		if(agentsInCombatWith.isEmpty()) {
			combatTargetAgent = null;
			forgetCombatTargetTimer = 5;
		}
		else if(combatTargetAgent != null && !this.equals(combatTargetAgent.getCombatTargetAgent())) {
			
			ArrayList<BaseAgent> agentsThatAreAttackingMe = new ArrayList<>();
			for (BaseAgent agent : agentsInCombatWith) {
				if(this.equals(agent.getCombatTargetAgent())){
					agentsThatAreAttackingMe.add(agent);
				}
			}
			
			if(!agentsThatAreAttackingMe.isEmpty()) {
				Random random = new Random();
				int index = random.nextInt(agentsThatAreAttackingMe.size());
				combatTargetAgent = agentsThatAreAttackingMe.get(index);
				this.addToLog("I'm fighting " + combatTargetAgent.getDisplayName());
				forgetCombatTargetTimer = 5;
			}
			else {
				combatTargetAgent = null;
				forgetCombatTargetTimer = 5;
			}
			
		}
		else if(combatTargetAgent == null || !agentsInCombatWith.contains(combatTargetAgent)) {
			Random random = new Random();
			int index = random.nextInt(agentsInCombatWith.size());
			combatTargetAgent = agentsInCombatWith.get(index);
			this.addToLog("I'm fighting " + combatTargetAgent.getDisplayName());
			forgetCombatTargetTimer = 5;
		}
		else if(forgetCombatTargetTimer > 0) {
			forgetCombatTargetTimer--;
		}
		else if(forgetCombatTargetTimer == 0) {
			agentsInCombatWith.remove(combatTargetAgent);
			combatTargetAgent = null;
			if(!agentsInCombatWith.isEmpty()) {
				Random random = new Random();
				int index = random.nextInt(agentsInCombatWith.size());
				combatTargetAgent = agentsInCombatWith.get(index);
				this.addToLog("I'm fighting " + combatTargetAgent.getDisplayName());
				forgetCombatTargetTimer = 5;
			}
		}
	}
	
	protected boolean isTargetedByOtherAgents() {
		System.out.println(getLocalName() + ": In combat with - " + agentsInCombatWith.size());
		
		for (BaseAgent agent : agentsInCombatWith) {
			if(this.equals(agent.combatTargetAgent)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	protected void regenarateHealth() {
		if(behaviour != BehaviourType.COMBAT && behaviour != BehaviourType.FLEEING && currentHealth < maxHealth 
				&& currentTile.getTileType() != TileType.BURNING_SAND) {
			int regenaratedHealth = Math.round(maxHealth * 0.03f);
			currentHealth = Math.min(currentHealth + regenaratedHealth, maxHealth);
		}
		
		if(hasStatusEffect(StatusEffect.REGENARATION)) {
			int regenaratedHealth = Math.round(maxHealth * 0.1f);
			currentHealth = Math.min(currentHealth + regenaratedHealth, maxHealth);
		}
	}
	
	protected void looseHealth() {
		if(hasStatusEffect(StatusEffect.POISON)) {
			int lostHealth = (int) Math.ceil(getMaxHealth() * 0.04f);
			currentHealth = Math.max(0, currentHealth - lostHealth);
		}
		
		if(hasStatusEffect(StatusEffect.BURNING)) {
			int lostHealth = (int) Math.ceil(getMaxHealth() * 0.06f);
			currentHealth = Math.max(0, currentHealth - lostHealth);
		}
		
		if(currentHealth<0) {
			isDead = true;
			//System.out.println(this.getLocalName() + " dies");
			addToLog("I died");
			currentTile.deleteAgent();
		}
	}
	
	public void takeDamage(int damage, BaseAgent attacker) {
		if(!isAlive()) {
			return;	
		}
		
		Random random = new Random();
		int chance = random.nextInt(100)+1;
		if(chance <= evasionChance) {
			//System.out.println(this.getLocalName() + " evades the attack");
			addToLog("I evaded " + attacker.getDisplayName() + "'s attack");
			attacker.addToLog("I attacked " + this.getLocalName() + " but they evaded it" );
			return;
		}
		
		chance = random.nextInt(100)+1;
		if(chance <= attacker.getCritChance()) {
			currentHealth -= damage*2;
			//System.out.println("CRITICAL HIT! " + this.getLocalName() + " takes " + damage*2 + " damage");
			addToLog("I took " + damage*2 + " damage from " + attacker.getLocalName());
			attacker.addToLog("I attacked " + this.getDisplayName() + " for " + damage*2 + " damage");
		}
		else {
			currentHealth -= damage;
			//System.out.println(this.getLocalName() + " takes " + damage + " damage");
			addToLog("I took " + damage + " damage from " + attacker.getLocalName());
			attacker.addToLog("I attacked " + this.getDisplayName() + " for " + damage + " damage");
		}
		
		if(attacker.isEntityAgent()) {
			((BaseEntityAgent)attacker).onDealtDamage(this);
		}
		
		if(currentHealth<0) {
			
			if(didDefyDeath()) {
				return;
			}
			
			isDead = true;
			//System.out.println(this.getLocalName() + " dies");
			if(attacker.isAlive()) {
				attacker.gainXP(getDroppedXP());
				attacker.addToLog("I killed " + this.getDisplayName());
				attacker.collectLoot(extractOwnLoot());
			}
			addToLog("I died");
			onDeath();
			currentTile.deleteAgent();
		}
	}
	
	public void onDealtDamage(BaseAgent target) {
		
	}
	
	@Override
	protected CollectableLoot extractOwnLoot() {
		return null;
	}
	
	@Override
	protected void onDeath() {
		
	}
	
	protected boolean didDefyDeath() {
		return false;
	}

	public void takeHeatDamage(int damage) {
		if(!isAlive()) {
			return;	
		}
		
		currentHealth -= damage;
		//System.out.println(this.getLocalName() + " takes " + damage + " damage");
		addToLog("I took " + damage + " damage from the heat");
		
		if(currentHealth<0) {
			isDead = true;
			//System.out.println(this.getLocalName() + " dies");
			addToLog("I died from the heat");
			currentTile.deleteAgent();
		}
	}

	public void heal(int restoredHealth) {
		currentHealth = Math.min(maxHealth, currentHealth+restoredHealth);
		addToLog("I healed for " + restoredHealth);
	}
	
	@Override
	public void gainXP(int xp) {
		currentXP += xp;
		if(level < 20 && currentXP >= neededXP) {
			currentXP -= neededXP;
			levelUp();
		}
	}
	
	protected void updateStatusEffects() 
	{
		ArrayList<StatusEffect> statusEffectsForRemoval = new ArrayList<>();
	
		for(StatusEffect effect : statusEffects.keySet()) {
			int newValue = statusEffects.get(effect) - 1;
			statusEffects.put(effect, newValue);
			
			if(newValue == 0) {
				statusEffectsForRemoval.add(effect);
			}
		}
		
		for (StatusEffect statusEffect : statusEffectsForRemoval) {
			statusEffects.remove(statusEffect);
			addToLog("I no longer have the effect " + statusEffect.toString());
		}
	}
	
	public void addStatusEffect(StatusEffect effect, int turnNum) {
		
		if(effect == StatusEffect.FROZEN && hasStatusEffect(StatusEffect.BURNING)) {
			return;
		}
		else if(effect == StatusEffect.BURNING && hasStatusEffect(StatusEffect.FROZEN)) {
			statusEffects.remove(StatusEffect.FROZEN);
		}
		
		int turns = turnNum+1;
		if(statusEffects.containsKey(effect)) {
			turns += statusEffects.get(effect);
		}
		
		statusEffects.put(effect, turns);
		addToLog("I have the effect " + effect.toString());
	}
	
	public boolean hasStatusEffect(StatusEffect effect) {
		return statusEffects.containsKey(effect);
	}
	
	@Override
	public void drawIcon(Graphics g, int x, int y, int width, int height) {
		if(!isAlive()) {
			return;
		}
		
		g.drawImage(this.getImage(), x, y, width, height, null);

		float scale = width/132F;
		
		if(behaviour == BehaviourType.COMBAT || behaviour == BehaviourType.FLEEING) {
			g.setColor(Color.GRAY);
			g.fillRect((int)(x+(28*scale)), y, (int)(76*scale), (int)(5*scale));
			g.setColor(Color.RED);
			g.fillRect((int)(x+(28*scale)), y, (int)(76*currentHealth/maxHealth*scale), (int)(5*scale));
		}
		
		if(statusEffects != null && statusEffects.size() > 0) {
			int counter = 0;
			for (StatusEffect effect : statusEffects.keySet()) {
				int effectX = (int)(x+28*scale + counter*20*scale);
				int effectY = (int)(y + 7*scale);
				g.drawImage(StatusEffectImagesUtils.getStatusEffectImage(effect), effectX, effectY, (int)(15*scale), (int)(15*scale), null);
				g.setFont(new Font("VCR OSD MONO", 1, (int)(12*scale)));
				g.setColor(new Color(253,231,111,255));
				int turnTextY = (int)(y + 25*scale);
				g.drawString(statusEffects.get(effect)+"", effectX, turnTextY);
				counter++;
			}
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

	public Tile getOldTile() {
		return oldTile;
	}

	public void setOldTile(Tile oldTile) {
		this.oldTile = oldTile;
	}
	
	public ArrayList<Tile> getPerceivedTiles(){
		return perceivedTiles;
	}
	
	public ArrayList<Tile> getMovableToTiles(){
		return movableToTiles;
	}

	public Stack<Tile> getPathToTile() {
		return pathToTile;
	}

	public void setPathToTile(Stack<Tile> pathToTile) {
		this.pathToTile = pathToTile;
	}

	public BehaviourType getBehaviour() {
		return behaviour;
	}

	public void setBehaviour(BehaviourType behaviour) {
		if(this.behaviour != behaviour) {
			oldBehaviour = this.behaviour;
			this.behaviour = behaviour;
			System.out.println(getLocalName() + ": " + behaviour);

			addToLog("My behaviour is " + behaviour.toString());
		}
	}

	public boolean isAlreadyCommunicated() {
		return alreadyCommunicated;
	}

	public void setAlreadyCommunicated(boolean alreadyCommunicated) {
		this.alreadyCommunicated = alreadyCommunicated;
	}
	
	public int getBaseAttack() {
		return attack;
	}
	
	public boolean hasMovedTwice() {
		return movedTwice;
	}

	public void setMovedTwice(boolean movedTwice) {
		this.movedTwice = movedTwice;
	}
	
	@Override
	public boolean isAlive() {
		if(isDead) {
			return false;
		}
		else {
			return super.isAlive();
		}
	}
	
	public void lowerRelationshipWithAgent(String name, String displayName) {
		RelationshipType relation = GameOntology.getRelationshipWithAgent(this.getLocalName(), name);
		
		switch (relation) {
		case ALLIES: {
			GameOntology.setRelationshipBetweenAgents(getLocalName(), name, RelationshipType.FRIENDLY);
			addToLog("Me and " + displayName + " are Friendly");
			break;
		}
		case FRIENDLY: {
			GameOntology.setRelationshipBetweenAgents(getLocalName(), name, RelationshipType.NEUTRAL);
			addToLog("Me and " + displayName + " are Neutral");
			break;
		}
		case NEUTRAL: {
			GameOntology.setRelationshipBetweenAgents(getLocalName(), name, RelationshipType.UNFRIENDLY);
			addToLog("Me and " + displayName + " are Unfriendly");
			break;
		}
		case UNFRIENDLY: {
			GameOntology.setRelationshipBetweenAgents(getLocalName(), name, RelationshipType.ENEMIES);
			addToLog("Me and " + displayName + " are Enemies");
			break;
		}
		default:
		}
	}
	
	public void improveRelationshipWithAgent(String name, String displayName) {
		RelationshipType relation = GameOntology.getRelationshipWithAgent(this.getLocalName(), name);
		
		switch (relation) {
		case ENEMIES: {
			GameOntology.setRelationshipBetweenAgents(getLocalName(), name, RelationshipType.UNFRIENDLY);
			addToLog("Me and " + displayName + " are Unfriendly");
			break;
		}
		case UNFRIENDLY: {
			GameOntology.setRelationshipBetweenAgents(getLocalName(), name, RelationshipType.NEUTRAL);
			addToLog("Me and " + displayName + " are Neutral");
			break;
		}
		case NEUTRAL: {
			GameOntology.setRelationshipBetweenAgents(getLocalName(), name, RelationshipType.FRIENDLY);
			addToLog("Me and " + displayName + " are Friendly");
			break;
		}
		case FRIENDLY: {
			GameOntology.setRelationshipBetweenAgents(getLocalName(), name, RelationshipType.ALLIES);
			addToLog("Me and " + displayName + " are Allies");
			break;
		}
		default:
		}
	}
	
	@Override
	public void setRelationshipWithAgent(String name, String displayName, RelationshipType relation) {
		this.addToLog("Me and " + displayName + " are now " + relation);
		GameOntology.setRelationshipBetweenAgents(this.getLocalName(), name, relation);
	}

	public abstract String getStringOfTraits();

	public void addOwnedStructure(BaseStructureAgent structure) {
		ownedStructures.add(structure);
	}

	public void removeOwnedStructure(BaseStructureAgent structure) {
		ownedStructures.remove(structure);
	}
	
	protected boolean canBuildStructure() {
		if(level >= 10 && movableToTiles.size() > 1 && ownedStructures.isEmpty() && canBuildStructureOnNeighboringTile()) {
			
			for(Tile tile : perceivedTiles) {
				if(tile.getOwningCity() != null) {
					return false;
				}
			}
			
			return true;
		}
		return false;
	}
	
	public BaseStructureAgent getSpawnStructure() {
		return spawnStructure;
	}
	
	protected boolean canBuildStructureOnNeighboringTile() {
		for (Tile tile : movableToTiles) {
			if(tile.getOwningCity() == null) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public RelationshipType getRelationshipWithAgent(BaseAgent agent) {
		if(agent.isEntityAgent()) {
			return GameOntology.getRelationshipWithAgent(getLocalName(), agent.getLocalName());
		}
		
		if(((BaseStructureAgent) agent).getOwner() == null) {
			return null;
		}
		
		if(ownedStructures.contains(agent)){
			return RelationshipType.ALLIES;
		}

		return GameOntology.getRelationshipWithAgent(getLocalName(), ((BaseStructureAgent) agent).getOwner().getLocalName());
	}

	public ArrayList<BaseStructureAgent> getOwnedStructures() {
		return ownedStructures;
	}

	public int getBaseMaxHealth() {
		return maxHealth;
	}

	public ArrayList<String> getOwnedStructureNames() {
		ArrayList<String> names = new ArrayList<>();
		
		for(BaseStructureAgent structureAgent : ownedStructures) {
			names.add(structureAgent.getLocalName());
		}
		
		return names;
	}
	
	@Override
	public void loadAgent(TileSaveData save) {
		super.loadAgent(save);
		
		spawnStructure = (BaseStructureAgent) AgentUtils.getAgentByLocalName(save.getSpawnStructureName());
		
		for (String name : save.getOwnedStructureNames()) {
			ownedStructures.add((BaseStructureAgent) AgentUtils.getAgentByLocalName(name));
		}
	}

	public int getStatusEffectDuration(StatusEffect status) {
		return statusEffects.get(status);
	}
	public ArrayList<StatusEffect> getStatusEffects() {
		ArrayList<StatusEffect> statuses = new ArrayList<>();
		
		for (StatusEffect statusEffect : statusEffects.keySet()) {
			statuses.add(statusEffect);
		}
		
		return statuses;
	}
}
