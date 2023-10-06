package ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

public class PauseMenu extends JPanel{
	
	private SaveGameDialog saveGameDialog;
	
	public PauseMenu(GameFrame frameRef) {
		this.setVisible(true);
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setBounds(frameRef.getBounds());
		this.setOpaque(false);
		this.setBackground(new Color(0, 0, 0, 100));
		this.setAlignmentX(CENTER_ALIGNMENT);
		
		this.add(Box.createRigidArea(new Dimension(0,125)));
		
		MenuButton continueBtn = new MenuButton("Continue", 100);
		continueBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
		this.add(continueBtn);
		continueBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				frameRef.closePauseMenu();
			}
		});
		
		this.add(Box.createRigidArea(new Dimension(0,50)));
		
		MenuButton saveGameBtn = new MenuButton("Save Game", 100);
		saveGameBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
		this.add(saveGameBtn);
		saveGameBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				frameRef.openSaveGameTab();
			}
		});
		
		this.add(Box.createRigidArea(new Dimension(0,50)));
		
		MenuButton saveMapBtn = new MenuButton("Save Map", 100);
		saveMapBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
		this.add(saveMapBtn);
		saveMapBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				frameRef.openSaveMapTab();
			}
		});
		
		this.add(Box.createRigidArea(new Dimension(0,50)));
		
		MenuButton controlsBtn = new MenuButton("Controls", 100);
		controlsBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
		this.add(controlsBtn);
		controlsBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				frameRef.openControlsMenu();
			}
		});
		
		this.add(Box.createRigidArea(new Dimension(0,50)));
		
		MenuButton mainMenuBtn = new MenuButton("Main Menu", 100);
		mainMenuBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
		this.add(mainMenuBtn);
		mainMenuBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				frameRef.toMainMenu();
			}
		});
		

		
		this.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				
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
	}
	
	@Override
	public void paint(Graphics g) {
		g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
		super.paint(g);
	}
}
