package ui.infoDialogs;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import agents.BaseEntityAgent;
import agents.BaseEntityAgent.StatusEffect;
import agents.heroAgents.BaseHeroAgent;
import agents.heroAgents.CityAgent;
import agents.items.BaseItem;
import agents.items.equipment.Equipment;
import agents.items.potions.BasePotion;
import agents.monsterAgents.BaseMonsterAgent;
import collectiables.materials.BaseMaterial.MaterialType;
import ui.MenuButton;

public class MonsterInfoDialog extends AgentInfoDialog{
	private BaseMonsterAgent agentRef;
	
	private JLabel traitsLabel;
	private JLabel statusesLabel;
	private JPanel statusBox;
	
	private JLabel levelLabel;
	private JLabel healthLabel;
	private JLabel attackLabel;
	private JLabel evasionLabel;
	private JLabel critLabel;
	
	private HashMap<StatusEffect, TraitStatusLabel> statusEffects;
	
	public MonsterInfoDialog(BaseMonsterAgent agent) {
		agentRef = agent;
		init();
	}
	
	@Override
	protected void init() {

		this.setVisible(true);
		this.setLayout(null);
		this.setSize(625, 250);

		nameLabel = new JButton(agentRef.getDisplayName());
		nameLabel.setFont(new Font(nameLabel.getFont().getName(), Font.PLAIN, 20));
		nameLabel.setOpaque(false);
		nameLabel.setContentAreaFilled(false);
		nameLabel.setBorderPainted(false);
		nameLabel.setBounds(0, 0, 250, 50);
		nameLabel.setHorizontalAlignment(SwingConstants.LEFT);
		this.add(nameLabel);
		
		JTextArea renameTextArea = new JTextArea();
		renameTextArea.setBounds(17, 15, 150, 50);
		renameTextArea.setFont(new Font(nameLabel.getFont().getName(), Font.PLAIN, 20));
		renameTextArea.setOpaque(false);
		renameTextArea.setVisible(false);
		this.add(renameTextArea);
		
		MenuButton saveNameBtn = new MenuButton("Save", 25);
		saveNameBtn.setBounds(160, 15, 200, 35);
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
		
		traitsLabel = new JLabel("Traits: ");
		traitsLabel.setBounds(10, 45, 90, 20);
		this.add(traitsLabel);
		
		JPanel traitBox = new JPanel();
		traitBox.setLayout(new BoxLayout(traitBox, BoxLayout.X_AXIS));
		traitBox.setBounds(100, 45, 400, 20);
		this.add(traitBox);
		
		for (int i = 0; i < agentRef.getTraits().size(); i++) {
			if(i != 0) {
				traitBox.add(Box.createRigidArea(new Dimension(10,0)));
			}
			TraitStatusLabel abilityTrait = new TraitStatusLabel(agentRef.getTraits().get(i), 20);
			traitBox.add(abilityTrait);
		}
		
		statusEffects = new HashMap<>();
		
		statusesLabel = new JLabel("Statuses: ");
		statusesLabel.setBounds(10, 70, 90, 20);
		this.add(statusesLabel);
		
		statusBox = new JPanel();
		statusBox.setLayout(new BoxLayout(statusBox, BoxLayout.X_AXIS));
		statusBox.setBounds(100, 70, 400, 20);
		this.add(statusBox);
		
		for (int i = 0; i < agentRef.getStatusEffects().size(); i++) {
			if(i != 0) {
				statusBox.add(Box.createRigidArea(new Dimension(10,0)));
			}
			TraitStatusLabel statusEffect = new TraitStatusLabel(agentRef.getStatusEffects().get(i), 20, agentRef);
			statusBox.add(statusEffect);
		}
		
		levelLabel = new JLabel("Level: " + agentRef.getLevel());
		levelLabel.setBounds(10, 100, 250, 20);
		this.add(levelLabel);
		
		healthLabel = new JLabel("Health: " + agentRef.getCurrentHealth() + "/" + agentRef.getMaxHealth());
		healthLabel.setBounds(10, 120, 250, 20);
		this.add(healthLabel);
		
		attackLabel = new JLabel("Attack: " + agentRef.getAttack());
		attackLabel.setBounds(10, 140, 250, 20);
		this.add(attackLabel);
		
		evasionLabel = new JLabel("Evasion chance: " + agentRef.getEvasionChance() + "%");
		evasionLabel.setBounds(10, 160, 250, 20);
		this.add(evasionLabel);
		
		critLabel = new JLabel("Crit chance: " + agentRef.getCritChance() + "%");
		critLabel.setBounds(10, 180, 250, 20);
		this.add(critLabel);
		
		JLabel logTitleLabel = new JLabel("Log:");
		logTitleLabel.setBounds(300, 10, 200, 20);
		logTitleLabel.setFont(new Font(logTitleLabel.getFont().getName(), Font.PLAIN, 16));
		this.add(logTitleLabel);
		
		String log = agentRef.getLog();
		logLabel = new JLabel("<html>" + log.replace("\n", "<br>") + "</html>");
		logScrollPane = new JScrollPane(logLabel);
		logScrollPane.setBounds(300, 30, 300, 175);
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
		
		ArrayList<Component> componentsToRemove = new ArrayList<>();
		
		for(int i = 0; i < statusBox.getComponents().length; i++) {
			componentsToRemove.add(statusBox.getComponent(i));
		}
		
		for (int i = 0; i < agentRef.getStatusEffects().size(); i++) {
			StatusEffect effect = agentRef.getStatusEffects().get(i);
			
			if(statusEffects.containsKey(effect)) {
				int duration = agentRef.getStatusEffectDuration(effect);
				if(duration != 0) {
					statusEffects.get(effect).updateStatusEffectDuration(agentRef.getStatusEffectDuration(effect));
					componentsToRemove.remove(statusEffects.get(effect));
				}
			}
			else {
				TraitStatusLabel statusEffect = new TraitStatusLabel(effect, 20, agentRef);
				statusBox.add(statusEffect);
				statusEffects.put(effect, statusEffect);
			}
		}
		
		for (Component component : componentsToRemove) {
			statusBox.remove(component);
		}
		
		levelLabel.setText("Level: " + agentRef.getLevel());
		healthLabel.setText("Health: " + agentRef.getCurrentHealth() + "/" + agentRef.getMaxHealth());
		attackLabel.setText("Attack: " + agentRef.getAttack());
		evasionLabel.setText("Evasion chance: " + agentRef.getEvasionChance() + "%");
		critLabel.setText("Crit chance: " + agentRef.getCritChance() + "%");
		
		String log = agentRef.getLog();
		logLabel.setText("<html>" + log.replace("\n", "<br>") + "</html>");
		
		if(!scrollbarIsUsed) {
			JScrollBar scrollBar = logScrollPane.getVerticalScrollBar();
			scrollBar.revalidate();
			scrollBar.setValue(scrollBar.getMaximum());
		}
	}
	
	
}
