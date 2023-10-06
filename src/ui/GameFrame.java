package ui;

import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

import agents.BaseAgent;
import agents.items.ItemUtils;
import ontology.GameOntology;
import ui.infoDialogs.AgentInfoPanel;
import ui.toolsPanel.ToolsPanel;

public class GameFrame extends JFrame{

	private JLayeredPane layeredPane;
	private MapPanel mapPanel;
	private ToolsPanel toolsPanel;
	
	private SearchPanel searchPanel;
	private boolean isUsingSearch;
	
	private AgentInfoPanel infoPanel;
	
	private MainMenu mainMenu;
	private NewGamePanel newGamePanel;
	private LoadGamePanel loadGamePanel;
	private MenuBackground menuBackground;
	
	private PauseMenu pauseMenu;
	private SaveMapMenu saveMapMenu;
	private SaveMapConfirmationWindow saveMapConfirm;
	private SaveGameMenu saveGameMenu;
	private SaveGameConfirmationWindow saveGameConfirm;
	private ControlsPanel controlsMenu;
	
	public GameFrame() {
		this.setExtendedState(JFrame.MAXIMIZED_BOTH); 
		this.setUndecorated(true);
		this.setVisible(true);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setResizable(false);
		this.setTitle("Hero Survival");
		this.getContentPane().setBackground(Color.black);
		
		loadNecessaryData();
		
		layeredPane = new JLayeredPane();
		layeredPane.setSize(this.getSize());
		this.add(layeredPane);

		menuBackground = new MenuBackground(this);
		layeredPane.add(menuBackground);
		
		mainMenu = new MainMenu(this);
		layeredPane.add(mainMenu);
		
		layeredPane.moveToBack(menuBackground);
	}
	
	private void loadNecessaryData() {
		GameOntology.tempInitOntology();
		ItemUtils.initPrices();
		ItemUtils.initCraftingRecipies();
	}

	public void openPauseMenu() {
		layeredPane.add(pauseMenu);
		layeredPane.moveToFront(pauseMenu);
		pauseMenu.requestFocusInWindow();
	}
	
	public void closePauseMenu() {
		layeredPane.remove(pauseMenu);
		mapPanel.onClosedPauseMenu();
	}

	public void toMainMenu() {
		mapPanel.abort();
		layeredPane.remove(mapPanel);
		layeredPane.remove(pauseMenu);
		layeredPane.remove(toolsPanel);
		layeredPane.remove(searchPanel);
		layeredPane.remove(infoPanel);
		mapPanel = null;
		pauseMenu = null;
		toolsPanel = null;
		searchPanel.stop();
		searchPanel = null;
		infoPanel.close();
		infoPanel = null;
		layeredPane.add(mainMenu);
		layeredPane.add(menuBackground);
	}

	public void openNewGameTab() {
		newGamePanel = new NewGamePanel(this);
		layeredPane.add(newGamePanel);
		layeredPane.moveToBack(menuBackground);
		layeredPane.remove(mainMenu);
	}

	public void startNewGame(String mapTemplateName) {
		layeredPane.remove(mainMenu);
		layeredPane.remove(newGamePanel);
		layeredPane.remove(menuBackground);
		newGamePanel = null;
		
		mapPanel = new MapPanel(this);
		mapPanel.initMapPanelFromMapTemplate(layeredPane, mapTemplateName);
		
		toolsPanel = new ToolsPanel();
		toolsPanel.initToolsPanel(layeredPane);
		
		searchPanel = new SearchPanel(this);
		layeredPane.add(searchPanel);
		layeredPane.moveToFront(searchPanel);
		
		infoPanel = new AgentInfoPanel(this);
		layeredPane.add(infoPanel);
		layeredPane.moveToFront(infoPanel);
		
		pauseMenu = new PauseMenu(this);
	}

	public void backToMainMenu() {
		if(newGamePanel != null) {
			layeredPane.remove(newGamePanel);
			newGamePanel = null;
		}
		if(loadGamePanel != null) {
			layeredPane.remove(loadGamePanel);
			loadGamePanel = null;
		}
		layeredPane.add(mainMenu);
		layeredPane.moveToBack(menuBackground);
	}
	
	public void saveMapTemplate(String name) {
		mapPanel.saveMapTemplate(name);
	}
	
