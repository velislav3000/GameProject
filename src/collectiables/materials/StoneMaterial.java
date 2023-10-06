package collectiables.materials;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

import agents.AgentUtils;

public class StoneMaterial extends BaseMaterial {
	
	protected static BufferedImage fullResourceImage;
	protected static BufferedImage emptyResourceImage;
	
	public StoneMaterial() {
		super();
		materialType = MaterialType.STONE;
		minResourceAmount = 1;
		maxResourceAmount = 20;
		ticksNeededToReplenish = 30;
		replenishCounter = ticksNeededToReplenish;
		fullResourceImageName = "mat_stone.png";
		emptyResourceImageName = "mat_stones.png";
	}
	
	@Override
	public void update() {
		super.update();
		
		if(canBeCollected() && tile.getAgent() == null) {
			Random random = new Random();
			int chance = random.nextInt(100);
			if(chance == 0) {
				AgentUtils.spawnAgent("Golem", false, tile, tile.toString());
				tile.deleteCollectable();
			}
		}
	}
	
	@Override
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
	
	@Override
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
}
