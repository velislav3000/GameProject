package ui.infoDialogs;

import javax.swing.JDialog;
import javax.swing.JLabel;

public class HelpDialog extends JDialog{

	public HelpDialog() {
		init();
	}
	
	private void init() {
		this.setResizable(false);
		this.setLayout(null);
		this.setAlwaysOnTop(true);
		this.setSize(500, 400);
		this.setTitle("Controls");
		
		JLabel leftMouseLabel = new JLabel("Left Mouse Button:");
		JLabel leftMouseLabel2 = new JLabel("- Select Tools from the toolbar");
		JLabel leftMouseLabel3 = new JLabel("- Apply tool by clicking on tile");
		JLabel leftMouseLabel4 = new JLabel("- Open Info Window by clicking on a Hero, Monster or Structure");
		JLabel leftMouseLabel5 = new JLabel("when you have no tool selected");
		JLabel leftMouseLabel6 = new JLabel("- If using the tile tool, you can hold down the button and drag to paint easier");
		
		leftMouseLabel.setBounds(10,10,450,20);
		leftMouseLabel2.setBounds(10,30,450,20);
		leftMouseLabel3.setBounds(10,50,450,20);
		leftMouseLabel4.setBounds(10,70,450,20);
		leftMouseLabel5.setBounds(10,90,450,20);
		leftMouseLabel6.setBounds(10,110,450,20);
		
		this.add(leftMouseLabel);
		this.add(leftMouseLabel2);
		this.add(leftMouseLabel3);
		this.add(leftMouseLabel4);
		this.add(leftMouseLabel5);
		this.add(leftMouseLabel6);
		
		JLabel rightMouseLabel = new JLabel("Right Mouse Button:");
		JLabel rightMouseLabel2 = new JLabel("- Hold down button and drag to move around the map");
		JLabel rightMouseLabel3 = new JLabel("- Clicking on Hero, Monster, Structure, Material or Chest deletes them");
		
		rightMouseLabel.setBounds(10,140,450,20);
		rightMouseLabel2.setBounds(10,160,450,20);
		rightMouseLabel3.setBounds(10,180,450,20);
		
		this.add(rightMouseLabel);
		this.add(rightMouseLabel2);
		this.add(rightMouseLabel3);
		
		JLabel mouseWheelLabel = new JLabel("Mouse Wheel - Zoom In/Out");
		mouseWheelLabel.setBounds(10,210,450,20);
		this.add(mouseWheelLabel);
		
		JLabel pauseLabel = new JLabel("P - Pause/Unpause");
		pauseLabel.setBounds(10,240,450,20);
		this.add(pauseLabel);
		
		JLabel saveMapLabel = new JLabel("S - Save Map. This only saves the Tiles, Materials and Chests");
		saveMapLabel.setBounds(10,260,450,20);
		this.add(saveMapLabel);
		
		JLabel loadMapLabel = new JLabel("L - Load Map. All Heroes, Monsters and Structures are deleted");
		loadMapLabel.setBounds(10,280,450,20);
		this.add(loadMapLabel);
		
		JLabel exitLabel = new JLabel("Esc - Exit Game");
		exitLabel.setBounds(10,300,450,20);
		this.add(exitLabel);
		
		JLabel helpLabel = new JLabel("H - Open Controls");
		helpLabel.setBounds(10,320,450,20);
		this.add(helpLabel);
		
		this.setVisible(true);
	}
}