	public void saveGame(String name) {
		mapPanel.saveGame(name);
	}

	public void openLoadGameTab() {
		loadGamePanel = new LoadGamePanel(this);
		layeredPane.add(loadGamePanel);
		layeredPane.remove(mainMenu);
		layeredPane.moveToBack(menuBackground);
	}

	public void loadGame(String selectedSave) {
		layeredPane.remove(mainMenu);
		layeredPane.remove(loadGamePanel);
		layeredPane.remove(menuBackground);
		loadGamePanel = null;
		
		mapPanel = new MapPanel(this);
		mapPanel.initMapPanelFromSave(layeredPane, selectedSave);
		
		toolsPanel = new ToolsPanel();
		toolsPanel.initToolsPanel(layeredPane);
		
		searchPanel = new SearchPanel(this);
		layeredPane.add(searchPanel);
		layeredPane.moveToFront(searchPanel);
		
		infoPanel = new AgentInfoPanel(this);
		layeredPane.add(infoPanel);
		layeredPane.moveToFront(infoPanel);
		
		pauseMenu = new PauseMenu(this);
	}

	public boolean isUsingSearch() {
		return isUsingSearch;
	}

	public void setUsingSearch(boolean isUsingSearch) {
		this.isUsingSearch = isUsingSearch;
	}

	public void findAgent(String agentName) {
		if(mapPanel != null) {
			mapPanel.findAgent(agentName);
		}
	}

	public void pauseMap() {
		if(mapPanel != null) {
			mapPanel.pause(true);
		}
	}

	public void updatePauseButton() {
		searchPanel.updatePauseButton();
	}
	
	public void openSaveMapTab() {
		saveMapMenu = new SaveMapMenu(this);
		layeredPane.add(saveMapMenu);
		layeredPane.remove(pauseMenu);
		layeredPane.moveToFront(saveMapMenu);
	}
	
	public void openSaveGameTab() {
		saveGameMenu = new SaveGameMenu(this);
		layeredPane.add(saveGameMenu);
		layeredPane.remove(pauseMenu);
		layeredPane.moveToFront(saveGameMenu);
	}
	
	public void openControlsMenu() {
		controlsMenu = new ControlsPanel(this);
		layeredPane.add(controlsMenu);
		layeredPane.remove(pauseMenu);
		layeredPane.moveToFront(controlsMenu);
	}

	public void backToPauseMenu() {
		if(saveMapMenu != null) {
			layeredPane.remove(saveMapMenu);
			saveMapMenu = null;
		}
		if(saveMapConfirm != null) {
			layeredPane.remove(saveMapConfirm);
			saveMapConfirm = null;
		}
		if(saveGameMenu != null) {
			layeredPane.remove(saveGameMenu);
			saveGameMenu = null;
		}
		if(saveGameConfirm != null) {
			layeredPane.remove(saveGameConfirm);
			saveGameConfirm = null;
		}
		if(controlsMenu != null) {
			layeredPane.remove(controlsMenu);
			controlsMenu = null;
		}
		layeredPane.add(pauseMenu);
		layeredPane.moveToFront(pauseMenu);
	}

	public void addSaveMapConfirmationWindow(String mapName) {
		saveMapConfirm = new SaveMapConfirmationWindow(this, mapName);
		layeredPane.add(saveMapConfirm);
		layeredPane.moveToFront(saveMapConfirm);
	}

	public void removeSaveMapConfirmationWindow() {
		if(saveMapConfirm != null) {
			layeredPane.remove(saveMapConfirm);
			saveMapConfirm = null;
		}
		saveMapMenu.enableButtons();
	}
	
	public void addSaveGameConfirmationWindow(String mapName) {
		saveGameConfirm = new SaveGameConfirmationWindow(this, mapName);
		layeredPane.add(saveGameConfirm);
		layeredPane.moveToFront(saveGameConfirm);
	}

	public void removeSaveGameConfirmationWindow() {
		if(saveGameConfirm != null) {
			layeredPane.remove(saveGameConfirm);
			saveGameConfirm = null;
		}
		saveGameMenu.enableButtons();
	}
	
	public void showAgentInfo(BaseAgent agent) {
		infoPanel.show(agent);
	}
}
