package ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import agents.BaseAgent;

public class SearchEntry extends JPanel{
	
	public SearchEntry(GameFrame frameRef, BaseAgent agent) {
		this.setOpaque(false);
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.setBackground(Color.blue);
		
		JLabel imageLabel = new JLabel();
		ImageIcon icon = new ImageIcon(agent.getImage());
		Image img = icon.getImage();
		Image newImg = img.getScaledInstance(66, 50, Image.SCALE_SMOOTH);
		icon.setImage(newImg);
		imageLabel.setIcon(icon);
		this.add(imageLabel);
		
		JLabel nameLabel = new JLabel(agent.getDisplayName());
		nameLabel.setFont(new Font("VCR OSD MONO", 1, 22));
		nameLabel.setForeground(new Color(253,231,111,255));
		nameLabel.setMaximumSize(new Dimension(250,50));
		nameLabel.setMinimumSize(new Dimension(250,50));
		this.add(nameLabel);

		this.add(Box.createRigidArea(new Dimension(10,0)));
		
		SearchButton searchBtn = new SearchButton();
		searchBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				frameRef.findAgent(agent.getLocalName());
			}
		});
		this.add(searchBtn);
		
		this.add(Box.createRigidArea(new Dimension(10,0)));
		
		InfoButton infoBtn = new InfoButton();
		infoBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				frameRef.showAgentInfo(agent);
			}
		});
		this.add(infoBtn);
	}
	
}
