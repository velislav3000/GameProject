package ui.toolsPanel;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import agents.BaseStructureAgent.StructureType;
import agents.heroAgents.BaseHeroAgent.HeroAbilityTrait;
import agents.heroAgents.BaseHeroAgent.HeroPersonalityTrait;
import agents.heroAgents.BaseHeroAgent.HeroType;
import agents.monsterAgents.BaseMonsterAgent.MonsterTrait;
import agents.monsterAgents.BaseMonsterAgent.MonsterType;
import collectiables.chests.BaseChest.ChestType;
import collectiables.materials.BaseMaterial.MaterialType;
import selection.ToolSelection;
import selection.ToolSelection.Tool;
import tiles.Tile.TileType;

public class ToolsPanel extends JTabbedPane{

	private ArrayList<ToolButton> tileButtons = new ArrayList<>();
	private ArrayList<ToolButton> heroTypeButtons = new ArrayList<>();
	private ArrayList<ToolButton> heroPersonalityButtons = new ArrayList<>();
	private ArrayList<ToolButton> heroAbilityButtons = new ArrayList<>();
	private ArrayList<ToolButton> monsterTypeButtons = new ArrayList<>();
	private ArrayList<ToolButton> monsterTraitButtons = new ArrayList<>();
	private ArrayList<ToolButton> materialButtons = new ArrayList<>();
	private ArrayList<ToolButton> chestTypeButtons = new ArrayList<>();
	
	private String basePath;
	
