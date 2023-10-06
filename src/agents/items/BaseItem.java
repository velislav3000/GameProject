package agents.items;

import java.io.Serializable;

public abstract class BaseItem implements Serializable{
	protected String name;
	protected int price;
	
	public enum ItemQuality{
		POOR,
		AVERAGE,
		GREAT,
		SUPREME
	}
	
	protected ItemQuality quality;

	public String getName() {
		return name;
	}
	
	public ItemQuality getQuality() {
		return quality;
	}

	public int getPrice() {
		return price;
	}
	
	protected abstract void setPrice();
}
