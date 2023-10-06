package tiles;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.imageio.ImageIO;

import agents.BaseAgent;
import agents.BaseEntityAgent;
import agents.BaseStructureAgent;
import agents.heroAgents.CityAgent;
import collectiables.BaseCollectable;
import collectiables.CollectableLoot;
import jade.util.leap.Serializable;

public class Tile implements Serializable{
	private int originalX;
	private int originalY;
	private int originalWidth;
	private int originalHeight;
	
	private int x;
	private int y;
	private int width;
	private int height;
	private double scale = 1;
	
	//neighbors
	private Tile leftNeighborTile;
	private Tile topLeftNeighborTile;
	private Tile bottomLeftNeighborTile;
	private Tile rightNeighborTile;
	private Tile topRightNeighborTile;
	private Tile bottomRightNeighborTile;
	
	public enum TileType{
		GRASS,
		FOREST,
		MOUNTAIN,
		WATER,
		SAND,
		BURNING_SAND,
		SNOW,
		ICE,
		MUSHROOM
	}
	private TileType tileType = TileType.GRASS;
	
	private static BufferedImage grassImage;
	private static BufferedImage forestImage;
	private static BufferedImage mountainImage;
	private static BufferedImage waterImage;
	private static BufferedImage sandImage;
	private static BufferedImage burningSandImage;
	private static BufferedImage snowImage;
	private static BufferedImage iceImage;
	private static BufferedImage mushroomImage;
	
	private BaseCollectable collectable;
	private BaseAgent agent;
	private BaseAgent lockedForMovementByAgent;
	
	private CityAgent owningCity = null;
	
