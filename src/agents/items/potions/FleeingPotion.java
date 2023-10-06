package agents.items.potions;

import agents.BaseEntityAgent;
import agents.BaseEntityAgent.StatusEffect;

public class FleeingPotion extends BasePotion{
	
	public FleeingPotion(ItemQuality quality) {
		super(quality, PotionType.FLEEING);
	}

	@Override
	public void activateEffect(BaseEntityAgent drinker) {
		int turns = 0;
		
		switch (quality) {
		case POOR: {
			turns = 1;
			break;
		}
		case AVERAGE: {
			turns = 2;
			break;
		}
		case GREAT: {
			turns = 3;
			break;
		}
		case SUPREME: {
			turns = 4;
			break;
		}
		}
		
		drinker.addStatusEffect(StatusEffect.QUICK, turns);
	}
}
