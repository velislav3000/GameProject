package ui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import agents.BaseEntityAgent.StatusEffect;

public class StatusEffectImagesUtils {

	private static BufferedImage burningImage;
	private static BufferedImage frozenImage;
	private static BufferedImage poisonImage;
	private static BufferedImage quickImage;
	private static BufferedImage regenarationImage;
	
	public static BufferedImage getStatusEffectImage(StatusEffect status) {
		try {
			switch (status) {
			case BURNING: {
				if(burningImage == null) {
					burningImage = ImageIO.read(new File("./res/statusEffectIcons/status_burning.png"));
				}
				return burningImage;
			}
			case FROZEN: {
				if(frozenImage == null) {
					frozenImage = ImageIO.read(new File("./res/statusEffectIcons/status_frozen.png"));
				}
				return frozenImage;
			}
			case POISON: {
				if(poisonImage == null) {
					poisonImage = ImageIO.read(new File("./res/statusEffectIcons/status_poison.png"));
				}
				return poisonImage;
			}
			case QUICK: {
				if(quickImage == null) {
					quickImage = ImageIO.read(new File("./res/statusEffectIcons/status_quick.png"));
				}
				return quickImage;
			}
			case REGENARATION: {
				if(regenarationImage == null) {
					regenarationImage = ImageIO.read(new File("./res/statusEffectIcons/status_regenaration.png"));
				}
				return regenarationImage;
			}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
		
	}
	
}
