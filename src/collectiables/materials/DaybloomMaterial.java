package collectiables.materials;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class DaybloomMaterial extends BaseMaterial {

	protected static BufferedImage fullResourceImage;
	protected static BufferedImage emptyResourceImage;
	
	public DaybloomMaterial() {
		super();
		materialType = MaterialType.DAYBLOOM;
		minResourceAmount = 1;
		maxResourceAmount = 7;
		ticksNeededToReplenish = 60;
		replenishCounter = ticksNeededToReplenish;
		fullResourceImageName = "mat_daybloom.png";
		emptyResourceImageName = "mat_cut_grass.png";
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