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
import agents.monsterAgents.BaseMonsterStructureAgent;
import collectiables.materials.BaseMaterial.MaterialType;
import ui.MenuButton;

public class MonsterStructureInfoDialog extends AgentInfoDialog {

	private BaseMonsterStructureAgent agentRef;
	
	private JLabel ownerLabel;
	
	private JLabel levelLabel;
	private JLabel healthLabel;
	private JLabel attackLabel;
	
	public MonsterStructureInfoDialog(BaseMonsterStructureAgent agent) {
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
		
		String owner = agentRef.getOwner() != null ? agentRef.getOwner().getLocalName() : "None";
		ownerLabel.setText("Owner: " + owner);
		
		levelLabel.setText("Level: " + agentRef.getLevel());
		healthLabel.setText("Health: " + agentRef.getCurrentHealth() + "/" + agentRef.getMaxHealth());
		attackLabel.setText("Attack: " + agentRef.getAttack());
		
		String log = agentRef.getLog();
		logLabel.setText("<html>" + log.replace("\n", "<br>") + "</html>");
		
		if(!scrollbarIsUsed) {
			JScrollBar scrollBar = logScrollPane.getVerticalScrollBar();
			scrollBar.revalidate();
			scrollBar.setValue(scrollBar.getMaximum());
		}
	}

}
