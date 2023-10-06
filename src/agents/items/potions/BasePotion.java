package agents.items.potions;

import agents.BaseEntityAgent;
import agents.items.BaseItem;
import agents.items.ItemUtils;
import ontology.GameOntology;

public abstract class BasePotion extends BaseItem{
	
	private static int potionId = -1;
	
	public enum PotionType{
		HEALTH,
		FLEEING,
		REGENARATION,
		SUPERHEALTH
	}
	
	protected PotionType type;
	
	public BasePotion(ItemQuality quality, PotionType type) {
		potionId++;
		
		String potionType = type.toString();
		potionType = potionType.substring(0, 1) + potionType.substring(1).toLowerCase();
		name = potionType + "Potion" + potionId;
		
		this.quality = quality;
		this.type = type;
		
		setPrice();
		
		GameOntology.addPotion(this);
	}
	
	@Override
	protected void setPrice() {
		price = ItemUtils.getBaseItemPrice(type.toString());
		float qualityMultiplier = ItemUtils.getQualityPriceMultiplier(quality);
		
		price = Math.round(price * qualityMultiplier);
	}
	
	public abstract void activateEffect(BaseEntityAgent drinker);

	public PotionType getType() {
		return type;
	}
}