	public void initToolsPanel(JLayeredPane layeredPane) {
		layeredPane.add(this);
		layeredPane.moveToFront(this);
		this.setFont(new Font("VCR OSD MONO", 1, 20));
		this.setForeground(new Color(253,231,111,255));
		this.setBackground(Color.black);
		
		this.setUI(new ToolPanelUI());
		
		this.setBounds(0, layeredPane.getHeight()*17/20, layeredPane.getWidth(), layeredPane.getHeight()*3/20);
		
		try {
			basePath = new File(".").getCanonicalPath() + "/res";
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		setUpTilesMenu();
		setUpHeroesMenu();
		setUpMonstersMenu();
		setUpMaterialsMenu();
		setUpChestsMenu();
		
		JTabbedPane self = this;
		
		this.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				
				ToolSelection.selectedTool = Tool.POINTER;
				
			    switch (self.getSelectedIndex()) {
				case 0: {
					for (ToolButton button : tileButtons) {
						button.setActivated(false);
					}
					break;
				}
				case 1: {
					for (ToolButton button : heroTypeButtons) {
						button.setActivated(false);
					}
					
					for (ToolButton button : heroPersonalityButtons) {
						button.setActivated(false);
					}
					ToolSelection.selectedHeroPersonalityTrait = HeroPersonalityTrait.RANDOM;
					
					for (ToolButton button : heroAbilityButtons) {
						button.setActivated(false);
					}
					ToolSelection.selectedHeroAbilityTraits.clear();
					break;
				}
				case 2: {
					for (ToolButton button : monsterTypeButtons) {
						button.setActivated(false);
					}
					
					for (ToolButton button : monsterTraitButtons) {
						button.setActivated(false);
					}
					ToolSelection.selectedMonsterTraits.clear();
					break;
				}
				case 3: {
					for (ToolButton button : materialButtons) {
						button.setActivated(false);
					}
					break;
				}
				case 4: {
					for (ToolButton button : chestTypeButtons) {
						button.setActivated(false);
					}
					break;
				}
				}
			}
		});
	}
	
	private void setUpTilesMenu() {
		
		String tileIconsPath = basePath + "/tileIcons/";
		
		JPanel tilesPanel = new JPanel();
		this.add("Tiles", tilesPanel);
		tilesPanel.setBackground(Color.black);
		tilesPanel.setLayout(null);
		
		ToolButton grassTileButton = new ToolButton(75);
		grassTileButton.setIcon(new ImageIcon(tileIconsPath + "tile_grass.png"));
		String tooltip = "<html>Grass Tile<br><br>Heroes and Monsters can move to it.</html>";
		grassTileButton.setToolTipText(tooltip);
		tilesPanel.add(grassTileButton);
		tileButtons.add(grassTileButton);
		grassTileButton.setBounds(10, 25, 80, 80);
		grassTileButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(grassTileButton.isActivated()) {
					ToolSelection.selectedTool = Tool.POINTER;
					grassTileButton.setActivated(false);
				}
				else {
					setSelectedButton(grassTileButton, tileButtons);
					ToolSelection.selectedTool = Tool.TILE;
					ToolSelection.selectedTileType = TileType.GRASS;
				}
			}
		});
		
		ToolButton forestTileButton = new ToolButton(75);
		forestTileButton.setIcon(new ImageIcon(tileIconsPath + "tile_forest.png"));
		tooltip = "<html>Forest Tile<br><br>Heroes and Monsters can move to it, but their perception is restricted to one tile next to them.</html>";
		forestTileButton.setToolTipText(tooltip);
		tilesPanel.add(forestTileButton);
		tileButtons.add(forestTileButton);
		forestTileButton.setBounds(110, 25, 80, 80);
		forestTileButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(forestTileButton.isActivated()) {
					ToolSelection.selectedTool = Tool.POINTER;
					forestTileButton.setActivated(false);
				}
				else {
					setSelectedButton(forestTileButton, tileButtons);
					ToolSelection.selectedTool = Tool.TILE;
					ToolSelection.selectedTileType = TileType.FOREST;
				}
			}
		});
		
		ToolButton mountainTileButton = new ToolButton(75);
		mountainTileButton.setIcon(new ImageIcon(tileIconsPath + "tile_mountain.png"));
		tooltip = "<html>Mountain Tile<br><br>It blocks Heroes and Monsters' movement and it also blocks the three tiles behind it from their perception.</html>";
		mountainTileButton.setToolTipText(tooltip);
		tilesPanel.add(mountainTileButton);
		tileButtons.add(mountainTileButton);
		mountainTileButton.setBounds(210, 25, 80, 80);
		mountainTileButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(mountainTileButton.isActivated()) {
					ToolSelection.selectedTool = Tool.POINTER;
					mountainTileButton.setActivated(false);
				}
				else {
					setSelectedButton(mountainTileButton, tileButtons);
					ToolSelection.selectedTool = Tool.TILE;
					ToolSelection.selectedTileType = TileType.MOUNTAIN;
				}
			}
		});
		
		ToolButton waterTileButton = new ToolButton(75);
		waterTileButton.setIcon(new ImageIcon(tileIconsPath + "tile_water.png"));
		tooltip = "<html>Water Tile<br><br>It blocks Heroes and Monsters' movement.</html>";
		waterTileButton.setToolTipText(tooltip);
		tilesPanel.add(waterTileButton);
		tileButtons.add(waterTileButton);
		waterTileButton.setBounds(310, 25, 80, 80);
		waterTileButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(waterTileButton.isActivated()) {
					ToolSelection.selectedTool = Tool.POINTER;
					waterTileButton.setActivated(false);
				}
				else {
					setSelectedButton(waterTileButton, tileButtons);
					ToolSelection.selectedTool = Tool.TILE;
					ToolSelection.selectedTileType = TileType.WATER;
				}
			}
		});
		
		ToolButton sandTileButton = new ToolButton(75);
		sandTileButton.setIcon(new ImageIcon(tileIconsPath + "tile_sand.png"));
		tooltip = "<html>Sand Tile<br><br>Has the atributes of a Grass Tile.<br>"
				+ "A Sand Tile turns into a Burning Sand Tile when surrounded from all sides by either Sand or Burning Sand Tiles.<br>"
				+ "When moving to a Burning Sand Tiles, Heroes and Monsters take 5-10 damage.</html>";
		sandTileButton.setToolTipText(tooltip);
		tilesPanel.add(sandTileButton);
		tileButtons.add(sandTileButton);
		sandTileButton.setBounds(410, 25, 80, 80);
		sandTileButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(sandTileButton.isActivated()) {
					ToolSelection.selectedTool = Tool.POINTER;
					sandTileButton.setActivated(false);
				}
				else {
					setSelectedButton(sandTileButton, tileButtons);
					ToolSelection.selectedTool = Tool.TILE;
					ToolSelection.selectedTileType = TileType.SAND;
				}
			}
		});
		
		ToolButton snowTileButton = new ToolButton(75);
		snowTileButton.setIcon(new ImageIcon(tileIconsPath + "tile_snow.png"));
		tooltip = "<html>Snow Tile<br><br>Has the atributes of a Grass Tile.<br>"
				+ "A Snow Tile turns into an Ice Tile when surrounded from all sides by either Snow or Ice Tiles.<br>"
				+ "When on an Ice Tile, there's a 20% chance for the Hero or Monster to be unable to move.</html>";
		snowTileButton.setToolTipText(tooltip);
		tilesPanel.add(snowTileButton);
		tileButtons.add(snowTileButton);
		snowTileButton.setBounds(510, 25, 80, 80);
		snowTileButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(snowTileButton.isActivated()) {
					ToolSelection.selectedTool = Tool.POINTER;
					snowTileButton.setActivated(false);
				}
				else {
					setSelectedButton(snowTileButton, tileButtons);
					ToolSelection.selectedTool = Tool.TILE;
					ToolSelection.selectedTileType = TileType.SNOW;
				}
			}
		});
		
		ToolButton mushroomTileButton = new ToolButton(75);
		mushroomTileButton.setIcon(new ImageIcon(tileIconsPath + "tile_mushroom.png"));
		tooltip = "<html>Mushroom Tile<br><br>Has the atributes of a Forest Tile.<br>"
				+ "Every 10 turns for every 10 Mushroom tiles on the map, a Shroomer will spawn on a random Mushroom Tile.<br>"
				+ "The minimum amount of Shroomers that can spawn at once is 1 and the maximum is 10.</html>";
		mushroomTileButton.setToolTipText(tooltip);
		tilesPanel.add(mushroomTileButton);
		tileButtons.add(mushroomTileButton);
		mushroomTileButton.setBounds(610, 25, 80, 80);
		mushroomTileButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(mushroomTileButton.isActivated()) {
					ToolSelection.selectedTool = Tool.POINTER;
					mushroomTileButton.setActivated(false);
				}
				else {
					setSelectedButton(mushroomTileButton, tileButtons);
					ToolSelection.selectedTool = Tool.TILE;
					ToolSelection.selectedTileType = TileType.MUSHROOM;
				}
			}
		});
	}
	
	private void setUpHeroesMenu() {
		String heroIconsPath = basePath + "/agentIcons/";
		
		JPanel heroesPanel = new JPanel();
		this.add("Heroes", heroesPanel);
		heroesPanel.setBackground(Color.black);
		heroesPanel.setLayout(null);
		
		ToolButton warriorHeroButton = new ToolButton(75);
		warriorHeroButton.setIcon(new ImageIcon(heroIconsPath + "agent_warrior.png"));
		String tooltip = "<html>Warrior<br><br>Hero with higher health and lower attack. Uses swords, shields and hammers for combat.</html>";
		warriorHeroButton.setToolTipText(tooltip);
		heroesPanel.add(warriorHeroButton);
		heroTypeButtons.add(warriorHeroButton);
		warriorHeroButton.setBounds(10, 25, 80, 80);
		warriorHeroButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(warriorHeroButton.isActivated()) {
					ToolSelection.selectedTool = Tool.POINTER;
					warriorHeroButton.setActivated(false);
				}
				else {
					setSelectedButton(warriorHeroButton, heroTypeButtons);
					ToolSelection.selectedTool = Tool.HERO;
					ToolSelection.selectedHeroType = HeroType.WARRIOR;
				}
			}
		});
		
		ToolButton rogueHeroButton = new ToolButton(75);
		rogueHeroButton.setIcon(new ImageIcon(heroIconsPath + "agent_rogue.png"));
		tooltip = "<html>Rogue<br><br>Hero with higher attack and lower health. Uses daggers and can wield two at a time.</html>";
		rogueHeroButton.setToolTipText(tooltip);
		heroesPanel.add(rogueHeroButton);
		heroTypeButtons.add(rogueHeroButton);
		rogueHeroButton.setBounds(110, 25, 80, 80);
		rogueHeroButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(rogueHeroButton.isActivated()) {
					ToolSelection.selectedTool = Tool.POINTER;
					rogueHeroButton.setActivated(false);
				}
				else {
					setSelectedButton(rogueHeroButton, heroTypeButtons);
					ToolSelection.selectedTool = Tool.HERO;
					ToolSelection.selectedHeroType = HeroType.ROGUE;
				}
			}
		});
		
		ToolButton archerHeroButton = new ToolButton(75);
		archerHeroButton.setIcon(new ImageIcon(heroIconsPath + "agent_archer.png"));
		tooltip = "<html>Archer<br><br>Hero with medium attack and health. Uses bows in combat and prefers to fight at range.</html>";
		archerHeroButton.setToolTipText(tooltip);
		heroesPanel.add(archerHeroButton);
		heroTypeButtons.add(archerHeroButton);
		archerHeroButton.setBounds(210, 25, 80, 80);
		archerHeroButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(archerHeroButton.isActivated()) {
					ToolSelection.selectedTool = Tool.POINTER;
					archerHeroButton.setActivated(false);
				}
				else {
					setSelectedButton(archerHeroButton, heroTypeButtons);
					ToolSelection.selectedTool = Tool.HERO;
					ToolSelection.selectedHeroType = HeroType.ARCHER;
				}
			}
		});
		
		String heroTraitPath = basePath + "/agentTraitIcons/";
		
		JLabel personalityLabel = new JLabel("Personality traits");
		personalityLabel.setFont(new Font("VCR OSD MONO", 1, 12));
		personalityLabel.setForeground(new Color(253,231,111,255));
		personalityLabel.setBounds(410, 5, 200, 20);
		heroesPanel.add(personalityLabel);
		
		ToolButton bloodthirstyPersonalityButton = new ToolButton(25);
		bloodthirstyPersonalityButton.setIcon(new ImageIcon(heroTraitPath + "trait_bloodthirsty.png"));
		tooltip = "<html>Bloodthirst<br><br>Will make the hero more likely to attack others.</html>";
		bloodthirstyPersonalityButton.setToolTipText(tooltip);
		heroesPanel.add(bloodthirstyPersonalityButton);
		heroPersonalityButtons.add(bloodthirstyPersonalityButton);
		bloodthirstyPersonalityButton.setBounds(410, 25, 30, 30);
		bloodthirstyPersonalityButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(bloodthirstyPersonalityButton.isActivated()) {
					ToolSelection.selectedHeroPersonalityTrait = HeroPersonalityTrait.RANDOM;
					bloodthirstyPersonalityButton.setActivated(false);
				}
				else {
					setSelectedButton(bloodthirstyPersonalityButton, heroPersonalityButtons);
					ToolSelection.selectedHeroPersonalityTrait = HeroPersonalityTrait.BLOODTHIRSTY;
				}
			}
		});
		
		ToolButton greedyPersonalityButton = new ToolButton(25);
		greedyPersonalityButton.setIcon(new ImageIcon(heroTraitPath + "trait_greedy.png"));
		tooltip = "<html>Greed<br><br>Will make the hero desire more money during trades.</html>";
		greedyPersonalityButton.setToolTipText(tooltip);
		heroesPanel.add(greedyPersonalityButton);
		heroPersonalityButtons.add(greedyPersonalityButton);
		greedyPersonalityButton.setBounds(410, 75, 30, 30);
		greedyPersonalityButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(greedyPersonalityButton.isActivated()) {
					ToolSelection.selectedHeroPersonalityTrait = HeroPersonalityTrait.RANDOM;
					greedyPersonalityButton.setActivated(false);
				}
				else {
					setSelectedButton(greedyPersonalityButton, heroPersonalityButtons);
					ToolSelection.selectedHeroPersonalityTrait = HeroPersonalityTrait.GREEDY;
				}
			}
		});
		
		ToolButton bravePersonalityButton = new ToolButton(25);
		bravePersonalityButton.setIcon(new ImageIcon(heroTraitPath + "trait_brave.png"));
		tooltip = "<html>Brave<br><br>Will make the hero less likely to flee from combat.</html>";
		bravePersonalityButton.setToolTipText(tooltip);
		heroesPanel.add(bravePersonalityButton);
		heroPersonalityButtons.add(bravePersonalityButton);
		bravePersonalityButton.setBounds(460, 25, 30, 30);
		bravePersonalityButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(bravePersonalityButton.isActivated()) {
					ToolSelection.selectedHeroPersonalityTrait = HeroPersonalityTrait.RANDOM;
					bravePersonalityButton.setActivated(false);
				}
				else {
					setSelectedButton(bravePersonalityButton, heroPersonalityButtons);
					ToolSelection.selectedHeroPersonalityTrait = HeroPersonalityTrait.BRAVE;
				}
			}
		});
		
		ToolButton cowardPersonalityButton = new ToolButton(25);
		cowardPersonalityButton.setIcon(new ImageIcon(heroTraitPath + "trait_coward.png"));
		tooltip = "<html>Coward<br><br>Will make the hero more likely to flee from combat.</html>";
		cowardPersonalityButton.setToolTipText(tooltip);
		heroesPanel.add(cowardPersonalityButton);
		heroPersonalityButtons.add(cowardPersonalityButton);
		cowardPersonalityButton.setBounds(460, 75, 30, 30);
		cowardPersonalityButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(cowardPersonalityButton.isActivated()) {
					ToolSelection.selectedHeroPersonalityTrait = HeroPersonalityTrait.RANDOM;
					cowardPersonalityButton.setActivated(false);
				}
				else {
					setSelectedButton(cowardPersonalityButton, heroPersonalityButtons);
					ToolSelection.selectedHeroPersonalityTrait = HeroPersonalityTrait.COWARD;
				}
			}
		});
		
		JLabel abilityLabel = new JLabel("Ability traits");
		abilityLabel.setBounds(660, 5, 200, 20);
		abilityLabel.setFont(new Font("VCR OSD MONO", 1, 12));
		abilityLabel.setForeground(new Color(253,231,111,255));
		heroesPanel.add(abilityLabel);
		
		ToolButton tankAbilityButton = new ToolButton(25);
		ToolButton nimbleAbilityButton = new ToolButton(25);
		ToolButton preciseAbilityButton = new ToolButton(25);
		ToolButton wildAbilityButton = new ToolButton(25);
		ToolButton blacksmithAbilityButton = new ToolButton(25);
		ToolButton forgeDisasterAbilityButton = new ToolButton(25);
		ToolButton alchemistAbilityButton = new ToolButton(25);
		ToolButton herbWasterAbilityButton = new ToolButton(25);
		ToolButton resourcefulAbilityButton = new ToolButton(25);
		ToolButton poisonDablerAbilityButton = new ToolButton(25);
		ToolButton frostInfuserAbilityButton = new ToolButton(25);
		ToolButton antivenomAbilityButton = new ToolButton(25);
		ToolButton burningBloodAbilityButton = new ToolButton(25);
		ToolButton lastStandAbilityButton = new ToolButton(25);
		ToolButton fastLearnerAbilityButton = new ToolButton(25);
		ToolButton dimwitAbilityButton = new ToolButton(25);
		ToolButton postDeathBitternessAbilityButton = new ToolButton(25);
		ToolButton randomAbilityButton = new ToolButton(75);
		heroAbilityButtons.add(tankAbilityButton);
		heroAbilityButtons.add(nimbleAbilityButton);
		heroAbilityButtons.add(preciseAbilityButton);
		heroAbilityButtons.add(wildAbilityButton);
		heroAbilityButtons.add(blacksmithAbilityButton);
		heroAbilityButtons.add(forgeDisasterAbilityButton);
		heroAbilityButtons.add(alchemistAbilityButton);
		heroAbilityButtons.add(herbWasterAbilityButton);
		heroAbilityButtons.add(resourcefulAbilityButton);
		heroAbilityButtons.add(poisonDablerAbilityButton);
		heroAbilityButtons.add(frostInfuserAbilityButton);
		heroAbilityButtons.add(antivenomAbilityButton);
		heroAbilityButtons.add(burningBloodAbilityButton);
		heroAbilityButtons.add(lastStandAbilityButton);
		heroAbilityButtons.add(fastLearnerAbilityButton);
		heroAbilityButtons.add(dimwitAbilityButton);
		heroAbilityButtons.add(postDeathBitternessAbilityButton);
		heroAbilityButtons.add(randomAbilityButton);

		tankAbilityButton.setIcon(new ImageIcon(heroTraitPath + "trait_tank.png"));
		tooltip = "<html>Tank<br><br>Increases the hero's health, but decreases their evasion.</html>";
		tankAbilityButton.setToolTipText(tooltip);
		heroesPanel.add(tankAbilityButton);
		tankAbilityButton.setBounds(660, 25, 30, 30);
		tankAbilityButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(tankAbilityButton.isActivated()) {
					ToolSelection.selectedHeroAbilityTraits.remove(HeroAbilityTrait.TANK);
					tankAbilityButton.setActivated(false);
				}
				else {
					tankAbilityButton.setActivated(true);
					nimbleAbilityButton.setActivated(false);
					ToolSelection.selectedHeroAbilityTraits.add(HeroAbilityTrait.TANK);
					ToolSelection.selectedHeroAbilityTraits.remove(HeroAbilityTrait.NIMBLE);
					
					ToolSelection.selectedHeroAbilityTraits.remove(HeroAbilityTrait.RANDOM);
					randomAbilityButton.setActivated(false);
				}
			}
		});
		
		nimbleAbilityButton.setIcon(new ImageIcon(heroTraitPath + "trait_nimble.png"));
		tooltip = "<html>Nimble<br><br>Increases the hero's evasion, but decreases their health.</html>";
		nimbleAbilityButton.setToolTipText(tooltip);
		heroesPanel.add(nimbleAbilityButton);
		nimbleAbilityButton.setBounds(660, 75, 30, 30);
		nimbleAbilityButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(nimbleAbilityButton.isActivated()) {
					ToolSelection.selectedHeroAbilityTraits.remove(HeroAbilityTrait.NIMBLE);
					nimbleAbilityButton.setActivated(false);
				}
				else {
					tankAbilityButton.setActivated(false);
					nimbleAbilityButton.setActivated(true);
					ToolSelection.selectedHeroAbilityTraits.remove(HeroAbilityTrait.TANK);
					ToolSelection.selectedHeroAbilityTraits.add(HeroAbilityTrait.NIMBLE);
					
					ToolSelection.selectedHeroAbilityTraits.remove(HeroAbilityTrait.RANDOM);
					randomAbilityButton.setActivated(false);
				}
			}
		});
		
		preciseAbilityButton.setIcon(new ImageIcon(heroTraitPath + "trait_precise.png"));
		tooltip = "<html>Precise<br><br>Increases the hero's critical hit chance, but decreases their attack.</html>";
		preciseAbilityButton.setToolTipText(tooltip);
		heroesPanel.add(preciseAbilityButton);
		preciseAbilityButton.setBounds(710, 25, 30, 30);
		preciseAbilityButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(preciseAbilityButton.isActivated()) {
					ToolSelection.selectedHeroAbilityTraits.remove(HeroAbilityTrait.PRECISE);
					preciseAbilityButton.setActivated(false);
				}
				else {
					preciseAbilityButton.setActivated(true);
					wildAbilityButton.setActivated(false);
					ToolSelection.selectedHeroAbilityTraits.add(HeroAbilityTrait.PRECISE);
					ToolSelection.selectedHeroAbilityTraits.remove(HeroAbilityTrait.WILD);
					
					ToolSelection.selectedHeroAbilityTraits.remove(HeroAbilityTrait.RANDOM);
					randomAbilityButton.setActivated(false);
				}
			}
		});
		
		wildAbilityButton.setIcon(new ImageIcon(heroTraitPath + "trait_wild.png"));
		tooltip = "<html>Wild<br><br>Increases the hero's attack, but decreases their critical hit chance.</html>";
		wildAbilityButton.setToolTipText(tooltip);
		heroesPanel.add(wildAbilityButton);
		wildAbilityButton.setBounds(710, 75, 30, 30);
		wildAbilityButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(wildAbilityButton.isActivated()) {
					ToolSelection.selectedHeroAbilityTraits.remove(HeroAbilityTrait.WILD);
					wildAbilityButton.setActivated(false);
				}
				else {
					preciseAbilityButton.setActivated(false);
					wildAbilityButton.setActivated(true);
					ToolSelection.selectedHeroAbilityTraits.remove(HeroAbilityTrait.PRECISE);
					ToolSelection.selectedHeroAbilityTraits.add(HeroAbilityTrait.WILD);
					
					ToolSelection.selectedHeroAbilityTraits.remove(HeroAbilityTrait.RANDOM);
					randomAbilityButton.setActivated(false);
				}
			}
		});
		
		blacksmithAbilityButton.setIcon(new ImageIcon(heroTraitPath + "trait_blacksmith.png"));
		tooltip = "<html>Blacksmith<br><br>Increases the crafted equipment's quality by one.</html>";
		blacksmithAbilityButton.setToolTipText(tooltip);
		heroesPanel.add(blacksmithAbilityButton);
		blacksmithAbilityButton.setBounds(760, 25, 30, 30);
		blacksmithAbilityButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(blacksmithAbilityButton.isActivated()) {
					ToolSelection.selectedHeroAbilityTraits.remove(HeroAbilityTrait.BLACKSMITH);
					blacksmithAbilityButton.setActivated(false);
				}
				else {
					blacksmithAbilityButton.setActivated(true);
					forgeDisasterAbilityButton.setActivated(false);
					ToolSelection.selectedHeroAbilityTraits.add(HeroAbilityTrait.BLACKSMITH);
					ToolSelection.selectedHeroAbilityTraits.remove(HeroAbilityTrait.FORGE_DISASTER);
					
					ToolSelection.selectedHeroAbilityTraits.remove(HeroAbilityTrait.RANDOM);
					randomAbilityButton.setActivated(false);
				}
			}
		});
		
		forgeDisasterAbilityButton.setIcon(new ImageIcon(heroTraitPath + "trait_forge_disaster.png"));
		tooltip = "<html>Forge Disaster<br><br>Decreases the crafted equipment's quality by one.</html>";
		forgeDisasterAbilityButton.setToolTipText(tooltip);
		heroesPanel.add(forgeDisasterAbilityButton);
		forgeDisasterAbilityButton.setBounds(760, 75, 30, 30);
		forgeDisasterAbilityButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(forgeDisasterAbilityButton.isActivated()) {
					ToolSelection.selectedHeroAbilityTraits.remove(HeroAbilityTrait.FORGE_DISASTER);
					forgeDisasterAbilityButton.setActivated(false);
				}
				else {
					forgeDisasterAbilityButton.setActivated(true);
					blacksmithAbilityButton.setActivated(false);
					ToolSelection.selectedHeroAbilityTraits.add(HeroAbilityTrait.FORGE_DISASTER);
					ToolSelection.selectedHeroAbilityTraits.remove(HeroAbilityTrait.BLACKSMITH);
					
					ToolSelection.selectedHeroAbilityTraits.remove(HeroAbilityTrait.RANDOM);
					randomAbilityButton.setActivated(false);
				}
			}
		});
		
		alchemistAbilityButton.setIcon(new ImageIcon(heroTraitPath + "trait_alchemist.png"));
		tooltip = "<html>Alchemist<br><br>Increases the crafted potion's quality by one.</html>";
		alchemistAbilityButton.setToolTipText(tooltip);
		heroesPanel.add(alchemistAbilityButton);
		alchemistAbilityButton.setBounds(810, 25, 30, 30);
		alchemistAbilityButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(alchemistAbilityButton.isActivated()) {
					ToolSelection.selectedHeroAbilityTraits.remove(HeroAbilityTrait.ALCHEMIST);
					alchemistAbilityButton.setActivated(false);
				}
				else {
					alchemistAbilityButton.setActivated(true);
					herbWasterAbilityButton.setActivated(false);
					ToolSelection.selectedHeroAbilityTraits.add(HeroAbilityTrait.ALCHEMIST);
					ToolSelection.selectedHeroAbilityTraits.remove(HeroAbilityTrait.HERB_WASTER);
					
					ToolSelection.selectedHeroAbilityTraits.remove(HeroAbilityTrait.RANDOM);
					randomAbilityButton.setActivated(false);
				}
			}
		});
		
		herbWasterAbilityButton.setIcon(new ImageIcon(heroTraitPath + "trait_herb_waster.png"));
		tooltip = "<html>Herb Waster<br><br>Decreases the crafted potion's quality by one.</html>";
		herbWasterAbilityButton.setToolTipText(tooltip);
		heroesPanel.add(herbWasterAbilityButton);
		herbWasterAbilityButton.setBounds(810, 75, 30, 30);
		herbWasterAbilityButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(herbWasterAbilityButton.isActivated()) {
					ToolSelection.selectedHeroAbilityTraits.remove(HeroAbilityTrait.HERB_WASTER);
					herbWasterAbilityButton.setActivated(false);
				}
				else {
					herbWasterAbilityButton.setActivated(true);
					alchemistAbilityButton.setActivated(false);
					ToolSelection.selectedHeroAbilityTraits.add(HeroAbilityTrait.HERB_WASTER);
					ToolSelection.selectedHeroAbilityTraits.remove(HeroAbilityTrait.ALCHEMIST);
					
					ToolSelection.selectedHeroAbilityTraits.remove(HeroAbilityTrait.RANDOM);
					randomAbilityButton.setActivated(false);
				}
			}
		});
		
		resourcefulAbilityButton.setIcon(new ImageIcon(heroTraitPath + "trait_resourceful.png"));
		tooltip = "<html>Resourceful<br><br>Has a 25% chance to use half of the required materials when crafting.</html>";
		resourcefulAbilityButton.setToolTipText(tooltip);
		heroesPanel.add(resourcefulAbilityButton);
		resourcefulAbilityButton.setBounds(860, 25, 30, 30);
		resourcefulAbilityButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(resourcefulAbilityButton.isActivated()) {
					ToolSelection.selectedHeroAbilityTraits.remove(HeroAbilityTrait.RESOURCEFUL);
					resourcefulAbilityButton.setActivated(false);
				}
				else {
					resourcefulAbilityButton.setActivated(true);
					ToolSelection.selectedHeroAbilityTraits.add(HeroAbilityTrait.RESOURCEFUL);
					
					ToolSelection.selectedHeroAbilityTraits.remove(HeroAbilityTrait.RANDOM);
					randomAbilityButton.setActivated(false);
				}
			}
		});
		
		poisonDablerAbilityButton.setIcon(new ImageIcon(heroTraitPath + "trait_poison_dabler.png"));
		tooltip = "<html>Poison Dabler<br><br>Has a 10% chance to inflict poison for 3 turns when attacking.</html>";
		poisonDablerAbilityButton.setToolTipText(tooltip);
		heroesPanel.add(poisonDablerAbilityButton);
		poisonDablerAbilityButton.setBounds(860, 75, 30, 30);
		poisonDablerAbilityButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(poisonDablerAbilityButton.isActivated()) {
					ToolSelection.selectedHeroAbilityTraits.remove(HeroAbilityTrait.POISON_DABLER);
					poisonDablerAbilityButton.setActivated(false);
				}
				else {
					poisonDablerAbilityButton.setActivated(true);
					ToolSelection.selectedHeroAbilityTraits.add(HeroAbilityTrait.POISON_DABLER);
					
					ToolSelection.selectedHeroAbilityTraits.remove(HeroAbilityTrait.RANDOM);
					randomAbilityButton.setActivated(false);
				}
			}
		});
		
		frostInfuserAbilityButton.setIcon(new ImageIcon(heroTraitPath + "trait_frost_infuser.png"));
		tooltip = "<html>Frost Infuser<br><br>Has a 5% chance to freeze the enemy for 1 turn when attacking,<br>"
				+ "preventing it from acting.</html>";
		frostInfuserAbilityButton.setToolTipText(tooltip);
		heroesPanel.add(frostInfuserAbilityButton);
		frostInfuserAbilityButton.setBounds(910, 25, 30, 30);
		frostInfuserAbilityButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(frostInfuserAbilityButton.isActivated()) {
					ToolSelection.selectedHeroAbilityTraits.remove(HeroAbilityTrait.FROST_INFUSER);
					frostInfuserAbilityButton.setActivated(false);
				}
				else {
					frostInfuserAbilityButton.setActivated(true);
					ToolSelection.selectedHeroAbilityTraits.add(HeroAbilityTrait.FROST_INFUSER);
					
					ToolSelection.selectedHeroAbilityTraits.remove(HeroAbilityTrait.RANDOM);
					randomAbilityButton.setActivated(false);
				}
			}
		});
		
		antivenomAbilityButton.setIcon(new ImageIcon(heroTraitPath + "trait_antivenom.png"));
		tooltip = "<html>Antivenom<br><br>Makes hero immune to poison.</html>";
		antivenomAbilityButton.setToolTipText(tooltip);
		heroesPanel.add(antivenomAbilityButton);
		antivenomAbilityButton.setBounds(910, 75, 30, 30);
		antivenomAbilityButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(antivenomAbilityButton.isActivated()) {
					ToolSelection.selectedHeroAbilityTraits.remove(HeroAbilityTrait.ANTIVENOM);
					antivenomAbilityButton.setActivated(false);
				}
				else {
					antivenomAbilityButton.setActivated(true);
					ToolSelection.selectedHeroAbilityTraits.add(HeroAbilityTrait.ANTIVENOM);
					
					ToolSelection.selectedHeroAbilityTraits.remove(HeroAbilityTrait.RANDOM);
					randomAbilityButton.setActivated(false);
				}
			}
		});
		
		burningBloodAbilityButton.setIcon(new ImageIcon(heroTraitPath + "trait_burning_blood.png"));
		tooltip = "<html>Burning Blood<br><br>Makes hero immune to freezing.</html>";
		burningBloodAbilityButton.setToolTipText(tooltip);
		heroesPanel.add(burningBloodAbilityButton);
		burningBloodAbilityButton.setBounds(960, 25, 30, 30);
		burningBloodAbilityButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(burningBloodAbilityButton.isActivated()) {
					ToolSelection.selectedHeroAbilityTraits.remove(HeroAbilityTrait.BURNING_BLOOD);
					burningBloodAbilityButton.setActivated(false);
				}
				else {
					burningBloodAbilityButton.setActivated(true);
					ToolSelection.selectedHeroAbilityTraits.add(HeroAbilityTrait.BURNING_BLOOD);
					
					ToolSelection.selectedHeroAbilityTraits.remove(HeroAbilityTrait.RANDOM);
					randomAbilityButton.setActivated(false);
				}
			}
		});
		
		lastStandAbilityButton.setIcon(new ImageIcon(heroTraitPath + "trait_last_stand.png"));
		tooltip = "<html>Last Stand<br><br>When a hero is about to die, their health is restored to 10% instead.</html>";
		lastStandAbilityButton.setToolTipText(tooltip);
		heroesPanel.add(lastStandAbilityButton);
		lastStandAbilityButton.setBounds(960, 75, 30, 30);
		lastStandAbilityButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(lastStandAbilityButton.isActivated()) {
					ToolSelection.selectedHeroAbilityTraits.remove(HeroAbilityTrait.LAST_STAND);
					lastStandAbilityButton.setActivated(false);
				}
				else {
					lastStandAbilityButton.setActivated(true);
					ToolSelection.selectedHeroAbilityTraits.add(HeroAbilityTrait.LAST_STAND);
					
					ToolSelection.selectedHeroAbilityTraits.remove(HeroAbilityTrait.RANDOM);
					randomAbilityButton.setActivated(false);
				}
			}
		});
		
		fastLearnerAbilityButton.setIcon(new ImageIcon(heroTraitPath + "trait_fast_learner.png"));
		tooltip = "<html>Fast Learner<br><br>Earns double XP.</html>";
		fastLearnerAbilityButton.setToolTipText(tooltip);
		heroesPanel.add(fastLearnerAbilityButton);
		fastLearnerAbilityButton.setBounds(1010, 25, 30, 30);
		fastLearnerAbilityButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(fastLearnerAbilityButton.isActivated()) {
					ToolSelection.selectedHeroAbilityTraits.remove(HeroAbilityTrait.FAST_LEARNER);
					fastLearnerAbilityButton.setActivated(false);
				}
				else {
					fastLearnerAbilityButton.setActivated(true);
					dimwitAbilityButton.setActivated(false);
					ToolSelection.selectedHeroAbilityTraits.add(HeroAbilityTrait.FAST_LEARNER);
					ToolSelection.selectedHeroAbilityTraits.remove(HeroAbilityTrait.DIMWIT);
					
					ToolSelection.selectedHeroAbilityTraits.remove(HeroAbilityTrait.RANDOM);
					randomAbilityButton.setActivated(false);
				}
			}
		});
		
		dimwitAbilityButton.setIcon(new ImageIcon(heroTraitPath + "trait_dimwit.png"));
		tooltip = "<html>Dimwit<br><br>Earns half XP.</html>";
		dimwitAbilityButton.setToolTipText(tooltip);
		heroesPanel.add(dimwitAbilityButton);
		dimwitAbilityButton.setBounds(1010, 75, 30, 30);
		dimwitAbilityButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(dimwitAbilityButton.isActivated()) {
					ToolSelection.selectedHeroAbilityTraits.remove(HeroAbilityTrait.DIMWIT);
					dimwitAbilityButton.setActivated(false);
				}
				else {
					dimwitAbilityButton.setActivated(true);
					fastLearnerAbilityButton.setActivated(false);
					ToolSelection.selectedHeroAbilityTraits.add(HeroAbilityTrait.DIMWIT);
					ToolSelection.selectedHeroAbilityTraits.remove(HeroAbilityTrait.FAST_LEARNER);
					
					ToolSelection.selectedHeroAbilityTraits.remove(HeroAbilityTrait.RANDOM);
					randomAbilityButton.setActivated(false);
				}
			}
		});
		
		postDeathBitternessAbilityButton.setIcon(new ImageIcon(heroTraitPath + "trait_post_death_bitterness.png"));
		tooltip = "<html>Post Death Bitterness<br><br>On death, hero destroys his own inventory, so it can't be taken by others.</html>";
		postDeathBitternessAbilityButton.setToolTipText(tooltip);
		heroesPanel.add(postDeathBitternessAbilityButton);
		postDeathBitternessAbilityButton.setBounds(1060, 25, 30, 30);
		postDeathBitternessAbilityButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(postDeathBitternessAbilityButton.isActivated()) {
					ToolSelection.selectedHeroAbilityTraits.remove(HeroAbilityTrait.POST_DEATH_BITTERNESS);
					postDeathBitternessAbilityButton.setActivated(false);
				}
				else {
					postDeathBitternessAbilityButton.setActivated(true);
					ToolSelection.selectedHeroAbilityTraits.add(HeroAbilityTrait.POST_DEATH_BITTERNESS);
					
					ToolSelection.selectedHeroAbilityTraits.remove(HeroAbilityTrait.RANDOM);
					randomAbilityButton.setActivated(false);
				}
			}
		});
		
		randomAbilityButton.setIcon(new ImageIcon(heroTraitPath + "trait_random.png"));
		tooltip = "<html>Random<br><br>Ability traits will be chosen at random.</html>";
		randomAbilityButton.setToolTipText(tooltip);
		heroesPanel.add(randomAbilityButton);
		randomAbilityButton.setBounds(1110, 25, 80, 80);
		randomAbilityButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(randomAbilityButton.isActivated()) {
					ToolSelection.selectedHeroAbilityTraits.remove(HeroAbilityTrait.RANDOM);
					randomAbilityButton.setActivated(false);
				}
				else {
					setSelectedButton(randomAbilityButton, heroAbilityButtons);
					ToolSelection.selectedHeroAbilityTraits.clear();
					ToolSelection.selectedHeroAbilityTraits.add(HeroAbilityTrait.RANDOM);
				}
			}
		});
		
		JLabel structuresLabel = new JLabel("Structures");
		structuresLabel.setBounds(1260, 5, 200, 20);
		structuresLabel.setFont(new Font("VCR OSD MONO", 1, 12));
		structuresLabel.setForeground(new Color(253,231,111,255));
		heroesPanel.add(structuresLabel);
		
		ToolButton cityStructureButton = new ToolButton(75);
		cityStructureButton.setIcon(new ImageIcon(heroIconsPath + "agent_city.png"));
		tooltip = "<html>City<br><br>A hero structure that collects material around itself, crafts items and makes transactions with nearby heroes.<br>"
				+ "If a hero owns the city, half of it's aquired loot will be sent to the hero.</html>";
		cityStructureButton.setToolTipText(tooltip);
		heroesPanel.add(cityStructureButton);
		heroTypeButtons.add(cityStructureButton);
		cityStructureButton.setBounds(1260, 25, 80, 80);
		cityStructureButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(cityStructureButton.isActivated()) {
					ToolSelection.selectedTool = Tool.POINTER;
					cityStructureButton.setActivated(false);
				}
				else {
					setSelectedButton(cityStructureButton, heroTypeButtons);
					ToolSelection.selectedTool = Tool.STRUCTURE;
					ToolSelection.selectedStructureType = StructureType.CITY;
				}
			}
		});
	}
	
	private void setUpMonstersMenu() {
		String monsterIconsPath = basePath + "/agentIcons/";
		
		JPanel monstersPanel = new JPanel();
		this.add("Monsters", monstersPanel);
		monstersPanel.setBackground(Color.black);
		monstersPanel.setLayout(null);
		
		ToolButton skeletonMonsterButton = new ToolButton(75);
		skeletonMonsterButton.setIcon(new ImageIcon(monsterIconsPath + "agent_skeleton.png"));
		String tooltip = "<html>Skeleton<br><br>A monster that wonders around and attacks heroes.<br>"
				+ "Has a 20% chance to turn into a skull when killed and revive itself after a few turns.<br>"
				+ "The skull can't be attacked.<br>"
				+ "A Boss Skeleton summons a Skeleton when it kills an enemy that isn't a Skeleton.</html>";
		skeletonMonsterButton.setToolTipText(tooltip);
		monstersPanel.add(skeletonMonsterButton);
		monsterTypeButtons.add(skeletonMonsterButton);
		skeletonMonsterButton.setBounds(10, 25, 80, 80);
		skeletonMonsterButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(skeletonMonsterButton.isActivated()) {
					ToolSelection.selectedTool = Tool.POINTER;
					skeletonMonsterButton.setActivated(false);
				}
				else {
					setSelectedButton(skeletonMonsterButton, monsterTypeButtons);
					ToolSelection.selectedTool = Tool.MONSTER;
					ToolSelection.selectedMonsterType = MonsterType.SKELETON;
				}
			}
		});
		
		ToolButton spiderMonsterButton = new ToolButton(75);
		spiderMonsterButton.setIcon(new ImageIcon(monsterIconsPath + "agent_spider.png"));
		tooltip = "<html>Spider<br><br>A weak monster that wonders around and attacks heroes. Drops String on death.<br>"
				+ "Has a chance to lay an egg, which will hatch into a spider after a few turns. The egg can be attacked.<br>"
				+ "A Boss Spider has a chance to poison it's enemy when attacking.</html>";
		spiderMonsterButton.setToolTipText(tooltip);
		monstersPanel.add(spiderMonsterButton);
		monsterTypeButtons.add(spiderMonsterButton);
		spiderMonsterButton.setBounds(110, 25, 80, 80);
		spiderMonsterButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(spiderMonsterButton.isActivated()) {
					ToolSelection.selectedTool = Tool.POINTER;
					spiderMonsterButton.setActivated(false);
				}
				else {
					setSelectedButton(spiderMonsterButton, monsterTypeButtons);
					ToolSelection.selectedTool = Tool.MONSTER;
					ToolSelection.selectedMonsterType = MonsterType.SPIDER;
				}
			}
		});
		
		ToolButton shroomerMonsterButton = new ToolButton(75);
		shroomerMonsterButton.setIcon(new ImageIcon(monsterIconsPath + "agent_shroomer.png"));
		tooltip = "<html>Shroomer<br><br>A weak monster that wonders around and attacks heroes.<br>"
				+ "Every 10 turns it will turn the tile it's on into a Mushroom tile.<br>"
				+ "A Boss Shroomer will instead turn the tile it's on and 2 neighbouring tiles into Mushroom tiles<br>"
				+ "every 7 turns.</html>";
		shroomerMonsterButton.setToolTipText(tooltip);
		monstersPanel.add(shroomerMonsterButton);
		monsterTypeButtons.add(shroomerMonsterButton);
		shroomerMonsterButton.setBounds(210, 25, 80, 80);
		shroomerMonsterButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(shroomerMonsterButton.isActivated()) {
					ToolSelection.selectedTool = Tool.POINTER;
					shroomerMonsterButton.setActivated(false);
				}
				else {
					setSelectedButton(shroomerMonsterButton, monsterTypeButtons);
					ToolSelection.selectedTool = Tool.MONSTER;
					ToolSelection.selectedMonsterType = MonsterType.SHROOMER;
				}
			}
		});
		
		ToolButton golemMonsterButton = new ToolButton(75);
		golemMonsterButton.setIcon(new ImageIcon(monsterIconsPath + "agent_golem_stone.png"));
		tooltip = "<html>Golem<br><br>A monster that wonders around, attacks heroes and eats materials.<br>"
				+ "It comes in 5 grades: Stone, Iron, Cobalt, Mythril and Adamantite.<br>"
				+ "To advance to the next grade, it needs to consume 6 materials of the same name as the rank.<br>"
				+ "Once dead it will leave a material that coresponds to it's grade.<br>"
				+ "A boss golem needs to consume 10 materials to advance it's grade, but every 15 turns<br>"
				+ "it spawns the material it's made of.</html>";
		golemMonsterButton.setToolTipText(tooltip);
		monstersPanel.add(golemMonsterButton);
		monsterTypeButtons.add(golemMonsterButton);
		golemMonsterButton.setBounds(310, 25, 80, 80);
		golemMonsterButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(golemMonsterButton.isActivated()) {
					ToolSelection.selectedTool = Tool.POINTER;
					golemMonsterButton.setActivated(false);
				}
				else {
					setSelectedButton(golemMonsterButton, monsterTypeButtons);
					ToolSelection.selectedTool = Tool.MONSTER;
					ToolSelection.selectedMonsterType = MonsterType.GOLEM;
				}
			}
		});
		
		JLabel traitsLabel = new JLabel("Traits");
		traitsLabel.setBounds(510, 5, 200, 20);
		traitsLabel.setFont(new Font("VCR OSD MONO", 1, 12));
		traitsLabel.setForeground(new Color(253,231,111,255));
		monstersPanel.add(traitsLabel);
		
		String monsterTraitPath = basePath + "/agentTraitIcons/";
		
		ToolButton ferociousTraitButton = new ToolButton(25);
		ToolButton tameTraitButton = new ToolButton(25);
		ToolButton armoredTraitButton = new ToolButton(25);
		ToolButton fragileTraitButton = new ToolButton(25);
		ToolButton detonatorTraitButton = new ToolButton(25);
		ToolButton moneybagsTraitButton = new ToolButton(25);
		ToolButton burningWastelandTraitButton = new ToolButton(25);
		ToolButton frozenWastelandTraitButton = new ToolButton(25);
		ToolButton bossTraitButton = new ToolButton(25);
		ToolButton randomTraitButton = new ToolButton(75);
		monsterTraitButtons.add(ferociousTraitButton);
		monsterTraitButtons.add(tameTraitButton);
		monsterTraitButtons.add(armoredTraitButton);
		monsterTraitButtons.add(fragileTraitButton);
		monsterTraitButtons.add(detonatorTraitButton);
		monsterTraitButtons.add(moneybagsTraitButton);
		monsterTraitButtons.add(burningWastelandTraitButton);
		monsterTraitButtons.add(frozenWastelandTraitButton);
		monsterTraitButtons.add(bossTraitButton);
		monsterTraitButtons.add(randomTraitButton);

		ferociousTraitButton.setIcon(new ImageIcon(monsterTraitPath + "trait_ferocious.png"));
		tooltip = "<html>Ferocious<br><br>Increases a monster's attack.</html>";
		ferociousTraitButton.setToolTipText(tooltip);
		monstersPanel.add(ferociousTraitButton);
		ferociousTraitButton.setBounds(510, 25, 30, 30);
		ferociousTraitButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(ferociousTraitButton.isActivated()) {
					ToolSelection.selectedMonsterTraits.remove(MonsterTrait.FEROCIOUS);
					ferociousTraitButton.setActivated(false);
				}
				else {
					ferociousTraitButton.setActivated(true);
					tameTraitButton.setActivated(false);
					ToolSelection.selectedMonsterTraits.add(MonsterTrait.FEROCIOUS);
					ToolSelection.selectedMonsterTraits.remove(MonsterTrait.TAME);
					
					ToolSelection.selectedMonsterTraits.remove(MonsterTrait.RANDOM);
					randomTraitButton.setActivated(false);
				}
			}
		});
		
		tameTraitButton.setIcon(new ImageIcon(monsterTraitPath + "trait_tame.png"));
		tooltip = "<html>Tame<br><br>Decreases a monster's attack.</html>";
		tameTraitButton.setToolTipText(tooltip);
		monstersPanel.add(tameTraitButton);
		tameTraitButton.setBounds(510, 75, 30, 30);
		tameTraitButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(tameTraitButton.isActivated()) {
					ToolSelection.selectedMonsterTraits.remove(MonsterTrait.TAME);
					tameTraitButton.setActivated(false);
				}
				else {
					ferociousTraitButton.setActivated(false);
					tameTraitButton.setActivated(true);
					ToolSelection.selectedMonsterTraits.remove(MonsterTrait.FEROCIOUS);
					ToolSelection.selectedMonsterTraits.add(MonsterTrait.TAME);
					
					ToolSelection.selectedMonsterTraits.remove(MonsterTrait.RANDOM);
					randomTraitButton.setActivated(false);
				}
			}
		});
		
		armoredTraitButton.setIcon(new ImageIcon(monsterTraitPath + "trait_armored.png"));
		tooltip = "<html>Armored<br><br>Increases a monster's health.</html>";
		armoredTraitButton.setToolTipText(tooltip);
		monstersPanel.add(armoredTraitButton);
		armoredTraitButton.setBounds(560, 25, 30, 30);
		armoredTraitButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(armoredTraitButton.isActivated()) {
					ToolSelection.selectedMonsterTraits.remove(MonsterTrait.ARMORED);
					armoredTraitButton.setActivated(false);
				}
				else {
					armoredTraitButton.setActivated(true);
					fragileTraitButton.setActivated(false);
					ToolSelection.selectedMonsterTraits.add(MonsterTrait.ARMORED);
					ToolSelection.selectedMonsterTraits.remove(MonsterTrait.FRAGILE);
					
					ToolSelection.selectedMonsterTraits.remove(MonsterTrait.RANDOM);
					randomTraitButton.setActivated(false);
				}
			}
		});
		
		fragileTraitButton.setIcon(new ImageIcon(monsterTraitPath + "trait_fragile.png"));
		tooltip = "<html>Fragile<br><br>Decreases a monster's health.</html>";
		fragileTraitButton.setToolTipText(tooltip);
		monstersPanel.add(fragileTraitButton);
		fragileTraitButton.setBounds(560, 75, 30, 30);
		fragileTraitButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(fragileTraitButton.isActivated()) {
					ToolSelection.selectedMonsterTraits.remove(MonsterTrait.FRAGILE);
					fragileTraitButton.setActivated(false);
				}
				else {
					armoredTraitButton.setActivated(false);
					fragileTraitButton.setActivated(true);
					ToolSelection.selectedMonsterTraits.remove(MonsterTrait.ARMORED);
					ToolSelection.selectedMonsterTraits.add(MonsterTrait.FRAGILE);
					
					ToolSelection.selectedMonsterTraits.remove(MonsterTrait.RANDOM);
					randomTraitButton.setActivated(false);
				}
			}
		});
		
		detonatorTraitButton.setIcon(new ImageIcon(monsterTraitPath + "trait_detonator.png"));
		tooltip = "<html>Detonator<br><br>On death, the monster deals 3 times it's attack to all heroes and monsters next to it.</html>";
		detonatorTraitButton.setToolTipText(tooltip);
		monstersPanel.add(detonatorTraitButton);
		detonatorTraitButton.setBounds(610, 25, 30, 30);
		detonatorTraitButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(detonatorTraitButton.isActivated()) {
					ToolSelection.selectedMonsterTraits.remove(MonsterTrait.DETONATOR);
					detonatorTraitButton.setActivated(false);
				}
				else {
					detonatorTraitButton.setActivated(true);
					ToolSelection.selectedMonsterTraits.add(MonsterTrait.DETONATOR);
					
					ToolSelection.selectedMonsterTraits.remove(MonsterTrait.RANDOM);
					randomTraitButton.setActivated(false);
				}
			}
		});
		
		moneybagsTraitButton.setIcon(new ImageIcon(monsterTraitPath + "trait_moneybags.png"));
		tooltip = "<html>Moneybags<br><br>Monster drops 3 times more loot.</html>";
		moneybagsTraitButton.setToolTipText(tooltip);
		monstersPanel.add(moneybagsTraitButton);
		moneybagsTraitButton.setBounds(610, 75, 30, 30);
		moneybagsTraitButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(moneybagsTraitButton.isActivated()) {
					ToolSelection.selectedMonsterTraits.remove(MonsterTrait.MONEYBAGS);
					moneybagsTraitButton.setActivated(false);
				}
				else {
					moneybagsTraitButton.setActivated(true);
					ToolSelection.selectedMonsterTraits.add(MonsterTrait.MONEYBAGS);
					
					ToolSelection.selectedMonsterTraits.remove(MonsterTrait.RANDOM);
					randomTraitButton.setActivated(false);
				}
			}
		});
		
		burningWastelandTraitButton.setIcon(new ImageIcon(monsterTraitPath + "trait_burning_wasteland.png"));
		tooltip = "<html>Burning Wasteland<br><br>Monster has a 10% chance to inflict Burn when attacking.<br>"
				+ "After reaching level 10, every turn the monster will turn it's current tile to a Sand tile.<br>"
				+ "The tile changing effect doesn't work on Shroomers.</html>";
		burningWastelandTraitButton.setToolTipText(tooltip);
		monstersPanel.add(burningWastelandTraitButton);
		burningWastelandTraitButton.setBounds(660, 25, 30, 30);
		burningWastelandTraitButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(burningWastelandTraitButton.isActivated()) {
					ToolSelection.selectedMonsterTraits.remove(MonsterTrait.BURNING_WASTELAND);
					burningWastelandTraitButton.setActivated(false);
				}
				else {
					burningWastelandTraitButton.setActivated(true);
					frozenWastelandTraitButton.setActivated(false);
					ToolSelection.selectedMonsterTraits.add(MonsterTrait.BURNING_WASTELAND);
					ToolSelection.selectedMonsterTraits.remove(MonsterTrait.FROZEN_WASTELAND);
					
					ToolSelection.selectedMonsterTraits.remove(MonsterTrait.RANDOM);
					randomTraitButton.setActivated(false);
				}
			}
		});
		
		frozenWastelandTraitButton.setIcon(new ImageIcon(monsterTraitPath + "trait_frozen_wasteland.png"));
		tooltip = "<html>Frozen Wasteland<br><br>Monster has a 7% chance to inflict Freeze when attacking.<br>"
				+ "After reaching level 10, every turn the monster will turn it's current tile to a Snow tile.<br>"
				+ "The tile changing effect doesn't work on Shroomers.</html>";
		frozenWastelandTraitButton.setToolTipText(tooltip);
		monstersPanel.add(frozenWastelandTraitButton);
		frozenWastelandTraitButton.setBounds(660, 75, 30, 30);
		frozenWastelandTraitButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(frozenWastelandTraitButton.isActivated()) {
					ToolSelection.selectedMonsterTraits.remove(MonsterTrait.FROZEN_WASTELAND);
					frozenWastelandTraitButton.setActivated(false);
				}
				else {
					frozenWastelandTraitButton.setActivated(true);
					burningWastelandTraitButton.setActivated(false);
					ToolSelection.selectedMonsterTraits.add(MonsterTrait.FROZEN_WASTELAND);
					ToolSelection.selectedMonsterTraits.remove(MonsterTrait.BURNING_WASTELAND);
					
					ToolSelection.selectedMonsterTraits.remove(MonsterTrait.RANDOM);
					randomTraitButton.setActivated(false);
				}
			}
		});
		
		bossTraitButton.setIcon(new ImageIcon(monsterTraitPath + "trait_boss.png"));
		tooltip = "<html>Boss<br><br>Increases monster's attack and health by 3 times and gives it a new ability.<br>"
				+ "All non-boss monsters of the same type will be Allies.<br>"
				+ "All boss monsters of the same type will be Enemies</html>";
		bossTraitButton.setToolTipText(tooltip);
		monstersPanel.add(bossTraitButton);
		bossTraitButton.setBounds(710, 25, 30, 30);
		bossTraitButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(bossTraitButton.isActivated()) {
					ToolSelection.selectedMonsterTraits.remove(MonsterTrait.BOSS);
					bossTraitButton.setActivated(false);
				}
				else {
					bossTraitButton.setActivated(true);
					ToolSelection.selectedMonsterTraits.add(MonsterTrait.BOSS);
					
					ToolSelection.selectedMonsterTraits.remove(MonsterTrait.RANDOM);
					randomTraitButton.setActivated(false);
				}
			}
		});
		
		randomTraitButton.setIcon(new ImageIcon(monsterTraitPath + "trait_random.png"));
		tooltip = "<html>Random<br><br>Traits will be chosen at random.</html>";
		randomTraitButton.setToolTipText(tooltip);
		monstersPanel.add(randomTraitButton);
		randomTraitButton.setBounds(760, 25, 80, 80);
		randomTraitButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(randomTraitButton.isActivated()) {
					ToolSelection.selectedMonsterTraits.remove(MonsterTrait.RANDOM);
					randomTraitButton.setActivated(false);
				}
				else {
					setSelectedButton(randomTraitButton, monsterTraitButtons);
					ToolSelection.selectedMonsterTraits.clear();
					ToolSelection.selectedMonsterTraits.add(MonsterTrait.RANDOM);
				}
			}
		});
		
		JLabel structuresLabel = new JLabel("Structures");
		structuresLabel.setBounds(910, 5, 200, 20);
		structuresLabel.setFont(new Font("VCR OSD MONO", 1, 12));
		structuresLabel.setForeground(new Color(253,231,111,255));
		monstersPanel.add(structuresLabel);
		
		ToolButton graveyardStructureButton = new ToolButton(75);
		graveyardStructureButton.setIcon(new ImageIcon(monsterIconsPath + "agent_graveyard.png"));
		tooltip = "<html>Graveyard<br><br>A structure that spawns skeletons.</html>";
		graveyardStructureButton.setToolTipText(tooltip);
		monstersPanel.add(graveyardStructureButton);
		monsterTypeButtons.add(graveyardStructureButton);
		graveyardStructureButton.setBounds(910, 25, 80, 80);
		graveyardStructureButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(graveyardStructureButton.isActivated()) {
					ToolSelection.selectedTool = Tool.POINTER;
					graveyardStructureButton.setActivated(false);
				}
				else {
					setSelectedButton(graveyardStructureButton, monsterTypeButtons);
					ToolSelection.selectedTool = Tool.STRUCTURE;
					ToolSelection.selectedStructureType = StructureType.GRAVEYARD;
				}
			}
		});
		
		ToolButton spiderNestStructureButton = new ToolButton(75);
		spiderNestStructureButton.setIcon(new ImageIcon(monsterIconsPath + "agent_spider_nest.png"));
		tooltip = "<html>Spider Nest<br><br>A structure that spawns spiders.</html>";
		spiderNestStructureButton.setToolTipText(tooltip);
		monstersPanel.add(spiderNestStructureButton);
		monsterTypeButtons.add(spiderNestStructureButton);
		spiderNestStructureButton.setBounds(1010, 25, 80, 80);
		spiderNestStructureButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(spiderNestStructureButton.isActivated()) {
					ToolSelection.selectedTool = Tool.POINTER;
					spiderNestStructureButton.setActivated(false);
				}
				else {
					setSelectedButton(spiderNestStructureButton, monsterTypeButtons);
					ToolSelection.selectedTool = Tool.STRUCTURE;
					ToolSelection.selectedStructureType = StructureType.SPIDER_NEST;
				}
			}
		});
	}
	
	private void setUpMaterialsMenu() {
		String materialPath = basePath + "/materialIcons/";
		
		JPanel materialsPanel = new JPanel();
		this.add("Materials", materialsPanel);
		materialsPanel.setBackground(Color.black);
		materialsPanel.setLayout(null);
		
		JLabel equipmentMaterialLabel = new JLabel("Equipment Materials");
		equipmentMaterialLabel.setBounds(10, 5, 200, 20);
		equipmentMaterialLabel.setFont(new Font("VCR OSD MONO", 1, 12));
		equipmentMaterialLabel.setForeground(new Color(253,231,111,255));
		materialsPanel.add(equipmentMaterialLabel);
		
		ToolButton stoneMaterialButton = new ToolButton(75);
		stoneMaterialButton.setIcon(new ImageIcon(materialPath + "mat_stone.png"));
		String tooltip = "<html>Stone<br><br>The worst material for equipments.</html>";
		stoneMaterialButton.setToolTipText(tooltip);
		materialsPanel.add(stoneMaterialButton);
		materialButtons.add(stoneMaterialButton);
		stoneMaterialButton.setBounds(10, 25, 80, 80);
		stoneMaterialButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(stoneMaterialButton.isActivated()) {
					ToolSelection.selectedTool = Tool.POINTER;
					stoneMaterialButton.setActivated(false);
				}
				else {
					setSelectedButton(stoneMaterialButton, materialButtons);
					ToolSelection.selectedTool = Tool.MATERIAL;
					ToolSelection.selectedMaterialType = MaterialType.STONE;
				}
			}
		});
		
		ToolButton ironMaterialButton = new ToolButton(75);
		ironMaterialButton.setIcon(new ImageIcon(materialPath + "mat_iron.png"));
		tooltip = "<html>Iron<br><br>Better material than Stone.</html>";
		ironMaterialButton.setToolTipText(tooltip);
		materialsPanel.add(ironMaterialButton);
		materialButtons.add(ironMaterialButton);
		ironMaterialButton.setBounds(110, 25, 80, 80);
		ironMaterialButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(ironMaterialButton.isActivated()) {
					ToolSelection.selectedTool = Tool.POINTER;
					ironMaterialButton.setActivated(false);
				}
				else {
					setSelectedButton(ironMaterialButton, materialButtons);
					ToolSelection.selectedTool = Tool.MATERIAL;
					ToolSelection.selectedMaterialType = MaterialType.IRON;
				}
			}
		});
		
		ToolButton cobaltMaterialButton = new ToolButton(75);
		cobaltMaterialButton.setIcon(new ImageIcon(materialPath + "mat_cobalt.png"));
		tooltip = "<html>Cobalt<br><br>Better material than Iron.</html>";
		cobaltMaterialButton.setToolTipText(tooltip);
		materialsPanel.add(cobaltMaterialButton);
		materialButtons.add(cobaltMaterialButton);
		cobaltMaterialButton.setBounds(210, 25, 80, 80);
		cobaltMaterialButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(cobaltMaterialButton.isActivated()) {
					ToolSelection.selectedTool = Tool.POINTER;
					cobaltMaterialButton.setActivated(false);
				}
				else {
					setSelectedButton(cobaltMaterialButton, materialButtons);
					ToolSelection.selectedTool = Tool.MATERIAL;
					ToolSelection.selectedMaterialType = MaterialType.COBALT;
				}
			}
		});
		
		ToolButton mythrilMaterialButton = new ToolButton(75);
		mythrilMaterialButton.setIcon(new ImageIcon(materialPath + "mat_mythril.png"));
		tooltip = "<html>Mythril<br><br>Better material than Cobalt.</html>";
		mythrilMaterialButton.setToolTipText(tooltip);
		materialsPanel.add(mythrilMaterialButton);
		materialButtons.add(mythrilMaterialButton);
		mythrilMaterialButton.setBounds(310, 25, 80, 80);
		mythrilMaterialButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(mythrilMaterialButton.isActivated()) {
					ToolSelection.selectedTool = Tool.POINTER;
					mythrilMaterialButton.setActivated(false);
				}
				else {
					setSelectedButton(mythrilMaterialButton, materialButtons);
					ToolSelection.selectedTool = Tool.MATERIAL;
					ToolSelection.selectedMaterialType = MaterialType.MYTHRIL;
				}
			}
		});
		
		ToolButton adamantiteMaterialButton = new ToolButton(75);
		adamantiteMaterialButton.setIcon(new ImageIcon(materialPath + "mat_adamantite.png"));
		tooltip = "<html>Adamantite<br><br>The best material for making equipment.</html>";
		adamantiteMaterialButton.setToolTipText(tooltip);
		materialsPanel.add(adamantiteMaterialButton);
		materialButtons.add(adamantiteMaterialButton);
		adamantiteMaterialButton.setBounds(410, 25, 80, 80);
		adamantiteMaterialButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(adamantiteMaterialButton.isActivated()) {
					ToolSelection.selectedTool = Tool.POINTER;
					adamantiteMaterialButton.setActivated(false);
				}
				else {
					setSelectedButton(adamantiteMaterialButton, materialButtons);
					ToolSelection.selectedTool = Tool.MATERIAL;
					ToolSelection.selectedMaterialType = MaterialType.ADAMANTITE;
				}
			}
		});
		
		JLabel potionMaterialLabel = new JLabel("Potion Materials");
		potionMaterialLabel.setBounds(610, 5, 200, 20);
		potionMaterialLabel.setFont(new Font("VCR OSD MONO", 1, 12));
		potionMaterialLabel.setForeground(new Color(253,231,111,255));
		materialsPanel.add(potionMaterialLabel);
		
		ToolButton daybloomMaterialButton = new ToolButton(75);
		daybloomMaterialButton.setIcon(new ImageIcon(materialPath + "mat_daybloom.png"));
		tooltip = "<html>Daybloom<br><br>Used to make Health potions.</html>";
		daybloomMaterialButton.setToolTipText(tooltip);
		materialsPanel.add(daybloomMaterialButton);
		materialButtons.add(daybloomMaterialButton);
		daybloomMaterialButton.setBounds(610, 25, 80, 80);
		daybloomMaterialButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(daybloomMaterialButton.isActivated()) {
					ToolSelection.selectedTool = Tool.POINTER;
					daybloomMaterialButton.setActivated(false);
				}
				else {
					setSelectedButton(daybloomMaterialButton, materialButtons);
					ToolSelection.selectedTool = Tool.MATERIAL;
					ToolSelection.selectedMaterialType = MaterialType.DAYBLOOM;
				}
			}
		});
		
		ToolButton moonglowMaterialButton = new ToolButton(75);
		moonglowMaterialButton.setIcon(new ImageIcon(materialPath + "mat_moonglow.png"));
		tooltip = "<html>Moonglow<br><br>Used to make Regeneration potions.</html>";
		moonglowMaterialButton.setToolTipText(tooltip);
		materialsPanel.add(moonglowMaterialButton);
		materialButtons.add(moonglowMaterialButton);
		moonglowMaterialButton.setBounds(710, 25, 80, 80);
		moonglowMaterialButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(moonglowMaterialButton.isActivated()) {
					ToolSelection.selectedTool = Tool.POINTER;
					moonglowMaterialButton.setActivated(false);
				}
				else {
					setSelectedButton(moonglowMaterialButton, materialButtons);
					ToolSelection.selectedTool = Tool.MATERIAL;
					ToolSelection.selectedMaterialType = MaterialType.MOONGLOW;
				}
			}
		});
		
		ToolButton shadowstalkMaterialButton = new ToolButton(75);
		shadowstalkMaterialButton.setIcon(new ImageIcon(materialPath + "mat_shadowstalk.png"));
		tooltip = "<html>Shadowstalk<br><br>Used to make Fleeing potions.</html>";
		shadowstalkMaterialButton.setToolTipText(tooltip);
		materialsPanel.add(shadowstalkMaterialButton);
		materialButtons.add(shadowstalkMaterialButton);
		shadowstalkMaterialButton.setBounds(810, 25, 80, 80);
		shadowstalkMaterialButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(shadowstalkMaterialButton.isActivated()) {
					ToolSelection.selectedTool = Tool.POINTER;
					shadowstalkMaterialButton.setActivated(false);
				}
				else {
					setSelectedButton(shadowstalkMaterialButton, materialButtons);
					ToolSelection.selectedTool = Tool.MATERIAL;
					ToolSelection.selectedMaterialType = MaterialType.SHADOWSTALK;
				}
			}
		});
		
		ToolButton bloodroseMaterialButton = new ToolButton(75);
		bloodroseMaterialButton.setIcon(new ImageIcon(materialPath + "mat_bloodrose.png"));
		tooltip = "<html>Blood Rose<br><br>Used to make Super Health potions.</html>";
		bloodroseMaterialButton.setToolTipText(tooltip);
		materialsPanel.add(bloodroseMaterialButton);
		materialButtons.add(bloodroseMaterialButton);
		bloodroseMaterialButton.setBounds(910, 25, 80, 80);
		bloodroseMaterialButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(bloodroseMaterialButton.isActivated()) {
					ToolSelection.selectedTool = Tool.POINTER;
					bloodroseMaterialButton.setActivated(false);
				}
				else {
					setSelectedButton(bloodroseMaterialButton, materialButtons);
					ToolSelection.selectedTool = Tool.MATERIAL;
					ToolSelection.selectedMaterialType = MaterialType.BLOODROSE;
				}
			}
		});
	}
	
	private void setUpChestsMenu() {
		String materialPath = basePath + "/chestIcons/";
		
		JPanel chestsPanel = new JPanel();
		this.add("Chests", chestsPanel);
		chestsPanel.setBackground(Color.black);
		chestsPanel.setLayout(null);
		
		ToolButton woodenChestButton = new ToolButton(75);
		woodenChestButton.setIcon(new ImageIcon(materialPath + "chest_wooden.png"));
		String tooltip = "<html>Wooden Chest<br><br>May contain gold, Stone, Iron and equipment made from those materials.</html>";
		woodenChestButton.setToolTipText(tooltip);
		chestsPanel.add(woodenChestButton);
		chestTypeButtons.add(woodenChestButton);
		woodenChestButton.setBounds(10, 25, 80, 80);
		woodenChestButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(woodenChestButton.isActivated()) {
					ToolSelection.selectedTool = Tool.POINTER;
					woodenChestButton.setActivated(false);
				}
				else {
					setSelectedButton(woodenChestButton, chestTypeButtons);
					ToolSelection.selectedTool = Tool.CHEST;
					ToolSelection.selectedChestType = ChestType.WOODEN_CHEST;
				}
			}
		});
		
		ToolButton ironChestButton = new ToolButton(75);
		ironChestButton.setIcon(new ImageIcon(materialPath + "chest_iron.png"));
		tooltip = "<html>Iron Chest<br><br>May contain gold, Iron, Cobalt, Mythril and equipment made from those materials.</html>";
		ironChestButton.setToolTipText(tooltip);
		chestsPanel.add(ironChestButton);
		chestTypeButtons.add(ironChestButton);
		ironChestButton.setBounds(110, 25, 80, 80);
		ironChestButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(ironChestButton.isActivated()) {
					ToolSelection.selectedTool = Tool.POINTER;
					ironChestButton.setActivated(false);
				}
				else {
					setSelectedButton(ironChestButton, chestTypeButtons);
					ToolSelection.selectedTool = Tool.CHEST;
					ToolSelection.selectedChestType = ChestType.IRON_CHEST;
				}
			}
		});
		
		ToolButton goldenChestButton = new ToolButton(75);
		goldenChestButton.setIcon(new ImageIcon(materialPath + "chest_gold.png"));
		tooltip = "<html>Golden Chest<br><br>May contain gold, Cobalt, Mythril, Adamantite and equipment made from those materials.</html>";
		goldenChestButton.setToolTipText(tooltip);
		chestsPanel.add(goldenChestButton);
		chestTypeButtons.add(goldenChestButton);
		goldenChestButton.setBounds(210, 25, 80, 80);
		goldenChestButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(goldenChestButton.isActivated()) {
					ToolSelection.selectedTool = Tool.POINTER;
					goldenChestButton.setActivated(false);
				}
				else {
					setSelectedButton(goldenChestButton, chestTypeButtons);
					ToolSelection.selectedTool = Tool.CHEST;
					ToolSelection.selectedChestType = ChestType.GOLD_CHEST;
				}
			}
		});
	}
	
	private void setSelectedButton(ToolButton selectedButton, ArrayList<ToolButton> buttonGroup) {
		for (ToolButton tbutton : buttonGroup) {
			tbutton.setActivated(false);
		}
		selectedButton.setActivated(true);
	}
	
}
