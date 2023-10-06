package agents.items.potions;

import agents.BaseEntityAgent;

public class SuperHealthPotion extends BasePotion{

	public SuperHealthPotion(ItemQuality quality) {
		super(quality, PotionType.SUPERHEALTH);
	}

	@Override
	public void activateEffect(BaseEntityAgent drinker) {
		int restoredHealth = 0;
		
		switch (quality) {
		case POOR: {
			restoredHealth = (int) Math.ceil(drinker.getMaxHealth()*0.2);
			break;
		}
		case AVERAGE: {
			restoredHealth = (int) Math.ceil(drinker.getMaxHealth()*0.35);
			break;
		}
		case GREAT: {
			restoredHealth = (int) Math.ceil(drinker.getMaxHealth()*0.45);
			break;
		}
		case SUPREME: {
			restoredHealth = (int) Math.ceil(drinker.getMaxHealth()*0.65);
			break;
		}
		}
		
		drinker.heal(restoredHealth);
	}

}