package collectiables.materials;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class CobaltMaterial extends BaseMaterial {

	protected static BufferedImage fullResourceImage;
	protected static BufferedImage emptyResourceImage;
	
	public CobaltMaterial() {
		super();
		materialType = MaterialType.COBALT;
		minResourceAmount = 1;
		maxResourceAmount = 7;
		ticksNeededToReplenish = 70;
		replenishCounter = ticksNeededToReplenish;
		fullResourceImageName = "mat_cobalt.png";
		emptyResourceImageName = "mat_stones.png";
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

