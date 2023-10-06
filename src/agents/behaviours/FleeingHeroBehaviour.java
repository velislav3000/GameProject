package agents.behaviours;

import java.util.HashMap;
import java.util.Random;

import agents.BaseAgent;
import agents.BaseEntityAgent;
import agents.BaseEntityAgent.StatusEffect;
import agents.heroAgents.BaseHeroAgent;
import agents.heroAgents.BaseHeroAgent.HeroAbilityTrait;
import agents.heroAgents.BaseHeroAgent.HeroPersonalityTrait;
import agents.items.potions.BasePotion.PotionType;
import jade.core.behaviours.WakerBehaviour;
import tiles.Tile;
import tiles.Tile.TileType;

public class FleeingHeroBehaviour extends FleeingBehaviour{

	private transient BaseHeroAgent heroAgentRef;
	
	public FleeingHeroBehaviour(BaseHeroAgent agent) {
		super(agent);
		heroAgentRef = agent;
	}
	
	@Override
	public void action() {
		if(heroAgentRef.getCallAlliesCooldown() == 0) {
			callAlliesForHelp();
		}
		
		HashMap<Tile, Integer> fleeTiles = new HashMap<>();
		
		if(heroAgentRef.hasPotion(PotionType.FLEEING) && !heroAgentRef.hasStatusEffect(StatusEffect.QUICK)){
			heroAgentRef.drinkBestPotion(PotionType.FLEEING);
		}
		
		System.out.println("FleeingStart:");
		
		for(Tile tile: heroAgentRef.getMovableToTiles()) {
			fleeTiles.put(tile, 0);
			for (BaseAgent agent : heroAgentRef.getAgentsInCombatWith()) {
				if(heroAgentRef.equals(agent.getCombatTargetAgent())) {
					if(!agent.getCurrentTile().equals(tile) && !agent.getCurrentTile().getNeighbours().contains(tile)) {
						if(!fleeTiles.containsKey(tile)) {
							fleeTiles.put(tile, 1);
						}
						fleeTiles.put(tile, fleeTiles.get(tile) + getDistanceToEnemy(tile, agent.getCurrentTile()));
						
						/*fleeTiles.put(tile, 3);
						
						for (Tile neighbourTile : tile.getNeighbours()) {
							if(neighbourTile.canAgentMoveToTile(heroAgentRef) && !agent.getCurrentTile().getNeighbours().contains(neighbourTile)) {
								fleeTiles.put(tile, fleeTiles.get(tile) + 1);
							}
						}*/
					}
				}
			}
		}
		
		if(heroAgentRef.hasMovedTwice()) {
			fleeTiles.remove(heroAgentRef.getOldTile());
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
					if(neightbourTile.canAgentMoveToTile(heroAgentRef)) {
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
		
		if(!fleeToTile.canAgentMoveToTile(heroAgentRef)) {
			System.out.println("!!!!!!!!!!!!Failed to flee");
			if(moveAttemptCounter > 10) {
				return;
			}
			moveAttemptCounter++;
			action();
			return;
		}
		fleeToTile.setLockedForMovementByAgent(heroAgentRef);
		System.out.println("Fleeing Tile:" + fleeToTile);
		
		heroAgentRef.addBehaviour(new WakerBehaviour(heroAgentRef, 1){
			protected void handleElapsedTimeout() {
				Random random = new Random();
				
				if(heroAgentRef.getCurrentTile().getTileType() == TileType.ICE && !heroAgentRef.hasAbilityTrait(HeroAbilityTrait.BURNING_BLOOD)) {
					int chance = random.nextInt(5);
					if(chance == 0) {
						if(heroAgentRef.hasStatusEffect(StatusEffect.QUICK) && !heroAgentRef.hasMovedTwice()) {
							heroAgentRef.setMovedTwice(true);
							heroAgentRef.perceiveSurroundings();
							heroAgentRef.addBehaviour(new ExplorationBehaviour(heroAgentRef));
						}
						heroAgentRef.removeBehaviour(this);
						return;
					}
				}
				
				if(heroAgentRef.equals(fleeToTile.getLockedForMovementByAgent())){
					heroAgentRef.setOldTile(heroAgentRef.getCurrentTile());
					heroAgentRef.getOldTile().setAgent(null);
					heroAgentRef.setCurrentTile(fleeToTile);
					heroAgentRef.getCurrentTile().setAgent(heroAgentRef);
					fleeToTile.setLockedForMovementByAgent(null);
					
					if(fleeToTile.getTileType() == TileType.BURNING_SAND) {
						int heatDamage = random.nextInt(6)+5;
						heroAgentRef.takeHeatDamage(heatDamage);
					}
				}
				else {
					heroAgentRef.addBehaviour(new FleeingHeroBehaviour(heroAgentRef));
					heroAgentRef.removeBehaviour(this);
					return;
				}
				
				int chance = -1;
				if(!heroAgentRef.hasMovedTwice() && heroAgentRef.getPersonalityTrait() == HeroPersonalityTrait.COWARD) {
					chance = random.nextInt(10);
				}
				
				if((heroAgentRef.hasStatusEffect(StatusEffect.QUICK) || chance == 0) && !heroAgentRef.hasMovedTwice()) {
					heroAgentRef.setMovedTwice(true);
					heroAgentRef.perceiveSurroundings();
					heroAgentRef.addBehaviour(new FleeingHeroBehaviour(heroAgentRef));
				}
				heroAgentRef.removeBehaviour(this);
			}
		});
		
		heroAgentRef.removeBehaviour(this);
		
	}

}
