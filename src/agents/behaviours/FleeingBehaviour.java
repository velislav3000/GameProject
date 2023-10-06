package agents.behaviours;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

import agents.BaseAgent;
import agents.BaseEntityAgent;
import agents.BaseEntityAgent.RelationshipType;
import agents.BaseEntityAgent.StatusEffect;
import agents.heroAgents.BaseHeroAgent;
import agents.heroAgents.BaseHeroAgent.HeroPersonalityTrait;
import agents.items.potions.BasePotion.PotionType;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import ontology.GameOntology;
import tiles.Tile;
import tiles.Tile.TileType;

public class FleeingBehaviour extends OneShotBehaviour{

	private transient BaseEntityAgent agentRef;
	protected int moveAttemptCounter = 0;
	protected transient Tile fleeToTile;
	
	public FleeingBehaviour(BaseEntityAgent agent) {
		agentRef = agent;
	}
	
	@Override
	public void action() {
		if(agentRef.getCallAlliesCooldown() == 0) {
			callAlliesForHelp();
		}
		
		HashMap<Tile, Integer> fleeTiles = new HashMap<>();
		
		System.out.println("FleeingStart:");
		
		for(Tile tile: agentRef.getMovableToTiles()) {
			fleeTiles.put(tile, 0);
			for (BaseAgent agent : agentRef.getAgentsInCombatWith()) {
				if(agentRef.equals(agent.getCombatTargetAgent())) {
					if(!agent.getCurrentTile().equals(tile) && !agent.getCurrentTile().getNeighbours().contains(tile)) {
						
						if(!fleeTiles.containsKey(tile)) {
							fleeTiles.put(tile, 1);
						}
						int distanceToEnemy = getDistanceToEnemy(agent.getCurrentTile(), tile);
						fleeTiles.put(tile, fleeTiles.get(tile) + distanceToEnemy);
						
						/*fleeTiles.put(tile, 3);
						for (Tile neighbourTile : tile.getNeighbours()) {
							if(neighbourTile.canAgentMoveToTile(agentRef) && !agent.getCurrentTile().getNeighbours().contains(neighbourTile)) {
								fleeTiles.put(tile, fleeTiles.get(tile) + 1);
							}
						}*/
					}
				}
			}
		}
		
		if(agentRef.hasMovedTwice()) {
			fleeTiles.remove(agentRef.getOldTile());
		}
		
		int maxTileValue = -1;
		HashMap<Tile, Integer> fleeToTiles = new HashMap<>();
		fleeToTile = null;
		
		for (Tile tile : fleeTiles.keySet()) {
			System.out.println(tile + " - " + fleeTiles.get(tile));
			if(maxTileValue<fleeTiles.get(tile)) {
				maxTileValue = fleeTiles.get(tile);
				fleeToTiles.clear();
				fleeToTiles.put(tile, maxTileValue);
			}
			else if(maxTileValue == fleeTiles.get(tile)) {
				fleeToTiles.put(tile, maxTileValue);
			}
		}
		
		if(fleeToTiles.size() == 1) {
			fleeToTile = fleeToTiles.keySet().iterator().next();
		}
		else {
			for (Tile tile : fleeToTiles.keySet()) {
				for(Tile neightbourTile : tile.getNeighbours()) {
					int movableNeighbouringTiles = 0;
					if(neightbourTile.canAgentMoveToTile(agentRef)) {
						movableNeighbouringTiles++;
					}
					fleeToTiles.put(tile, fleeToTiles.get(tile) + movableNeighbouringTiles);
				}
			}
			
			maxTileValue = -1;
			
			for (Tile tile : fleeTiles.keySet()) {
				if(maxTileValue<fleeTiles.get(tile)) {
					maxTileValue = fleeTiles.get(tile);
					fleeToTile = tile;
				}
			}
		}
		
		if(!fleeToTile.canAgentMoveToTile(agentRef)) {
			//System.out.println("!!!!!!!!!!!!Failed to flee");
			if(moveAttemptCounter > 10) {
				return;
			}
			moveAttemptCounter++;
			action();
			return;
		}
		fleeToTile.setLockedForMovementByAgent(agentRef);
		//System.out.println("Fleeing Tile:" + fleeToTile);
		
		agentRef.addBehaviour(new WakerBehaviour(agentRef, 1){
			protected void handleElapsedTimeout() {
				if(agentRef.getCurrentTile().getTileType() == TileType.ICE) {
					Random random = new Random();
					int chance = random.nextInt(5);
					if(chance == 0) {
						if(agentRef.hasStatusEffect(StatusEffect.QUICK) && !agentRef.hasMovedTwice()) {
							agentRef.setMovedTwice(true);
							agentRef.perceiveSurroundings();
							agentRef.addBehaviour(new ExplorationBehaviour(agentRef));
						}
						agentRef.removeBehaviour(this);
						return;
					}
				}
				
				if(agentRef.equals(fleeToTile.getLockedForMovementByAgent())){
					agentRef.setOldTile(agentRef.getCurrentTile());
					agentRef.getOldTile().setAgent(null);
					agentRef.setCurrentTile(fleeToTile);
					agentRef.getCurrentTile().setAgent(agentRef);
					fleeToTile.setLockedForMovementByAgent(null);
					
					if(fleeToTile.getTileType() == TileType.BURNING_SAND) {
						Random random = new Random();
						int heatDamage = random.nextInt(6)+5;
						agentRef.takeHeatDamage(heatDamage);
					}
				}
				else {
					agentRef.addBehaviour(new FleeingBehaviour(agentRef));
					agentRef.removeBehaviour(this);
					return;
				}
				
				if(agentRef.hasStatusEffect(StatusEffect.QUICK) && !agentRef.hasMovedTwice()) {
					agentRef.setMovedTwice(true);
					agentRef.perceiveSurroundings();
					agentRef.addBehaviour(new FleeingBehaviour(agentRef));
				}
				agentRef.removeBehaviour(this);
			}
		});
		
		agentRef.removeBehaviour(this);
		
	}
	
