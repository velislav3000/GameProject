package ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

public class MainMenu extends JPanel{
	
	public MainMenu(GameFrame frameRef) {
		this.setVisible(true);
		this.setBounds(frameRef.getBounds());
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setAlignmentX(CENTER_ALIGNMENT);
		this.setOpaque(false);
		//this.setBackground(new Color(0,0,0,0));
		
		JLabel titleLabel = new JLabel();
		ImageIcon icon = new ImageIcon("./res/ui/game_title.png");
		Image img = icon.getImage();
		Image newImg = img.getScaledInstance(700, 350, Image.SCALE_SMOOTH);
		icon.setImage(newImg);
		titleLabel.setIcon(icon);
		titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		this.add(titleLabel);
		
		this.add(Box.createRigidArea(new Dimension(0,50)));
		
		MenuButton newGameBtn = new MenuButton("New Game", 150);
		newGameBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
		this.add(newGameBtn);
		newGameBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				frameRef.openNewGameTab();
			}
		});
		
		this.add(Box.createRigidArea(new Dimension(0,50)));
		
		MenuButton loadGameBtn = new MenuButton("Load Game", 150);
		loadGameBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
		this.add(loadGameBtn);
		loadGameBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				frameRef.openLoadGameTab();
			}
		});
		
		this.add(Box.createRigidArea(new Dimension(0,50)));
		
		MenuButton exitBtn = new MenuButton("Exit", 150);
		exitBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
		this.add(exitBtn);
		exitBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
	}
}