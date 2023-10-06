package ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import agents.AgentUtils;
import agents.BaseAgent;
import agents.BaseEntityAgent;
import agents.heroAgents.BaseHeroAgent;
import agents.heroAgents.CityAgent;
import agents.items.HeroInventory;
import agents.items.ItemUtils;
import agents.monsterAgents.BaseMonsterAgent;
import agents.monsterAgents.BaseMonsterStructureAgent;
import collectiables.CollectableLoot;
import collectiables.chests.BaseChest;
import collectiables.chests.GoldChest;
import collectiables.chests.IronChest;
import collectiables.chests.WoodenChest;
import collectiables.materials.AdamantiteMaterial;
import collectiables.materials.BaseMaterial;
import collectiables.materials.BloodroseMaterial;
import collectiables.materials.CobaltMaterial;
import collectiables.materials.DaybloomMaterial;
import collectiables.materials.IronMaterial;
import collectiables.materials.MoonglowMaterial;
import collectiables.materials.MythrilMaterial;
import collectiables.materials.ShadowstalkMaterial;
import collectiables.materials.StoneMaterial;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.StaleProxyException;
import ontology.GameOntology;
import saveData.GlobalSaveData;
import saveData.TileSaveData;
import selection.ToolSelection;
import selection.ToolSelection.Tool;
import tiles.Tile;
import tiles.Tile.TileType;

public class MapPanel extends JPanel implements Runnable{
	
	private GameFrame frameRef;
	
	private int tileRows = 50;
	private int tileColumns = tileRows;
	private int tileWidth = 132;
	private int tileHeight = 100;
	private double zoomScale = 1;
	private final double MAX_ZOOM_SCALE = 1;
	private final double MIN_ZOOM_SCALE = 0.5;
	
	private Tile[][] tiles;
	private ArrayList<BaseMaterial> materials;
	private ArrayList<BaseAgent> agents;
	private JLayeredPane parentPane;
	
	Thread mapThread;
	
	//Dragging panel
	private int dragStartX = 0;
	private int dragStartY = 0;
	
	//GameUpdate
	private final float UPDATE_TIMES_PER_SECOND = 2.0f/3.0f;
	private final long REPAINT_TIMES_PER_SECOND = 5;
	private final long MILLIS_PER_SECOND = 1000;
	private long nextUpdateTime;
	private long nextRepaintTime;
	
	private boolean isPaused;
	private boolean wasPausedBeforePauseMenu;
	private boolean isPausedMenuOpened;
	private JLabel pausedLabel;
	
