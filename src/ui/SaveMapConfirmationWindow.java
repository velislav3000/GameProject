package ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class SaveMapConfirmationWindow extends JPanel{
	
	public SaveMapConfirmationWindow(GameFrame frameRef, String mapName) {
		this.setVisible(true);
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setBounds(frameRef.getBounds());
		this.setOpaque(false);
		
		JPanel alignmentPanel = new JPanel();
		alignmentPanel.setMaximumSize(new Dimension(frameRef.getWidth()/3, frameRef.getHeight()));
		alignmentPanel.setOpaque(false);
		alignmentPanel.setLayout(new BoxLayout(alignmentPanel, BoxLayout.X_AXIS));
		alignmentPanel.setAlignmentX(CENTER_ALIGNMENT);
		this.add(alignmentPanel);
		
		JPanel contentPanel = new JPanel();
		contentPanel.setBackground(new Color(0,0,0));
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		contentPanel.setAlignmentY(CENTER_ALIGNMENT);
		contentPanel.setMaximumSize(new Dimension(alignmentPanel.getWidth(), frameRef.getHeight()/4));
		alignmentPanel.add(contentPanel);
		
		JLabel text = new JLabel("<html>There's a map with that name.<br>Are you sure you want to overwrite it?</html>");
		text.setFont(new Font("VCR OSD MONO", 1, 30));
		text.setForeground(new Color(253,231,111));
		text.setBounds(10, 10, 300, 50);
		text.setAlignmentX(CENTER_ALIGNMENT);
		contentPanel.add(text);
		
		contentPanel.add(Box.createRigidArea(new Dimension(0,50)));
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setOpaque(false);
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.setAlignmentX(CENTER_ALIGNMENT);
		contentPanel.add(buttonPanel);
		
		MenuButton yesBtn = new MenuButton("Yes", 75);
		yesBtn.setAlignmentY(Component.CENTER_ALIGNMENT);
		buttonPanel.add(yesBtn);
		yesBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				frameRef.saveMapTemplate(mapName);
				frameRef.backToPauseMenu();
			}
		});
		
		buttonPanel.add(Box.createRigidArea(new Dimension(25,0)));

		MenuButton noBtn = new MenuButton("No", 75);
		noBtn.setAlignmentY(Component.CENTER_ALIGNMENT);
		buttonPanel.add(noBtn);
		noBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				frameRef.removeSaveMapConfirmationWindow();
			}
		});
		
		System.out.println("Confirm opened");
	}
}