	protected int getDistanceToEnemy(Tile startTile, Tile endTile) {
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
            		return 0;
            	}
            	
            	alreadyVisited.add(currentTile);
            	for (Tile tile : currentTile.getNeighbours()) {
					if(!(tile.getTileType() == TileType.MOUNTAIN || tile.getTileType() == TileType.WATER) && !alreadyVisited.contains(tile) && !queue.contains(tile)) {
						queue.add(tile);
						if(!connections.containsKey(tile)) {
							connections.put(tile, currentTile);
						}
					}
				}
            }
        }
        if(!foundPath) {
        	return 0;
        }
        
        Stack<Tile> reversedPath = new Stack<>();
        
        while(!currentTile.equals(startTile)) {
        	currentTile = connections.get(currentTile);
        	reversedPath.add(currentTile);
        }
        
        return reversedPath.size();
	}
	
	private transient ArrayList<BaseEntityAgent> allies;
	private transient ArrayList<Tile> checkedTilesForAgents;
	
	protected void callAlliesForHelp() {
		allies = new ArrayList<>();
		checkedTilesForAgents = new ArrayList<>();
		getNearbyAllies(agentRef.getCurrentTile(), 8);
		
		ArrayList<BaseAgent> chasingAgents = new ArrayList<>();
		
		for (BaseAgent agent : agentRef.getAgentsInCombatWith()) {
			if(agentRef.equals(agent.getCombatTargetAgent()) && agent.isEntityAgent()) {
				chasingAgents.add(agent);
			}
		}
		
		Random random = new Random();
		
		for (BaseEntityAgent agent : allies) {
			ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
			msg.addReceiver(agent.getAID());
			msg.setConversationId("requestingHelp");
			
			int index = random.nextInt(chasingAgents.size());
			
			msg.setContent(chasingAgents.get(index).getLocalName());
			agentRef.send(msg);
		}
		
		agentRef.setCallAlliesCooldown(5);
	}
	
	private void getNearbyAllies(Tile tile, int tileLevel) {
		if(checkedTilesForAgents.contains(tile) || tileLevel == 0) {
			return;
		}
		
		checkedTilesForAgents.add(tile);
		
		if(tile.getAgent() != null && tile.getAgent() != agentRef && tile.getAgent().isEntityAgent()) {
			RelationshipType relation = GameOntology.getRelationshipWithAgent(agentRef.getLocalName(), tile.getAgent().getLocalName());
			
			if(relation == RelationshipType.ALLIES ) {
				allies.add((BaseEntityAgent) tile.getAgent());
			}
		}
		
		for (Tile tile2 : tile.getNeighbours()) {
			getNearbyAllies(tile2, tileLevel - 1);
		}
	}
}
