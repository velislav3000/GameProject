package ui.infoDialogs;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import agents.BaseEntityAgent;
import agents.heroAgents.BaseHeroAgent;
import agents.heroAgents.CityAgent;
import agents.items.BaseItem;
import agents.items.equipment.Equipment;
import agents.items.potions.BasePotion;
import collectiables.materials.BaseMaterial.MaterialType;
import ui.MenuButton;

public class HeroStructureInfoDialog extends AgentInfoDialog {

	private CityAgent agentRef;
	
	private JLabel ownerLabel;
	
	private JLabel levelLabel;
	private JLabel healthLabel;
	private JLabel attackLabel;
	private JLabel goldLabel;
	
	private JLabel stoneLabel;
	private JLabel ironLabel;
	private JLabel cobaltLabel;
	private JLabel mythrilLabel;
	private JLabel adamantiteLabel;
	private JLabel stringLabel;
	private JLabel daybloomLabel;
	private JLabel moonglowLabel;
	private JLabel shadowstalkLabel;
	private JLabel bloodroseLabel;
	
	private JLabel equipmentLabel;
	private JLabel potionsLabel;
	
	public HeroStructureInfoDialog(CityAgent agent) {
		agentRef = agent;
		init();
	}
	
	@Override
	protected void init() {
		this.setVisible(true);
		this.setLayout(null);
		this.setSize(825, 510);

		nameLabel = new JButton(agentRef.getDisplayName());
		nameLabel.setFont(new Font(nameLabel.getFont().getName(), Font.PLAIN, 20));
		nameLabel.setOpaque(false);
		nameLabel.setContentAreaFilled(false);
		nameLabel.setBorderPainted(false);
		nameLabel.setBounds(0, 0, 250, 50);
		nameLabel.setHorizontalAlignment(SwingConstants.LEFT);
		this.add(nameLabel);
		
		JTextArea renameTextArea = new JTextArea();
		renameTextArea.setBounds(17, 15, 250, 50);
		renameTextArea.setFont(new Font(nameLabel.getFont().getName(), Font.PLAIN, 20));
		renameTextArea.setOpaque(false);
		renameTextArea.setVisible(false);
		this.add(renameTextArea);
		
		MenuButton saveNameBtn = new MenuButton("Save", 25);
		saveNameBtn.setBounds(280, 15, 200, 35);
		saveNameBtn.setOpaque(false);
		saveNameBtn.setVisible(false);
		this.add(saveNameBtn);
		
		nameLabel.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				nameLabel.setVisible(false);
				renameTextArea.setVisible(true);
				renameTextArea.setText(agentRef.getDisplayName());
				saveNameBtn.setVisible(true);
			}
		});
		
		saveNameBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				agentRef.setDisplayName(renameTextArea.getText());
				renameTextArea.setVisible(false);
				saveNameBtn.setVisible(false);
				nameLabel.setText(renameTextArea.getText());
				nameLabel.setVisible(true);
			}
		});
		
		String owner = agentRef.getOwner() != null ? agentRef.getOwner().getLocalName() : "None";
		ownerLabel = new JLabel("Owner: " + owner);
		ownerLabel.setBounds(10, 40, 250, 20);
		this.add(ownerLabel);
		
		levelLabel = new JLabel("Level: " + agentRef.getLevel());
		levelLabel.setBounds(10, 100, 250, 20);
		this.add(levelLabel);
		
		healthLabel = new JLabel("Health: " + agentRef.getCurrentHealth() + "/" + agentRef.getMaxHealth());
		healthLabel.setBounds(10, 120, 250, 20);
		this.add(healthLabel);
		
		attackLabel = new JLabel("Attack: " + agentRef.getAttack());
		attackLabel.setBounds(10, 140, 250, 20);
		this.add(attackLabel);
		
		goldLabel = new JLabel("Gold: " + agentRef.getGold());
		goldLabel.setBounds(10, 160, 200, 20);
		this.add(goldLabel);
		
		JLabel materialsLabel = new JLabel("Materials:");
		materialsLabel.setBounds(10, 190, 200, 20);
		materialsLabel.setFont(new Font(materialsLabel.getFont().getName(), Font.PLAIN, 16));
		this.add(materialsLabel);
		
		stoneLabel = new JLabel("Stone: " + agentRef.getMaterialTotal(MaterialType.STONE));
		stoneLabel.setBounds(10, 210, 200, 20);
		this.add(stoneLabel);
		
		ironLabel = new JLabel("Iron: " + agentRef.getMaterialTotal(MaterialType.IRON));
		ironLabel.setBounds(10, 230, 200, 20);
		this.add(ironLabel);
		
		cobaltLabel = new JLabel("Cobalt: " + agentRef.getMaterialTotal(MaterialType.COBALT));
		cobaltLabel.setBounds(10, 250, 200, 20);
		this.add(cobaltLabel);
		
		mythrilLabel = new JLabel("Mythril: " + agentRef.getMaterialTotal(MaterialType.MYTHRIL));
		mythrilLabel.setBounds(10, 270, 200, 20);
		this.add(mythrilLabel);
		
		adamantiteLabel = new JLabel("Adamantite: " + agentRef.getMaterialTotal(MaterialType.ADAMANTITE));
		adamantiteLabel.setBounds(10, 290, 200, 20);
		this.add(adamantiteLabel);
		
		stringLabel = new JLabel("String: " + agentRef.getMaterialTotal(MaterialType.STRING));
		stringLabel.setBounds(260, 210, 200, 20);
		this.add(stringLabel);
		
		daybloomLabel = new JLabel("Daybloom: " + agentRef.getMaterialTotal(MaterialType.DAYBLOOM));
		daybloomLabel.setBounds(260, 230, 200, 20);
		this.add(daybloomLabel);
		
		moonglowLabel = new JLabel("Moonglow: " + agentRef.getMaterialTotal(MaterialType.MOONGLOW));
		moonglowLabel.setBounds(260, 250, 200, 20);
		this.add(moonglowLabel);
		
		shadowstalkLabel = new JLabel("Shadow Stalk: " + agentRef.getMaterialTotal(MaterialType.SHADOWSTALK));
		shadowstalkLabel.setBounds(260, 270, 200, 20);
		this.add(shadowstalkLabel);
		
		bloodroseLabel = new JLabel("Blood Rose: " + agentRef.getMaterialTotal(MaterialType.BLOODROSE));
		bloodroseLabel.setBounds(260, 290, 200, 20);
		this.add(bloodroseLabel);
		
		JLabel equipmentTitleLabel = new JLabel("Equipment:");
		equipmentTitleLabel.setBounds(10, 320, 200, 20);
		equipmentTitleLabel.setFont(new Font(equipmentTitleLabel.getFont().getName(), Font.PLAIN, 16));
		this.add(equipmentTitleLabel);
		
		equipmentLabel = new JLabel("<html>");
		JScrollPane equipmentScrollPane = new JScrollPane(equipmentLabel);
		equipmentScrollPane.setBounds(10, 340, 230, 125);
		
		JLabel potionsTitleLabel = new JLabel("Potions:");
		potionsTitleLabel.setBounds(250, 320, 200, 20);
		potionsTitleLabel.setFont(new Font(potionsTitleLabel.getFont().getName(), Font.PLAIN, 16));
		this.add(potionsTitleLabel);
		
		potionsLabel = new JLabel("<html>");
		JScrollPane potionsScrollPane = new JScrollPane(potionsLabel);
		potionsScrollPane.setBounds(250, 340, 230, 125);
		
		for(BaseItem item : agentRef.getAllItems()) {
			if(Equipment.class.isAssignableFrom(item.getClass())) {
				Equipment equipment = (Equipment) item;
				equipmentLabel.setText(equipmentLabel.getText() + equipment.getQuality() + " " + equipment.getMaterial() + " " + equipment.getType() + "<br>");						
			}
			else {
				BasePotion potion = (BasePotion) item; 
				
				potionsLabel.setText(potionsLabel.getText() + potion.getQuality() + " " + potion.getType() + " POTION" + "<br>");
			}
		}
		
		equipmentLabel.setText(equipmentLabel.getText() + "</html>");
		potionsLabel.setText(potionsLabel.getText() + "</html>");

		this.add(equipmentScrollPane);
		this.add(potionsScrollPane);
		
		JLabel logTitleLabel = new JLabel("Log:");
		logTitleLabel.setBounds(500, 10, 200, 20);
		logTitleLabel.setFont(new Font(logTitleLabel.getFont().getName(), Font.PLAIN, 16));
		this.add(logTitleLabel);
		
		String log = agentRef.getLog();
		logLabel = new JLabel("<html>" + log.replace("\n", "<br>") + "</html>");
		logScrollPane = new JScrollPane(logLabel);
		logScrollPane.setBounds(500, 30, 300, 435);
		this.add(logScrollPane);
		
		JScrollBar scrollbar = logScrollPane.getVerticalScrollBar();
		scrollbar.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				scrollbarIsUsed = false;
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				scrollbarIsUsed = true;
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
				// TODO Auto-generated method stub
				
			}
		});
		
		super.init();
	}
	
	@Override
	protected void updateStats() {
		if(!checkIfShouldUpdate(agentRef)) {
			shouldUpdate = false;
			return;
		}
		
		nameLabel.setText(agentRef.getDisplayName());
		
		String owner = agentRef.getOwner() != null ? agentRef.getOwner().getLocalName() : "None";
		ownerLabel.setText("Owner: " + owner);
		
		levelLabel.setText("Level: " + agentRef.getLevel());
		healthLabel.setText("Health: " + agentRef.getCurrentHealth() + "/" + agentRef.getMaxHealth());
		attackLabel.setText("Attack: " + agentRef.getAttack());
		
		goldLabel.setText("Gold: " + agentRef.getGold());
		
		stoneLabel.setText("Stone: " + agentRef.getMaterialTotal(MaterialType.STONE));
		ironLabel.setText("Iron: " + agentRef.getMaterialTotal(MaterialType.IRON));
		cobaltLabel.setText("Cobalt: " + agentRef.getMaterialTotal(MaterialType.COBALT));
		mythrilLabel.setText("Mythril: " + agentRef.getMaterialTotal(MaterialType.MYTHRIL));
		adamantiteLabel.setText("Adamantite: " + agentRef.getMaterialTotal(MaterialType.ADAMANTITE));
		stringLabel.setText("String: " + agentRef.getMaterialTotal(MaterialType.STRING));
		daybloomLabel.setText("Daybloom: " + agentRef.getMaterialTotal(MaterialType.DAYBLOOM));
		moonglowLabel.setText("Moonglow: " + agentRef.getMaterialTotal(MaterialType.MOONGLOW));
		shadowstalkLabel.setText("Shadow Stalk: " + agentRef.getMaterialTotal(MaterialType.SHADOWSTALK));
		bloodroseLabel.setText("Blood Rose: " + agentRef.getMaterialTotal(MaterialType.BLOODROSE));
		
		equipmentLabel.setText("<html>");
		potionsLabel.setText("<html>");
		
		for(BaseItem item : agentRef.getAllItems()) {
			if(Equipment.class.isAssignableFrom(item.getClass())) {
				Equipment equipment = (Equipment) item;
				
				equipmentLabel.setText(equipmentLabel.getText() + equipment.getQuality() + " " + equipment.getMaterial() + " " + equipment.getType() + "<br>");						
			}
			else {
				BasePotion potion = (BasePotion) item; 
				
				potionsLabel.setText(potionsLabel.getText() + potion.getQuality() + " " + potion.getType() + " POTION" + "<br>");
			}
		}
		
		equipmentLabel.setText(equipmentLabel.getText() + "</html>");
		potionsLabel.setText(potionsLabel.getText() + "</html>");
		
		String log = agentRef.getLog();
		logLabel.setText("<html>" + log.replace("\n", "<br>") + "</html>");
		
		if(!scrollbarIsUsed) {
			JScrollBar scrollBar = logScrollPane.getVerticalScrollBar();
			scrollBar.revalidate();
			scrollBar.setValue(scrollBar.getMaximum());
		}
	}

}
