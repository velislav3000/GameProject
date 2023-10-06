package ui.infoDialogs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

import agents.BaseAgent;
import agents.heroAgents.BaseHeroAgent;
import agents.heroAgents.CityAgent;
import agents.monsterAgents.BaseMonsterAgent;
import agents.monsterAgents.BaseMonsterStructureAgent;
import ui.GameFrame;
import ui.SearchButton;

public class AgentInfoPanel extends JPanel{
	
	private AgentInfoDialog content;
	
	public AgentInfoPanel(GameFrame frameRef) {
		this.setVisible(false);
		this.setLocation(new Point(0,0));
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setOpaque(true);
		this.setBackground(new Color(0,0,0,100));
		
		JPanel closeToolPanel = new JPanel();
		closeToolPanel.setAlignmentX(RIGHT_ALIGNMENT);
		closeToolPanel.setOpaque(false);
		closeToolPanel.setMaximumSize(new Dimension(50,50));
		this.add(closeToolPanel);
		
		CloseButton closeBtn = new CloseButton();
		closeBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				close();
			}
		});
		closeToolPanel.add(closeBtn);
	}
	
	public void show(BaseAgent agent) {
		close();
		
		if(agent.isHeroFactionAgent()) {
			if(agent.isEntityAgent()) {
				content = new HeroInfoDialog((BaseHeroAgent) agent);
			}
			else {
				content = new HeroStructureInfoDialog((CityAgent) agent);
			}
		}
		else {
			if(agent.isEntityAgent()) {
				content = new MonsterInfoDialog((BaseMonsterAgent) agent);
			}
			else {
				content = new MonsterStructureInfoDialog((BaseMonsterStructureAgent) agent);
			}
		}
		
		this.add(content);
		this.setSize(new Dimension(content.getSize().width, content.getSize().height + 50));
		this.setVisible(true);
	}
	
	public void close() {
		if(content != null) {
			this.remove(content);
			content.close();
			content = null;
		}
		this.setVisible(false);
	}
	
}
