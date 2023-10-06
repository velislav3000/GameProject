package collectiables.materials;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

import javax.imageio.ImageIO;

import collectiables.BaseCollectable;
import collectiables.CollectableLoot;
import tiles.Tile;

public abstract class BaseMaterial extends BaseCollectable{
	protected int minResourceAmount;
	protected int maxResourceAmount;
	
	protected int ticksNeededToReplenish;
	protected int replenishCounter;
	
	protected static BufferedImage fullResourceImage;
	protected String fullResourceImageName = "";
	protected static BufferedImage emptyResourceImage;
	protected String emptyResourceImageName = "";
	
	protected MaterialType materialType;
	
	public enum MaterialType{
		STONE,
		IRON,
		COBALT,
		MYTHRIL,
		ADAMANTITE,
		STRING,
		DAYBLOOM,
		SHADOWSTALK,
		MOONGLOW,
		BLOODROSE
	}
	
	public BaseMaterial() {
		super("/res/materialIcons");
	}
	
	public void update() {
		if(replenishCounter == ticksNeededToReplenish) {
			return;
		}
		
		replenishCounter++;
	}
	
	@Override
	public boolean canBeCollected() {
		return replenishCounter == ticksNeededToReplenish;
	}
	
	@Override
	public CollectableLoot collect() {
		if(!canBeCollected()) {
			return new CollectableLoot(0, null, null, null);
		}
		replenishCounter = 0;
		Random random = new Random();
		int materialAmount = random.nextInt(maxResourceAmount - minResourceAmount + 1) + minResourceAmount;
		
		HashMap<MaterialType, Integer> collectedMaterials = new HashMap<>();
		collectedMaterials.put(materialType, materialAmount);
		
		loot = new CollectableLoot(0, collectedMaterials, null, null);
		
		return loot;
		
	}

	public BufferedImage getFullResourceImage() {
		if(fullResourceImage == null) {
			try {
				fullResourceImage = ImageIO.read(new File(iconPath, fullResourceImageName));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return fullResourceImage;
	}

	public BufferedImage getEmptyResourceImage() {
		if(emptyResourceImage == null) {
			try {
				emptyResourceImage = ImageIO.read(new File(iconPath,emptyResourceImageName));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return emptyResourceImage;
	}

	public void drawIcon(Graphics g, int x, int y, int width, int height) {
		BufferedImage image = replenishCounter == ticksNeededToReplenish ? getFullResourceImage() : getEmptyResourceImage();
		g.drawImage(image, x, y, width, height, null);
	}

	public MaterialType getMaterialType() {
		return materialType;
	}

	public void setMaterialType(MaterialType materialType) {
		this.materialType = materialType;
	}
	
	@Override
	public String getType() {
		return materialType.toString();
	}
	
	@Override
	public void onDeleted() {
		setTile(null);
	}

	public int getReplenishCounter() {
		return replenishCounter;
	}

	public void setReplenishCounter(int replenishCounter) {
		this.replenishCounter = replenishCounter;
	}
}
