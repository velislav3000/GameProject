package agents.items.potions;

import agents.BaseEntityAgent;
import agents.BaseEntityAgent.StatusEffect;

public class RegenarationPotion extends BasePotion{

	public RegenarationPotion(ItemQuality quality) {
		super(quality, PotionType.REGENARATION);
	}

	@Override
	public void activateEffect(BaseEntityAgent drinker) {
		int turns = 0;
		
		switch (quality) {
		case POOR: {
			turns = 2;
			break;
		}
		case AVERAGE: {
			turns = 3;
			break;
		}
		case GREAT: {
			turns = 4;
			break;
		}
		case SUPREME: {
			turns = 6;
			break;
		}
		}
		
		drinker.addStatusEffect(StatusEffect.REGENARATION, turns);
	}

}