	public MapPanel(GameFrame frameRef) {
		this.frameRef = frameRef;
		
		materials = new ArrayList<>();
		AgentUtils.materialsArray = materials;
		agents = new ArrayList<>();
		AgentUtils.agentsArray = agents;
		
		jade.core.Runtime runtime = jade.core.Runtime.instance();
		Profile profile = new ProfileImpl();
		profile.setParameter(Profile.CONTAINER_NAME, "AgentContainer");
		profile.setParameter(Profile.MAIN_HOST, "localhost");
		AgentUtils.agentContainer = runtime.createAgentContainer(profile);

		AgentUtils.createUpdaterAgent();
	    
	    MapPanel mapRef = this;
		
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0), "Pause");
	    this.getActionMap().put("Pause", new AbstractAction() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				if(isPausedMenuOpened || frameRef.isUsingSearch()) {
					return;
				}
				
				pause(false);
			}
	    });
	    
	    this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "OpenPauseMenu");
	    this.getActionMap().put("OpenPauseMenu", new AbstractAction() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(isPausedMenuOpened) {
					return;
				}
				
				wasPausedBeforePauseMenu = isPaused;
				pausedLabel.setVisible(false);
				isPaused = true;
				isPausedMenuOpened = true;
				frameRef.openPauseMenu();
			}
	    });
	}
	
	public void initMapPanel(JLayeredPane layeredPane) {
		layeredPane.add(this);
		parentPane = layeredPane;
		
		setBackground(Color.black);

		int panelWidth = tileWidth * tileColumns + tileWidth;
		int panelHeight = tileHeight* 4/3 * tileRows/2 + tileHeight/3 + tileHeight*4;
		setSize(new Dimension(panelWidth, panelHeight));
		
		pausedLabel = new JLabel("Paused");
		pausedLabel.setForeground(new Color(253,231,111,255));
		pausedLabel.setFont(new Font("VCR OSD MONO", pausedLabel.getFont().getStyle(), 50));
		pausedLabel.setBounds(layeredPane.getWidth()/2 - 100, 10, 200, 50);
		pausedLabel.setVisible(false);
		layeredPane.add(pausedLabel);
		
		initTiles();
		
		setupMouseListener();
		
		nextUpdateTime = System.currentTimeMillis();
		nextRepaintTime = System.currentTimeMillis();
		
		startMapThread();
	}
		
	public void initMapPanelFromMapTemplate(JLayeredPane layeredPane, String mapTemplateName) {
		initMapPanel(layeredPane);

		GameOntology.initOntology();
		
		if(mapTemplateName.contentEquals("Default")) {
			return;
		}
		
		try {
			File file = new File(".\\mapTemplates\\" + mapTemplateName);
			if(!file.exists()) {
				return;
			}
			
			FileInputStream fi = new FileInputStream(file);
			ObjectInputStream oi = new ObjectInputStream(fi);
			
			for(int i = 0; i<tileRows; i++) {
				for(int j = 0; j<tileColumns; j++) {
					
					String content = oi.readObject().toString();
					
					Tile tile = tiles[i][j];
					
					if(tile.getAgent() != null) {
						tile.deleteAgent();
					}
					
					String tileType = content.split(":")[0];
					tile.setTileType(TileType.valueOf(tileType));
					
					String collectableClass = content.split(":")[1];
					
					if(!collectableClass.contentEquals("null")) {
						collectableClass = collectableClass.split(Pattern.quote("."))[collectableClass.split(Pattern.quote(".")).length-1];
						
						switch (collectableClass) {
						case "StoneMaterial": {
							BaseMaterial material = new StoneMaterial();
							tile.setCollectable(material);
							materials.add(material);
							break;
						}
						case "IronMaterial": {
							BaseMaterial material = new IronMaterial();
							tile.setCollectable(material);
							materials.add(material);
							break;
						}
						case "CobaltMaterial": {
							BaseMaterial material = new CobaltMaterial();
							tile.setCollectable(material);
							materials.add(material);
							break;
						}
						case "MythrilMaterial": {
							BaseMaterial material = new MythrilMaterial();
							tile.setCollectable(material);
							materials.add(material);
							break;
						}
						case "AdamantiteMaterial": {
							BaseMaterial material = new AdamantiteMaterial();
							tile.setCollectable(material);
							materials.add(material);
							break;
						}
						case "DaybloomMaterial": {
							BaseMaterial material = new DaybloomMaterial();
							tile.setCollectable(material);
							materials.add(material);
							break;
						}
						case "MoonglowMaterial": {
							BaseMaterial material = new MoonglowMaterial();
							tile.setCollectable(material);
							materials.add(material);
							break;
						}
						case "ShadowstalkMaterial": {
							BaseMaterial material = new ShadowstalkMaterial();
							tile.setCollectable(material);
							materials.add(material);
							break;
						}
						case "BloodroseMaterial": {
							BaseMaterial material = new BloodroseMaterial();
							tile.setCollectable(material);
							materials.add(material);
							break;
						}
						case "WoodenChest": {
							BaseChest chest = new WoodenChest();
							tile.setCollectable(chest);
							break;
						}
						case "IronChest": {
							BaseChest chest = new IronChest();
							tile.setCollectable(chest);
							break;
						}
						case "GoldChest": {
							BaseChest chest = new GoldChest();
							tile.setCollectable(chest);
							break;
						}
						}
					}
					else {
						tile.deleteCollectable();
					}
				}
			}
			
			oi.close();
			fi.close();
			
			System.out.println("Loaded");
			
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
	}
	
	public void initMapPanelFromSave(JLayeredPane layeredPane, String saveName) {
		initMapPanel(layeredPane);
		
		File file = new File(".\\saves\\" + saveName);
		if(!file.exists()) {
			return;
		}
		
		file = new File(".\\saves\\" + saveName + "\\" + saveName);
		
		try {
			FileInputStream fi = new FileInputStream(file);
			ObjectInputStream oi = new ObjectInputStream(fi);
			
			GlobalSaveData globalData = (GlobalSaveData) oi.readObject();
			AgentUtils.updater.setSpawnShroomersCooldown(globalData.getSpawnShroomersCooldown());
			AgentUtils.agentCounter = globalData.getAgentCounter();
			
			ArrayList<TileSaveData> saves = new ArrayList<>();
			
			for(int i = 0; i<tileRows; i++) {
				for(int j = 0; j<tileColumns; j++) {
					
					TileSaveData saveData = (TileSaveData) oi.readObject();
					
					Tile tile = tiles[i][j];
					
					tile.setTileType(TileType.valueOf(saveData.getTileType()));
					
					String collectableClass = saveData.getCollectableClass();
					
					if(!collectableClass.contentEquals("null")) {
						collectableClass = collectableClass.split(Pattern.quote("."))[collectableClass.split(Pattern.quote(".")).length-1];
						
						switch (collectableClass) {
						case "StoneMaterial": {
							BaseMaterial material = new StoneMaterial();
							tile.setCollectable(material);
							materials.add(material);
							break;
						}
						case "IronMaterial": {
							BaseMaterial material = new IronMaterial();
							tile.setCollectable(material);
							materials.add(material);
							break;
						}
						case "CobaltMaterial": {
							BaseMaterial material = new CobaltMaterial();
							tile.setCollectable(material);
							materials.add(material);
							break;
						}
						case "MythrilMaterial": {
							BaseMaterial material = new MythrilMaterial();
							tile.setCollectable(material);
							materials.add(material);
							break;
						}
						case "AdamantiteMaterial": {
							BaseMaterial material = new AdamantiteMaterial();
							tile.setCollectable(material);
							materials.add(material);
							break;
						}
						case "DaybloomMaterial": {
							BaseMaterial material = new DaybloomMaterial();
							tile.setCollectable(material);
							materials.add(material);
							break;
						}
						case "MoonglowMaterial": {
							BaseMaterial material = new MoonglowMaterial();
							tile.setCollectable(material);
							materials.add(material);
							break;
						}
						case "ShadowstalkMaterial": {
							BaseMaterial material = new ShadowstalkMaterial();
							tile.setCollectable(material);
							materials.add(material);
							break;
						}
						case "BloodroseMaterial": {
							BaseMaterial material = new BloodroseMaterial();
							tile.setCollectable(material);
							materials.add(material);
							break;
						}
						case "WoodenChest": {
							BaseChest chest = new WoodenChest(saveData.getChestName(), saveData.getChestLoot());
							tile.setCollectable(chest);
							break;
						}
						case "IronChest": {
							BaseChest chest = new IronChest(saveData.getChestName(), saveData.getChestLoot());
							tile.setCollectable(chest);
							break;
						}
						case "GoldChest": {
							BaseChest chest = new GoldChest(saveData.getChestName(), saveData.getChestLoot());
							tile.setCollectable(chest);
							break;
						}
						}
						
						if(tile.getCollectable() instanceof BaseMaterial) {
							((BaseMaterial)tile.getCollectable()).setReplenishCounter(saveData.getMaterialReplenishCounter());
						}
					}
					
					if(saveData.getAgent() != null) {
						saves.add(saveData);
						
						BaseAgent agent = saveData.getAgent();
						AgentUtils.agentContainer.acceptNewAgent(agent.getLocalName(), agent);
						tile.setAgent(agent);
						agent.setCurrentTile(tile);
					}
				}
			}
			
			for (TileSaveData saveData : saves) {
				if(saveData.getAgent() != null) {
					saveData.getAgent().loadAgent(saveData);
				}
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}

		File ontoFile = new File(".\\saves\\" + saveName + "\\gameOntology.owl");
		File ontoCopyFile = new File("src/ontology/gameOntology.owl");
		
		if(ontoCopyFile.exists()) {
			ontoCopyFile.delete();
		}
		
		try {
			Files.copy(ontoFile.toPath(), ontoCopyFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		GameOntology.reloadOntology();
	}
	
	public void initTiles() {
		int xOffset = tileWidth/4;
		int yOffset = tileHeight/4;
		int oddRowXOffset = tileWidth/2;
		int oddRowYOffset = tileHeight*2/3+1;
		int oddAndEvenRowsAddedCounter = 0;
		tiles = new Tile[tileRows][tileColumns];
		for(int i = 0; i<tileRows; i++) {
			for(int j = 0; j<tileColumns; j++) {
				if(j%2 == 0) {
					tiles[i][j] = new Tile(xOffset+i*tileWidth,yOffset+(j-oddAndEvenRowsAddedCounter)*(tileHeight*4/3),tileWidth,tileHeight);
				}
				else {
					tiles[i][j] = new Tile(xOffset+i*tileWidth+oddRowXOffset,yOffset+(j-oddAndEvenRowsAddedCounter)*(tileHeight*4/3)-oddRowYOffset,tileWidth,tileHeight);
					oddAndEvenRowsAddedCounter++;
				}
			}
			oddAndEvenRowsAddedCounter = 0;
		}
		
		for(int i = 0; i<tileRows; i++) {
			for(int j = 0; j<tileColumns; j++) {
				Tile tempTile = tiles[i][j];
				if(j%2 == 0) {
					if(j!=0) {
						if(i!=0) {
							tempTile.setTopLeftNeighborTile(tiles[i-1][j-1]);
						}
						tempTile.setTopRightNeighborTile(tiles[i][j-1]);
					}
					if(i!=0) {
						tempTile.setBottomLeftNeighborTile(tiles[i-1][j+1]);
						tempTile.setLeftNeighborTile(tiles[i-1][j]);
					}
					tempTile.setBottomRightNeighborTile(tiles[i][j+1]);
					if(i!=tileRows-1) {
						tempTile.setRightNeighborTile(tiles[i+1][j]);
					}
				}
				else {
					tempTile.setTopLeftNeighborTile(tiles[i][j-1]);
					if(i!=0) {
						tempTile.setLeftNeighborTile(tiles[i-1][j]);
					}
					if(i!=tileRows-1) {
						tempTile.setTopRightNeighborTile(tiles[i+1][j-1]);
						tempTile.setRightNeighborTile(tiles[i+1][j]);
					}
					if(j!=tileColumns-1) {
						tempTile.setBottomLeftNeighborTile(tiles[i][j+1]);
						if(i!=tileRows-1) {
							tempTile.setBottomRightNeighborTile(tiles[i+1][j+1]);
						}
					}
				}
			}
		}
	}
	
	public void startMapThread() {
		mapThread = new Thread(this);
		mapThread.start();
	}

	@Override
	public void run() {
		while(mapThread != null) {
			if(needToUpdateLogic() && !isPausedMenuOpened) {
				update();
			}
			
			if(needToRepaint()) {
				repaint();
			}
		}
	}
	
	public boolean needToUpdateLogic() {
		 
	    if(nextUpdateTime <= System.currentTimeMillis()){
	        return true;
	    }
	 
	    return false;
	}
	
	public boolean needToRepaint() {
		 
	    if(nextRepaintTime <= System.currentTimeMillis()){
	    	nextRepaintTime =System.currentTimeMillis() + MILLIS_PER_SECOND / REPAINT_TIMES_PER_SECOND;
	        return true;
	    }
	 
	    return false;
	}
	
	public void update() {
		updateAgents();
		if(!isPaused) {
			updateMaterials();

			nextUpdateTime = System.currentTimeMillis() + Math.round(MILLIS_PER_SECOND / UPDATE_TIMES_PER_SECOND);
		}
	}
	
	private void updateMaterials() {
		for(int i = materials.size()-1; i>=0; i--) {
			BaseMaterial tempMaterial = materials.get(i);
			if(tempMaterial.getTile() == null) {
				materials.remove(tempMaterial);
			}
			else {
				tempMaterial.update();
			}
		}
	}
	
	private void updateAgents() {
		
		AgentUtils.updater.update(isPaused);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		repaintTiles(g);
		
		parentPane.moveToBack(this);
	}
	
	private void repaintTiles(Graphics g) {
		
		if(tiles == null) {
			return;
		}
		
		for(int j = 0; j<tileColumns; j++) {
			for(int i = 0; i<tileColumns; i++) {
				if(tiles[i][j] != null) {
					tiles[i][j].drawTile(g);
				}
			}
		}
	}
	
	private void setupMouseListener() {
		this.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				if(isPausedMenuOpened) {
					return;
				}
				
				if(SwingUtilities.isRightMouseButton(e)) {
					dragStartX = e.getX();
					dragStartY = e.getY();
				}
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {		
				if(isPausedMenuOpened) {
					return;
				}
				
				if(SwingUtilities.isLeftMouseButton(e)) {
					Tile tile = getClickedTile(e.getX(), e.getY());
					if(tile == null) {
						System.out.println("Didn't click a tile");
						return;
					}
					switch (ToolSelection.selectedTool) {
					case POINTER: {
						if(tile.getAgent() != null) {
							frameRef.showAgentInfo(tile.getAgent());
						}
						break;
					}
					case TILE: {
						tile.setTileType(ToolSelection.selectedTileType);
						break;
					}
					case MATERIAL: {
						if(tile.canPlaceObjectsOnTile()) {
							BaseMaterial material = null;
							switch (ToolSelection.selectedMaterialType) {
								case STONE: {
									material = new StoneMaterial();
									break;
								}
								case IRON:{
									material = new IronMaterial();
									break;
								}
								case COBALT:{
									material = new CobaltMaterial();
									break;
								}
								case MYTHRIL:{
									material = new MythrilMaterial();
									break;
								}
								case ADAMANTITE:{
									material = new AdamantiteMaterial();
									break;
								}
								case DAYBLOOM:{
									material = new DaybloomMaterial();
									break;
								}
								case MOONGLOW:{
									material = new MoonglowMaterial();
									break;
								}
								case SHADOWSTALK:{
									material = new ShadowstalkMaterial();
									break;
								}
								case BLOODROSE:{
									material = new BloodroseMaterial();
									break;
								}
							}
							materials.add(material);
							tile.setCollectable(material);
						}
						break;
					}
					case HERO: {
						if(tile.canPlaceObjectsOnTile() && tile.getAgent() == null) {
							switch (ToolSelection.selectedHeroType) {
								case WARRIOR: {
									AgentUtils.spawnAgent("Warrior", true, tile);
									break;
								}
								case ROGUE: {
									AgentUtils.spawnAgent("Rogue", true, tile);
									break;
								}
								case ARCHER: {
									AgentUtils.spawnAgent("Archer", true, tile);
									break;
								}
							}
						}
						break;
					}
					case MONSTER: {
						if(tile.canPlaceObjectsOnTile() && tile.getAgent() == null) {
							switch (ToolSelection.selectedMonsterType) {
							case SKELETON: {
								AgentUtils.spawnAgent("Skeleton", false, tile);
								break;
							}
							case SPIDER: {
								AgentUtils.spawnAgent("Spider", false, tile);
								break;
							}
							case SHROOMER: {
								AgentUtils.spawnAgent("Shroomer", false, tile);
								break;
							}
							case GOLEM: {
								AgentUtils.spawnAgent("Golem", false, tile);
								break;
							}
							}
						}
						break;
					}
					case STRUCTURE: {
						if(tile.canPlaceObjectsOnTile() && tile.getAgent() == null && tile.getOwningCity() == null) {
							switch (ToolSelection.selectedStructureType) {
							case CITY: {
								AgentUtils.spawnAgent("City", true, tile);
								break;
							}
							case GRAVEYARD: {
								AgentUtils.spawnAgent("Graveyard", false, tile);
								break;
							}
							case SPIDER_NEST: {
								AgentUtils.spawnAgent("SpiderNest", false, tile);
								break;
							}
							}
						}
						break;
					}
					case CHEST: {
						if(tile.canPlaceObjectsOnTile()) {
							BaseChest chest = null;
							switch (ToolSelection.selectedChestType) {
								case WOODEN_CHEST: {
									chest = new WoodenChest();
									break;
								}
								case IRON_CHEST: {
									chest = new IronChest();
									break;
								}
								case GOLD_CHEST: {
									chest = new GoldChest();
									break;
								}
							}
							tile.setCollectable(chest);
						}
						break;
					}
					}
				}
				else if(SwingUtilities.isRightMouseButton(e)) {
					Tile tile = getClickedTile(e.getX(), e.getY());
					if(tile == null) {
						System.out.println("Didn't click a tile");
						return;
					}
					tile.deleteContent();
				}
			}

		});
		
		MapPanel panelRef = this;
		this.addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseDragged(MouseEvent e) {
				if(isPausedMenuOpened) {
					return;
				}
				
				if(SwingUtilities.isLeftMouseButton(e)) {
					if(ToolSelection.selectedTool == Tool.TILE) {
						Tile tile = getClickedTile(e.getX(), e.getY());
						if(tile == null) {
							System.out.println("Didn't click a tile");
							return;
						}
						tile.setTileType(ToolSelection.selectedTileType);
					}
				}
				else if(SwingUtilities.isRightMouseButton(e)) {
					int dragDiffX = dragStartX - e.getX();
                    int dragDiffY = dragStartY - e.getY();
                    
                    int newX = panelRef.getX() - dragDiffX;
                    newX = Math.min(newX, 0);
                    newX = Math.max(newX, parentPane.getWidth() - panelRef.getWidth());
                    
                    int newY = panelRef.getY() - dragDiffY;
                    newY = Math.min(newY, 0);
                    newY = Math.max(newY, parentPane.getHeight() - panelRef.getHeight());
                    		
                    panelRef.setLocation(newX, newY);
				}
			}
		});
		
		this.addMouseWheelListener(new MouseWheelListener() {
			
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				if(isPausedMenuOpened) {
					return;
				}
				
				float widthPositionProportion;
				float heightPositionProportion;
				int newX = 0;
				int newY = 0;
				
				if(e.getWheelRotation() < 0) {
					if(zoomScale == MAX_ZOOM_SCALE) {
						return;
					}
					zoomScale = zoomScale*2;
					
					int mouseX = (int) MouseInfo.getPointerInfo().getLocation().getX();
					int mouseY = (int) MouseInfo.getPointerInfo().getLocation().getY();
					
					widthPositionProportion = panelRef.getX()/((float)panelRef.getWidth());
					heightPositionProportion = panelRef.getY()/((float)panelRef.getHeight());
					
					panelRef.setSize(panelRef.getWidth()*2, panelRef.getHeight()*2);
					
					newX = Math.round(widthPositionProportion*panelRef.getWidth()) - (mouseX*2 - parentPane.getWidth()/2);
					newY = Math.round(heightPositionProportion*panelRef.getHeight()) - (mouseY*2 - parentPane.getHeight()/2);
				}
				else {
					if(zoomScale == MIN_ZOOM_SCALE) {
						return;
					}
					zoomScale = zoomScale/2;
					
					widthPositionProportion = (panelRef.getX() + parentPane.getWidth()/2)/((float)panelRef.getWidth());
					heightPositionProportion = (panelRef.getY() + parentPane.getHeight()/2)/((float)panelRef.getHeight());
					
					panelRef.setSize(panelRef.getWidth()/2, panelRef.getHeight()/2);
					
					newX = Math.round(widthPositionProportion*panelRef.getWidth());
					newY = Math.round(heightPositionProportion*panelRef.getHeight());
				}

                newX = Math.min(newX, 0);
				newX = Math.max(newX, parentPane.getWidth() - panelRef.getWidth());
				
				newY = Math.min(newY, 0);
				newY = Math.max(newY, parentPane.getHeight() - panelRef.getHeight());
				
				panelRef.setLocation(newX, newY);
				
				for(int i = 0; i<tileRows; i++) {
					for(int j = 0; j<tileColumns; j++) {
						tiles[i][j].updateTileScale(zoomScale);
					}
				}
			}
		});
	}
	
	private Tile getClickedTile(int x, int y) {
		int row1 = 0, row2 = 0, row3 = 0, column1 = 0, column2 = 0, column3 = 0;
		
		for(int i = 0; i < tiles.length;i++) {
			Tile tempTile = tiles[0][i];
			if(y >= tempTile.getY() && y <= tempTile.getY() + tempTile.getHeight()) {
				row1 = i;
				if(i<tiles.length-1) {
					row2 = i+1;
				}
				else {
					row2 = row1;
				}
				if(i>0) {
					row3 = i-1;
				}
				else {
					row3 = row1;
				}
			}
		}
		
		for(int i = 0; i < tiles[0].length;i++) {
			Tile tempTile = tiles[i][0];
			if(x >= tempTile.getX() && x <= tempTile.getX() + tempTile.getWidth()) {
				column1 = i;
				if(i<tiles[0].length-1) {
					column2 = i+1;
				}
				else {
					column2 = column1;
				}
				if(i>0) {
					column3 = i-1;
				}
				else {
					column3 = column1;
				}
			}
		}
		
		for(int j = row3; j <= row2;j++) {
			for(int i = column3; i <= column2;i++) {
				if(tiles[i][j].isTileClicked(x, y)) {
					return tiles[i][j];
				}
			}
		}
		
		return null;
	}

	public void onClosedPauseMenu() {
		isPaused = wasPausedBeforePauseMenu;
		pausedLabel.setVisible(isPaused);
		isPausedMenuOpened = false;
	}

	public void abort() {
		isPaused = true;
		AgentUtils.destroyAll();
	}
	
	public void saveMapTemplate(String name) {
		try {			
			File file = new File(".\\mapTemplates\\" + name);
			if(file.exists()) {
				file.delete();
			}
			file.createNewFile();
			FileOutputStream fo = new FileOutputStream(file);
			ObjectOutputStream oo = new ObjectOutputStream(fo);
			
			
			for(int i = 0; i<tileRows; i++) {
				for(int j = 0; j<tileColumns; j++) {
					
					Tile tile = tiles[i][j];
					String tileType = tile.getTileType().toString();
					String collectableClass = "null";
					if(tile.getCollectable() != null) {
						collectableClass = tile.getCollectable().getClass().toString();
					}
					
					oo.writeObject(tileType + ":" + collectableClass);
				}
			}
			
			System.out.println("Saved");
			
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	public void saveGame(String name) {
		try {
			File saveFolder = new File(".\\saves");
			saveFolder.mkdirs();
			
			File folder = new File(".\\saves\\" + name);
			folder.mkdirs();
			
			File file = new File(".\\saves\\" + name + "\\" + name);
			if(file.exists()) {
				file.delete();
			}
			file.createNewFile();
			FileOutputStream fo = new FileOutputStream(file);
			ObjectOutputStream oo = new ObjectOutputStream(fo);
			
			int spawnShroomersCooldown = AgentUtils.updater.getSpawnShroomersCooldown();
			int agentCounter = AgentUtils.agentCounter;
			
			GlobalSaveData globalData = new GlobalSaveData(spawnShroomersCooldown, agentCounter);
			oo.writeObject(globalData);
			
			for(int i = 0; i<tileRows; i++) {
				for(int j = 0; j<tileColumns; j++) {
					
					Tile tile = tiles[i][j];
					String tileType = tile.getTileType().toString();
					String collectableClass = "null";
					int materialReplenishCounter = 0;
					String chestName = "";
					CollectableLoot chestLoot = null;
					if(tile.getCollectable() != null) {
						collectableClass = tile.getCollectable().getClass().toString();
						
						if(tile.getCollectable() instanceof BaseMaterial) {
							materialReplenishCounter = ((BaseMaterial)tile.getCollectable()).getReplenishCounter();
						}
						else {
							BaseChest tileChest = (BaseChest)tile.getCollectable();
							chestName = tileChest.getName();
							chestLoot = tileChest.getLoot();
						}
					}
					
					TileSaveData saveData;
					
					if(tile.getAgent() == null) {
						saveData = new TileSaveData(tileType, collectableClass, materialReplenishCounter, chestName, chestLoot, null);
					}
					else {
						saveData = new TileSaveData(tileType, collectableClass, materialReplenishCounter, chestName, chestLoot, tile.getAgent());
					}
					
					oo.writeObject(saveData);
				}
			}
			
			File ontoFile = new File("src/ontology/gameOntology.owl");
			File ontoCopyFile = new File(".\\saves\\" + name + "\\gameOntology.owl");
			
			if(ontoCopyFile.exists()) {
				ontoCopyFile.delete();
			}
			
			Files.copy(ontoFile.toPath(), ontoCopyFile.toPath());
			
			System.out.println("Saved");
			
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public void findAgent(String agentName) {
		
		BaseAgent agent = AgentUtils.getAgentByLocalName(agentName);
		Tile tile = agent.getCurrentTile();
		
		int tileX = tile.getX()+tile.getWidth()/2;
		int tileY = tile.getY()+tile.getHeight()/2;
		
		int newX = Math.round(tileX - parentPane.getWidth()/2) * -1;
		int newY = Math.round(tileY - parentPane.getHeight()/2) * -1;
		
        newX = Math.min(newX, 0);
		newX = Math.max(newX, parentPane.getWidth() - this.getWidth());
		
		newY = Math.min(newY, 0);
		newY = Math.max(newY, parentPane.getHeight() - this.getHeight());
		
		this.setLocation(newX, newY);
	}
	
	public void pause(boolean pausedFromMenuButton) {
		isPaused = !isPaused;
		pausedLabel.setVisible(isPaused);
		for (BaseAgent agent : agents) {
			agent.setPaused(isPaused);
		}
		
		if(!pausedFromMenuButton) {
			frameRef.updatePauseButton();
		}
	}
}
