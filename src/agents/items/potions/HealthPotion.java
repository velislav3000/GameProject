package agents.items.potions;

import agents.BaseEntityAgent;

public class HealthPotion extends BasePotion{

	public HealthPotion(ItemQuality quality) {
		super(quality, PotionType.HEALTH);
	}

	@Override
	public void activateEffect(BaseEntityAgent drinker) {
		int restoredHealth = 0;
		
		switch (quality) {
		case POOR: {
			restoredHealth = (int) Math.ceil(drinker.getMaxHealth()*0.05);
			break;
		}
		case AVERAGE: {
			restoredHealth = (int) Math.ceil(drinker.getMaxHealth()*0.1);
			break;
		}
		case GREAT: {
			restoredHealth = (int) Math.ceil(drinker.getMaxHealth()*0.15);
			break;
		}
		case SUPREME: {
			restoredHealth = (int) Math.ceil(drinker.getMaxHealth()*0.2);
			break;
		}
		}
		
		drinker.heal(restoredHealth);
	}

}
