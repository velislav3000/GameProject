package ui;

import java.awt.Graphics;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class MenuBackground extends JPanel{

	private boolean scrollRight = true;
	private int start;
	private int stop;
	private int currentLocation;
	
	public MenuBackground(GameFrame frameRef) {
		this.setVisible(true);
		this.setLayout(null);
		
		int height = frameRef.getHeight();
		int width = Math.round(height*4.9f);
		
		this.setBounds(0, 0, width, height);
		
		JLabel imgLabel = new JLabel();
		imgLabel.setBounds(0, 0, width, height);
		ImageIcon icon = new ImageIcon("./res/ui/main_menu_bgr.png");
		Image img = icon.getImage();
		Image newImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
		icon.setImage(newImg);
		imgLabel.setIcon(icon);
		this.add(imgLabel);

		int screenWidth = frameRef.getWidth();
		
		start = 0;
		stop = (width - screenWidth) * -1;
		currentLocation = 0;
	}
	
	@Override
	public void paint(Graphics g) {
		if(scrollRight) {
			currentLocation -= 1;
		}
		else {
			currentLocation += 1;
		}
		
		if(currentLocation >= start || currentLocation <= stop) {
			scrollRight = !scrollRight;
		}
		
		this.setLocation(currentLocation, 0);
		
		super.paint(g);
	}
}
