package collectiables;

import java.awt.Graphics;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import tiles.Tile;

public abstract class BaseCollectable {
	protected String iconPath = "";
	protected Tile tile;
	protected CollectableLoot loot;
	
	public BaseCollectable(String relativeIconPath) {
		if(iconPath.isEmpty()) {
			try {
				iconPath = (new File(".").getCanonicalPath() + relativeIconPath);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		loot = null;
	}
	
	public abstract CollectableLoot collect();

	public abstract void drawIcon(Graphics g, int x, int y, int width, int height);
	
	public Tile getTile() {
		return tile;
	}

	public void setTile(Tile tile) {
		this.tile = tile;
	}
	
	public boolean canBeCollected() {
		return true;
	}
	
	public String getType() {
		return "";
	}
	
	public abstract void onDeleted();
	
}
