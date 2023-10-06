package ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.plaf.basic.BasicScrollBarUI;

public class NewGamePanel extends JPanel{

	private String selectedMapTemplate;
	
	public NewGamePanel(GameFrame frameRef) {
		this.setVisible(true);
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setBounds(frameRef.getBounds());
		this.setAlignmentX(CENTER_ALIGNMENT);
		this.setBackground(Color.BLACK);
		this.setOpaque(false);
		
		this.add(Box.createRigidArea(new Dimension(0,100)));
		
		JLabel selectedMap = new JLabel("Map: Default");
		selectedMap.setSize(500, 200);
		selectedMap.setAlignmentX(Component.CENTER_ALIGNMENT);
		selectedMap.setFont(new Font("VCR OSD MONO", 1, 100));
		selectedMap.setForeground(new Color(253,231,111,255));
		this.add(selectedMap);

		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		contentPanel.setBackground(new Color(0,0,0,100));
		
		JScrollPane scrollArea = new JScrollPane(contentPanel);
		scrollArea.setSize(frameRef.getWidth()/3, frameRef.getHeight()/2);
		scrollArea.setAlignmentX(Component.CENTER_ALIGNMENT);
		scrollArea.setMaximumSize(new Dimension(frameRef.getWidth()/3, 10000));
		scrollArea.setBackground(new Color(0,0,0,100));
		scrollArea.getVerticalScrollBar().setUnitIncrement(16);
		scrollArea.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
			@Override
			protected void configureScrollBarColors() {
				super.configureScrollBarColors();
				thumbColor = new Color(253,231,111);
				thumbHighlightColor = new Color(216,138,2);
				trackColor = Color.black;
				trackHighlightColor = Color.black;
				thumbDarkShadowColor = Color.black;
				thumbLightShadowColor = Color.black;
			}
			
			@Override
			protected JButton createIncreaseButton(int orientation) {
				JButton btn = super.createIncreaseButton(orientation);
				btn.setForeground(new Color(253,231,111));
				btn.setBackground(new Color(216,138,2));
				return btn;
			}
			
			@Override
			protected JButton createDecreaseButton(int orientation) {
				JButton btn = super.createDecreaseButton(orientation);
				btn.setForeground(new Color(253,231,111));
				btn.setBackground(new Color(216,138,2));
				return btn;
			}
		});
		this.add(scrollArea);
		
		contentPanel.setSize(scrollArea.getSize());

		setMaps(contentPanel, selectedMap);

		this.add(Box.createRigidArea(new Dimension(0,15)));
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.setOpaque(false);
		this.add(buttonPanel);
		
		MenuButton startBtn = new MenuButton("Start", 50);
		startBtn.setAlignmentY(Component.CENTER_ALIGNMENT);
		buttonPanel.add(startBtn);
		startBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				frameRef.startNewGame(selectedMapTemplate);
			}
		});
		
		buttonPanel.add(Box.createRigidArea(new Dimension(10,0)));
		
		MenuButton deleteBtn = new MenuButton("Delete", 50);
		deleteBtn.setAlignmentY(Component.CENTER_ALIGNMENT);
		buttonPanel.add(deleteBtn);
		deleteBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!selectedMapTemplate.contentEquals("Default")) {
					File file = new File(".\\mapTemplates\\" + selectedMapTemplate);
					if(file.exists()) {
						file.delete();
						setMaps(contentPanel, selectedMap);
						selectedMapTemplate = "Default";
						selectedMap.setText("Map: Default");
					}
				}
			}
		});
		
		buttonPanel.add(Box.createRigidArea(new Dimension(10,0)));
		
		MenuButton backBtn = new MenuButton("Back", 50);
		backBtn.setSize(500, 300);
		backBtn.setAlignmentY(Component.CENTER_ALIGNMENT);
		buttonPanel.add(backBtn);
		backBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				frameRef.backToMainMenu();
			}
		});
		
		this.add(Box.createRigidArea(new Dimension(0,15)));
	}
	
	private void setMaps(JPanel contentPanel, JLabel selectedMap) {
		contentPanel.removeAll();
		
		contentPanel.add(Box.createRigidArea(new Dimension(0,25)));
		
		selectedMapTemplate = "Default";
		
		MenuButton defaultBtn = new MenuButton("Default", 75);
		defaultBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
		contentPanel.add(defaultBtn);
		defaultBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				selectedMapTemplate = "Default";
				selectedMap.setText("Map: Default");
			}
		});
		
		String mapTeplatesPath = "";
		try {
			mapTeplatesPath = (new File(".").getCanonicalPath()) + "/mapTemplates";
		} catch (IOException e) {
			e.printStackTrace();
		}
		Path path = Paths.get(mapTeplatesPath);
		
		if(Files.isDirectory(path)) {
			File folder = new File(mapTeplatesPath);
			File[] listOfFiles = folder.listFiles();

			for (int i = 0; i < listOfFiles.length; i++) {
			  if (listOfFiles[i].isFile()) {
			    contentPanel.add(Box.createRigidArea(new Dimension(0,25)));
				
			    MenuButton mapTemplateBtn = new MenuButton(listOfFiles[i].getName(), 75);
				mapTemplateBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
				contentPanel.add(mapTemplateBtn);
				mapTemplateBtn.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						selectedMapTemplate = mapTemplateBtn.getText();
						selectedMap.setText("Map: " + mapTemplateBtn.getText());
					}
				});
			  }
			}
		}
		
		contentPanel.add(Box.createRigidArea(new Dimension(0,25)));
	}
}
