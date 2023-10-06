package ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicScrollBarUI;

public class SaveGameMenu extends JPanel{
	
	private boolean isConfirmationWindowOpen;
	private ArrayList<String> saveNames = new ArrayList<>();
	private JTextField saveTextField;
	
	public SaveGameMenu(GameFrame frameRef) {
		this.setVisible(true);
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setBounds(frameRef.getBounds());
		this.setOpaque(false);
		this.setBackground(new Color(0, 0, 0, 100));
		this.setAlignmentX(CENTER_ALIGNMENT);
		
		this.add(Box.createRigidArea(new Dimension(0,50)));

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
		
		String savesPath = "";
		try {
			savesPath = (new File(".").getCanonicalPath()) + "/saves";
		} catch (IOException e) {
			e.printStackTrace();
		}
		Path path = Paths.get(savesPath);
		
		if(Files.isDirectory(path)) {
			File folder = new File(savesPath);
			File[] listOfFiles = folder.listFiles();

			for (int i = 0; i < listOfFiles.length; i++) {
				if (!listOfFiles[i].isFile()) {
					contentPanel.add(Box.createRigidArea(new Dimension(0,25)));
				
					saveNames.add(listOfFiles[i].getName());
			    	MenuButton saveBtn = new MenuButton(listOfFiles[i].getName(), 75);
			    	saveBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
			    	saveBtn.addActionListener(new ActionListener() {
						
						@Override
						public void actionPerformed(ActionEvent e) {
							saveTextField.setText(saveBtn.getText());
						}
					});
			    	contentPanel.add(saveBtn);
			  	}
			}
		}
		
		contentPanel.add(Box.createRigidArea(new Dimension(0,25)));

		this.add(Box.createRigidArea(new Dimension(0,25)));
		
		JPanel saveTools = new JPanel();
		saveTools.setLayout(new BoxLayout(saveTools, BoxLayout.X_AXIS));
		saveTools.setOpaque(false);
		saveTools.setMaximumSize(new Dimension(frameRef.getWidth()/3, 30));
		saveTools.setAlignmentX(Component.CENTER_ALIGNMENT);
		this.add(saveTools);
		
		JLabel saveLabel = new JLabel("Save Name");
		saveLabel.setBounds(10, 10, 100, 50);
		saveLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
		saveLabel.setFont(new Font("VCR OSD MONO", 1, 20));
		saveLabel.setForeground(new Color(253,231,111,255));
		saveTools.add(saveLabel);
		
		saveTools.add(Box.createRigidArea(new Dimension(20,0)));
		
		saveTextField = new JTextField();
		saveTextField.setAlignmentY(Component.CENTER_ALIGNMENT);
		saveTextField.setMaximumSize(new Dimension(200,25));
		saveTextField.setFont(new Font("VCR OSD MONO", 1, 20));
		saveTextField.setBackground(Color.black);
		saveTextField.setForeground(new Color(253,231,111,255));
		saveTextField.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				limitCharAmount(saveTextField);
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				limitCharAmount(saveTextField);
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				limitCharAmount(saveTextField);
			}
		});
		saveTools.add(saveTextField);
		
		saveTools.add(Box.createRigidArea(new Dimension(20,0)));
		
		MenuButton saveBtn = new MenuButton("Save", 50);
		saveBtn.setAlignmentY(Component.CENTER_ALIGNMENT);
		saveTools.add(saveBtn);
		saveBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				String saveName = saveTextField.getText();
				if(isConfirmationWindowOpen) {
					return;
				}
				
				if(saveNames.contains(saveName)) {
					isConfirmationWindowOpen = true;
					frameRef.addSaveGameConfirmationWindow(saveName);
				}
				else {
					frameRef.saveGame(saveTextField.getText().replace(".", ""));
					frameRef.backToPauseMenu();
				}
			}
		});
		
		saveTools.add(Box.createRigidArea(new Dimension(20,0)));
		
		MenuButton backBtn = new MenuButton("Back", 50);
		backBtn.setSize(500, 300);
		backBtn.setAlignmentY(Component.CENTER_ALIGNMENT);
		saveTools.add(backBtn);
		backBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(isConfirmationWindowOpen) {
					return;
				}
				frameRef.backToPauseMenu();
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
	
	private void limitCharAmount(JTextField textField) {
		Runnable limitChars = new Runnable() {
	        @Override
	        public void run() {
	    		if(textField.getText().length() > 9) {
	    			String text = textField.getText();
	    			text = text.substring(0, 9);
	    			textField.setText(text);
	    		}
	        }
	    };       
	    SwingUtilities.invokeLater(limitChars);
	}
	
	public void enableButtons() {
		isConfirmationWindowOpen = false;
	}
}