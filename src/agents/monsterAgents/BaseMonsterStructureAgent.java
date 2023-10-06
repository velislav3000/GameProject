package agents.monsterAgents;

import java.util.ArrayList;

import agents.BaseAgent;
import agents.BaseEntityAgent;
import agents.BaseStructureAgent;
import agents.BaseEntityAgent.RelationshipType;
import agents.behaviours.structure.CombatStructureBehaviour;
import agents.behaviours.structure.MessageProcessingStructureBehaviour;
import agents.behaviours.structure.SpawnUnitBehaviour;
import collectiables.CollectableLoot;
import ontology.GameOntology;
import tiles.Tile;
import tiles.Tile.TileType;

public abstract class BaseMonsterStructureAgent extends BaseStructureAgent{

	protected int currentXP;
	protected int neededXP;
	
	public BaseMonsterStructureAgent() {
		super();
		
		addBehaviour(new MessageProcessingStructureBehaviour(this));
	}
	
	@Override
	protected void setup() {
		
		currentXP = 0;
		neededXP = 4;
		
		super.setup();
	}

	@Override
	public void update() {
		regenarateHealth();
		
		if(agentSpawningCooldown > 0) {
			agentSpawningCooldown--;
		}
		
		if(canLevelUp()) {
			levelUp();
			return;
		}
		
		perceiveSurroundings();
		updateAgentsInCombatWith();
		
		if(!agentsInCombatWith.isEmpty()) {
			setBehaviour(StructureBehaviourType.COMBAT);
			this.addBehaviour(new CombatStructureBehaviour(this));
			return;
		}
		
		if(agentSpawningCooldown == 0) {
			setBehaviour(StructureBehaviourType.SPAWN_UNIT);
			this.addBehaviour(new SpawnUnitBehaviour(this));
			return;
		}
		
		setBehaviour(StructureBehaviourType.IDLE);
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
	}
	
	@Override
	protected boolean canLevelUp() {
		if(level == 10) {
			return false;
		}
		
		if(currentXP >= neededXP) {
			return true;
		}
		return false;
	}

	@Override
	public void gainXP(int xp) {
		currentXP += xp;
	}
	
	@Override
	public RelationshipType getRelationshipWithAgent(BaseAgent agent) {
		if(owner != null) {
			RelationshipType relation = GameOntology.getRelationshipWithAgent(owner.getLocalName(), agent.getLocalName());
			if(relation != null) {
				return relation;
			}
			else {
				if(agent.isHeroAgent()) {
					return RelationshipType.ENEMIES;
				}
				return RelationshipType.NEUTRAL;
			}
		}
		else {
			if(agent.isHeroAgent()) {
				return RelationshipType.ENEMIES;
			}
			return RelationshipType.NEUTRAL;
		}
	}
	
	@Override
	public void collectLoot(CollectableLoot loot) {
		
	}
}
