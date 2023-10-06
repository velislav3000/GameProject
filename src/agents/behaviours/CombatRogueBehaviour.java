package agents.behaviours;

import java.util.Random;

import agents.BaseAgent;
import agents.BaseEntityAgent;
import agents.BaseEntityAgent.StatusEffect;
import agents.heroAgents.RogueAgent;
import agents.heroAgents.BaseHeroAgent.HeroAbilityTrait;
import jade.core.behaviours.WakerBehaviour;
import tiles.Tile.TileType;

public class CombatRogueBehaviour extends CombatBehaviour{

	private transient RogueAgent rogueAgentRef;
	
	public CombatRogueBehaviour(RogueAgent agent, BaseAgent combatTargetAgent) {
		super(agent, combatTargetAgent);
		rogueAgentRef = agent;
	}
	
	@Override
	public void action() {
		if(rogueAgentRef.getCallAlliesCooldown() == 0) {
			callAlliesForHelp();
		}
		
		if(enemyAgentRef.getCurrentTile().getNeighbours().contains(rogueAgentRef.getCurrentTile())) {
			if(rogueAgentRef.isSneakAttackEnabled()) {
				System.out.println(rogueAgentRef.getLocalName() + " sneak attacks " + enemyAgentRef.getLocalName());
				int sneakAttackDamage = Math.round(rogueAgentRef.getAttack() * 1.5F);
				enemyAgentRef.takeDamage(sneakAttackDamage, rogueAgentRef);
				rogueAgentRef.setSneakAttackEnabled(false);
				rogueAgentRef.removeBehaviour(this);
				return;
			}
			else {
				System.out.println(rogueAgentRef.getLocalName() + " attacks " + enemyAgentRef.getLocalName());
				enemyAgentRef.takeDamage(rogueAgentRef.getAttack(), rogueAgentRef);
			}
		}
		else {
			if(rogueAgentRef.getPathToTile().empty()) {
				return;
			}
			moveToTile = rogueAgentRef.getPathToTile().pop();
			if(!moveToTile.canAgentMoveToTile(rogueAgentRef)) {
				rogueAgentRef.getPathToTile().add(moveToTile);
				return;
			}
			moveToTile.setLockedForMovementByAgent(rogueAgentRef);
			
			rogueAgentRef.addBehaviour(new WakerBehaviour(rogueAgentRef, 1){
				protected void handleElapsedTimeout() {
					if(rogueAgentRef.getCurrentTile().getTileType() == TileType.ICE && !rogueAgentRef.hasAbilityTrait(HeroAbilityTrait.BURNING_BLOOD)) {
						Random random = new Random();
						int chance = random.nextInt(5);
						if(chance == 0) {
							if(rogueAgentRef.hasStatusEffect(StatusEffect.QUICK) && !agentRef.hasMovedTwice()) {
								rogueAgentRef.setMovedTwice(true);
								rogueAgentRef.perceiveSurroundings();
								rogueAgentRef.addBehaviour(new CombatRogueBehaviour(rogueAgentRef, enemyAgentRef));
							}
							rogueAgentRef.removeBehaviour(this);
							return;
						}
					}
					
					if(rogueAgentRef.equals(moveToTile.getLockedForMovementByAgent())){
						rogueAgentRef.setOldTile(rogueAgentRef.getCurrentTile());
						rogueAgentRef.getOldTile().setAgent(null);
						rogueAgentRef.setCurrentTile(moveToTile);
						rogueAgentRef.getCurrentTile().setAgent(rogueAgentRef);
						moveToTile.setLockedForMovementByAgent(null);
					}
					else {
						rogueAgentRef.getPathToTile().add(moveToTile);
					}
					
					if(rogueAgentRef.hasStatusEffect(StatusEffect.QUICK) && !rogueAgentRef.hasMovedTwice()) {
						rogueAgentRef.setMovedTwice(true);
						rogueAgentRef.perceiveSurroundings();
						rogueAgentRef.addBehaviour(new CombatRogueBehaviour(rogueAgentRef, enemyAgentRef));
					}
					rogueAgentRef.removeBehaviour(this);
				}
			});
		}
		rogueAgentRef.removeBehaviour(this);
	}

}
