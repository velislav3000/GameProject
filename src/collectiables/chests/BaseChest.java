package collectiables.chests;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;

import agents.items.BaseItem;
import agents.items.equipment.Equipment;
import agents.items.potions.BasePotion;
import collectiables.BaseCollectable;
import collectiables.CollectableLoot;
import collectiables.materials.BaseMaterial.MaterialType;
import ontology.GameOntology;

public class BaseChest extends BaseCollectable{

	protected static BufferedImage image;
	protected String imageName = "";
	
	protected static int chestId = 0;
	protected String name;
	
	protected boolean wasCollected = false;
	
	public enum ChestType{
		WOODEN_CHEST,
		IRON_CHEST,
		GOLD_CHEST
	}
	
	protected ChestType chestType; 
	
	public BaseChest() {
		super("/res/chestIcons");
		chestId ++;
	}
	
	public BaseChest(String name, CollectableLoot loot) {
		super("/res/chestIcons");
		this.name = name;
		this.loot = loot;
	}
	
	protected void addToOntology(ArrayList<BaseItem> items) {
		GameOntology.addChest(this, items);
	}

	@Override
	public CollectableLoot collect() {
		if(!canBeCollected() || tile == null) {
			return new CollectableLoot(0, null, null, null);
		}
		wasCollected = true;
		tile.deleteCollectable();
		return loot;
	}

	@Override
	public void drawIcon(Graphics g, int x, int y, int width, int height) {
		BufferedImage image = getImage();
		g.drawImage(image, x, y, width, height, null);
	}

	public BufferedImage getImage() {
		if(image == null) {
			try {
				image = ImageIO.read(new File(iconPath, imageName));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return image;
	}
	
	@Override
	public String getType() {
		return chestType.toString();
	}
	
	public String getName() {
		return name;
	}

	@Override
	public void onDeleted() {
		setTile(null);
		
		ArrayList<String> deletedObjects = new ArrayList<>();
		
		if(!wasCollected) {
			for (Equipment equipment : loot.getEquipment()) {
				deletedObjects.add(equipment.getName());
			}
			
			for (BasePotion potion : loot.getPotions()) {
				deletedObjects.add(potion.getName());
			}
		}
		
		deletedObjects.add(name);
		
		GameOntology.removeObjectsFromOntology(deletedObjects);
	}

	public CollectableLoot getLoot() {
		return loot;
	}
}