	public Tile(int x, int y, int width, int height) {
		originalX = x;
		originalY = y;
		originalWidth = width;
		originalHeight = height;
		
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		
		if(grassImage == null) {
			String imagePath = "";
			try {
				imagePath = new File(".").getCanonicalPath() + "/res/tileIcons";
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			if(grassImage == null) {
				try {
					grassImage = ImageIO.read(new File(imagePath, "tile_grass.png"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(forestImage == null) {
				try {
					forestImage = ImageIO.read(new File(imagePath, "tile_forest.png"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(mountainImage == null) {
				try {
					mountainImage = ImageIO.read(new File(imagePath, "tile_mountain.png"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(waterImage == null) {
				try {
					waterImage = ImageIO.read(new File(imagePath, "tile_water.png"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(sandImage == null) {
				try {
					sandImage = ImageIO.read(new File(imagePath, "tile_sand.png"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(burningSandImage == null) {
				try {
					burningSandImage = ImageIO.read(new File(imagePath, "tile_sand_burning.png"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(snowImage == null) {
				try {
					snowImage = ImageIO.read(new File(imagePath, "tile_snow.png"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(iceImage == null) {
				try {
					iceImage = ImageIO.read(new File(imagePath, "tile_ice.png"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(mushroomImage == null) {
				try {
					mushroomImage = ImageIO.read(new File(imagePath, "tile_mushroom.png"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public TileType getTileType() {
		return tileType;
	}

	public void setTileType(TileType tileType) {
		if(this.tileType == tileType) {
			return;
		}
		
		this.tileType = tileType;
		if(tileType == TileType.MOUNTAIN || tileType == TileType.WATER) {
			deleteCollectable();
			deleteAgent();
		}
		
		if(tileType == TileType.MUSHROOM) {
			TileUtils.addMushroomTile(this);
		}
		else {
			TileUtils.removeMushroomTile(this);
		}
		
		correctTileType();
		for(Tile neighbouringTile : getNeighbours()) {
			neighbouringTile.onNeighbourTileTypeUpdated();
		}
	}
	
	private void correctTileType() {
		if(tileType == TileType.SAND || tileType == TileType.BURNING_SAND) {
			int counter = 0;
			for(Tile neighbouringTile : getNeighbours()) {
				if(neighbouringTile.getTileType() == TileType.SAND || neighbouringTile.getTileType() == TileType.BURNING_SAND) {
					counter++;
				}
			}
			
			if(counter == 6) {
				tileType = TileType.BURNING_SAND;
			}
			else {
				tileType = TileType.SAND;
			}
		}
		else if(tileType == TileType.SNOW || tileType == TileType.ICE) {
			int counter = 0;
			for(Tile neighbouringTile : getNeighbours()) {
				if(neighbouringTile.getTileType() == TileType.SNOW || neighbouringTile.getTileType() == TileType.ICE) {
					counter++;
				}
			}
			
			if(counter == 6) {
				tileType = TileType.ICE;
			}
			else {
				tileType = TileType.SNOW;
			}
		}
	}
	
	public void onNeighbourTileTypeUpdated() {
		if(tileType == TileType.SAND || tileType == TileType.BURNING_SAND) {
			int counter = 0;
			for(Tile neighbouringTile : getNeighbours()) {
				if(neighbouringTile.getTileType() == TileType.SAND || neighbouringTile.getTileType() == TileType.BURNING_SAND) {
					counter++;
				}
			}
			
			if(counter == 6) {
				setTileType(TileType.BURNING_SAND);
			}
			else {
				setTileType(TileType.SAND);
			}
		}
		else if(tileType == TileType.SNOW || tileType == TileType.ICE) {
			int counter = 0;
			for(Tile neighbouringTile : getNeighbours()) {
				if(neighbouringTile.getTileType() == TileType.SNOW || neighbouringTile.getTileType() == TileType.ICE) {
					counter++;
				}
			}
			
			if(counter == 6) {
				setTileType(TileType.ICE);
			}
			else {
				setTileType(TileType.SNOW);
			}
		}
	}

	public Tile getLeftNeighborTile() {
		return leftNeighborTile;
	}

	public void setLeftNeighborTile(Tile leftNeighborTile) {
		this.leftNeighborTile = leftNeighborTile;
	}

	public Tile getTopLeftNeighborTile() {
		return topLeftNeighborTile;
	}

	public void setTopLeftNeighborTile(Tile topLeftNeighborTile) {
		this.topLeftNeighborTile = topLeftNeighborTile;
	}

	public Tile getBottomLeftNeighborTile() {
		return bottomLeftNeighborTile;
	}

	public void setBottomLeftNeighborTile(Tile bottomLeftNeighborTile) {
		this.bottomLeftNeighborTile = bottomLeftNeighborTile;
	}

	public Tile getRightNeighborTile() {
		return rightNeighborTile;
	}

	public void setRightNeighborTile(Tile rightNeighborTile) {
		this.rightNeighborTile = rightNeighborTile;
	}

	public Tile getTopRightNeighborTile() {
		return topRightNeighborTile;
	}

	public void setTopRightNeighborTile(Tile topRightNeighborTile) {
		this.topRightNeighborTile = topRightNeighborTile;
	}

	public Tile getBottomRightNeighborTile() {
		return bottomRightNeighborTile;
	}

	public void setBottomRightNeighborTile(Tile bottomRightNeighborTile) {
		this.bottomRightNeighborTile = bottomRightNeighborTile;
	}
	
	public BaseCollectable getCollectable() {
		return collectable;
	}

	public void setCollectable(BaseCollectable collectable) {
		if(this.collectable != null) {
			this.deleteCollectable();
		}
		this.collectable = collectable;
		collectable.setTile(this);
	}

	public BaseAgent getAgent() {
		return agent;
	}

	public void setAgent(BaseAgent baseAgent) {
		this.agent = baseAgent;
	}
	
	public BaseAgent getLockedForMovementByAgent() {
		return lockedForMovementByAgent;
	}

	public void setLockedForMovementByAgent(BaseEntityAgent lockedForMovementByAgent) {
		this.lockedForMovementByAgent = lockedForMovementByAgent;
	}

	public void updateTileScale(double scale) {
		this.scale = scale;
		x = (int) Math.round(originalX * scale);
		y = (int) Math.round(originalY * scale);
		width = (int) Math.round(originalWidth * scale);
		height = (int) Math.round(originalHeight * scale);
	}
	
	public void drawTile(Graphics g) {
		int innerOffset = (int) (2*scale);
		int[] xOuterPoints = {x+width/2,x+width,x+width,x+width/2,x,x};
        int[] yOuterPoints = {y,y+height/3,y+height*2/3,y+height,y+height*2/3,y+height/3};
        int[] xInnerPoints = {x+width/2,x+width-innerOffset,x+width-innerOffset,x+width/2,x+innerOffset,x+innerOffset};
        int[] yInnerPoints = {y+innerOffset*2,y+height/3+innerOffset,y+height*2/3-innerOffset,y+height-innerOffset*2,y+height*2/3-innerOffset,y+height/3+innerOffset};
        g.setColor(Color.black);
        g.fillPolygon(xOuterPoints, yOuterPoints, xOuterPoints.length);
        
        BufferedImage tileImage = null;
        
        switch (tileType) {
		case GRASS: {
			tileImage = grassImage;
	        break;
			}
		case FOREST: {
			tileImage = forestImage;
	        break;
			}
		case MOUNTAIN: {
	        tileImage = mountainImage;
	        break;
			}
		case WATER: {
	        tileImage = waterImage;
	        break;
			}
		case SAND: {
	        tileImage = sandImage;
	        break;
			}
		case BURNING_SAND: {
	        tileImage = burningSandImage;
	        break;
			}
		case SNOW: {
	        tileImage = snowImage;
	        break;
			}
		case ICE: {
	        tileImage = iceImage;
	        break;
			}
		case MUSHROOM: {
	        tileImage = mushroomImage;
	        break;
			}
		}
        
        Polygon polygon = new Polygon(xInnerPoints, yInnerPoints, xInnerPoints.length);
        
        Graphics2D g2D = (Graphics2D) g.create();
        
        Shape oldClip = g2D.getClip();
        g2D.setClip(polygon);
        g2D.drawImage(tileImage, x, y, width, height, null);
        g2D.setClip(oldClip);
        g2D.dispose();
        
        if(collectable != null) {
        	collectable.drawIcon(g, x, y, width, height);
        }
        
        if(agent != null) {
        	agent.drawIcon(g, x, y, width, height);
        }
	}
	
	public boolean isTileClicked(int clickedX, int clickedY) {
		int innerOffset = 2;
		int[] xPoints = {x+width/2,x+width-innerOffset,x+width-innerOffset,x+width/2,x+innerOffset,x+innerOffset};
        int[] yPoints = {y+innerOffset*2,y+height/3+innerOffset,y+height*2/3-innerOffset,y+height-innerOffset*2,y+height*2/3-innerOffset,y+height/3+innerOffset};
        
        //Calculate if clicked location is in top part of tile
        double A = triangleArea(xPoints[0], yPoints[0], xPoints[1], yPoints[1], xPoints[5], yPoints[5]);
        double A1 = triangleArea(clickedX, clickedY, xPoints[1], yPoints[1], xPoints[5], yPoints[5]);
        double A2 = triangleArea(xPoints[0], yPoints[0], clickedX, clickedY, xPoints[5], yPoints[5]);
        double A3 = triangleArea(xPoints[0], yPoints[0], xPoints[1], yPoints[1], clickedX, clickedY);
        
        boolean isInTopPart = (A == A1 + A2 + A3);
        if(isInTopPart) {
        	return true;
        }
        
        //Calculate if clicked location is in bottom part of tile
        A = triangleArea(xPoints[2], yPoints[2], xPoints[3], yPoints[3], xPoints[4], yPoints[4]);
        A1 = triangleArea(clickedX, clickedY, xPoints[3], yPoints[3], xPoints[4], yPoints[4]);
        A2 = triangleArea(xPoints[2], yPoints[2], clickedX, clickedY, xPoints[4], yPoints[4]);
        A3 = triangleArea(xPoints[2], yPoints[2], xPoints[3], yPoints[3], clickedX, clickedY);
        
        boolean isInBottomPart = (A == A1 + A2 + A3);
        if(isInBottomPart) {
        	return true;
        }
        
        //Calculate if clicked location is in middle part of tile
        boolean isInMiddlePart = (clickedX>=xPoints[5] && clickedX<=xPoints[1] && clickedY>=yPoints[5] && clickedY<=yPoints[4]);
        if(isInMiddlePart) {
        	return true;
        }
        
		return false;
	}
	
	private double triangleArea(int x1, int y1, int x2, int y2, int x3, int y3)
	{
		   return Math.abs((x1*(y2-y3) + x2*(y3-y1)+ x3*(y1-y2))/2.0);
	}
	
	public boolean canPlaceObjectsOnTile() {
		if(tileType == TileType.MOUNTAIN || tileType == TileType.WATER) {
			return false;
		}
		return true;
	}
	
	public boolean hasNeighbour(Tile tile) {
		if(tile == null) {
			return false;
		}
		
		ArrayList<Tile> neighbours = new ArrayList<>();
		neighbours.add(leftNeighborTile);
		neighbours.add(rightNeighborTile);
		neighbours.add(topLeftNeighborTile);
		neighbours.add(topRightNeighborTile);
		neighbours.add(bottomLeftNeighborTile);
		neighbours.add(bottomRightNeighborTile);
		return neighbours.contains(tile);
	}
	
	public ArrayList<Tile> getNeighbours(){
		ArrayList<Tile> neighbours = new ArrayList<>();
		neighbours.add(leftNeighborTile);
		neighbours.add(rightNeighborTile);
		neighbours.add(topLeftNeighborTile);
		neighbours.add(topRightNeighborTile);
		neighbours.add(bottomLeftNeighborTile);
		neighbours.add(bottomRightNeighborTile);
		
		while (neighbours.contains(null)){
			neighbours.remove(null);
		}
		
		return neighbours;
	}
	
	public ArrayList<Tile> getNeighboursOppositeOfTile(Tile tile){
		ArrayList<Tile> neighbours = new ArrayList<>();
		
		if(tile == null) {
			return null;
		}
		
		if(tile.equals(leftNeighborTile)) {
			neighbours.add(rightNeighborTile);
			neighbours.add(topRightNeighborTile);
			neighbours.add(bottomRightNeighborTile);
		}
		else if(tile.equals(rightNeighborTile)) {
			neighbours.add(leftNeighborTile);
			neighbours.add(topLeftNeighborTile);
			neighbours.add(bottomLeftNeighborTile);
		}
		else if(tile.equals(topLeftNeighborTile)) {
			neighbours.add(rightNeighborTile);
			neighbours.add(bottomRightNeighborTile);
			neighbours.add(bottomLeftNeighborTile);
		}
		else if(tile.equals(topRightNeighborTile)) {
			neighbours.add(leftNeighborTile);
			neighbours.add(bottomLeftNeighborTile);
			neighbours.add(bottomRightNeighborTile);
		}
		else if(tile.equals(bottomLeftNeighborTile)) {
			neighbours.add(rightNeighborTile);
			neighbours.add(topRightNeighborTile);
			neighbours.add(topLeftNeighborTile);
		}
		else if(tile.equals(bottomRightNeighborTile)) {
			neighbours.add(leftNeighborTile);
			neighbours.add(topLeftNeighborTile);
			neighbours.add(topRightNeighborTile);
		}
		
		while (neighbours.contains(null)){
			neighbours.remove(null);
		}
		
		return neighbours;
	}
	
	public Tile getDirectOppositeNeighbourOfTile(Tile tile){
		if(tile == null) {
			return null;
		}
		
		if(tile.equals(leftNeighborTile)) {
			return rightNeighborTile;
		}
		else if(tile.equals(rightNeighborTile)) {
			return leftNeighborTile;
		}
		else if(tile.equals(topLeftNeighborTile)) {
			return bottomRightNeighborTile;
		}
		else if(tile.equals(topRightNeighborTile)) {
			return bottomLeftNeighborTile;
		}
		else if(tile.equals(bottomLeftNeighborTile)) {
			return topRightNeighborTile;
		}
		else if(tile.equals(bottomRightNeighborTile)) {
			return topLeftNeighborTile;
		}
		return null;
	}
	
	public boolean canAgentMoveToTile(BaseEntityAgent checkingAgent) {
		if(tileType == TileType.MOUNTAIN || tileType == TileType.WATER) {
			return false;
		}
		else if(checkingAgent != agent && agent != null) {
			return false;
		}
		else if(checkingAgent != lockedForMovementByAgent && lockedForMovementByAgent != null) {
			return false;
		}
		return true;
	}

	public CollectableLoot collectCollectable() {
		if(collectable != null) {
			return collectable.collect();
		}
		return null;
	}
	
	public void deleteCollectable() {
		if(collectable != null) {
			collectable.onDeleted();
			collectable = null;
		}
	}
	
	public void deleteAgent() {
		if(agent != null) {
			agent.doDelete();
			agent = null;
		}
	}
	
	public void deleteContent() {
		if(agent == null) {
			deleteCollectable();
		}
		else {
			deleteAgent();
		}
	}

	public CityAgent getOwningCity() {
		return owningCity;
	}

	public void setOwningCity(CityAgent city) {
		owningCity = city;
	}
}
