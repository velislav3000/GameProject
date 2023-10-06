package ui;

import java.awt.Color;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;

public class PauseButton extends JButton{

	private boolean showPlay;
	private boolean hovered;
	private ImageIcon pauseIcon;
	private ImageIcon pauseIconGold;
	private ImageIcon playIcon;
	private ImageIcon playIconGold;
	
	public PauseButton() {
		super();
		setOpaque(false);
		setBorder(null);
		setBorderPainted(false);
        setContentAreaFilled(false); 
        setFocusPainted(false); 
        
		showPlay = false;
		hovered = false;
		
		pauseIcon = new ImageIcon("./res/ui/btn_pause.png");
		pauseIconGold = new ImageIcon("./res/ui/btn_pause_gold.png");
		playIcon = new ImageIcon("./res/ui/btn_play.png");
		playIconGold = new ImageIcon("./res/ui/btn_play_gold.png");
		
		Image img = pauseIcon.getImage();
		Image newImg = img.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
		pauseIcon.setImage(newImg);
		
		img = pauseIconGold.getImage();
		newImg = img.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
		pauseIconGold.setImage(newImg);
		
		img = playIcon.getImage();
		newImg = img.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
		playIcon.setImage(newImg);
		
		img = playIconGold.getImage();
		newImg = img.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
		playIconGold.setImage(newImg);
		
		setIcon(pauseIcon);
		setPressedIcon(pauseIcon);
		setBackground(new Color(0,0,0,0));
		
		addMouseListener(new MouseListener() {
			
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
				hovered = false;
				if(showPlay) {
					setIcon(playIcon);
					setPressedIcon(playIcon);
				}
				else {
					setIcon(pauseIcon);
					setPressedIcon(pauseIcon);
				}
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				hovered = true;
				if(showPlay) {
					setIcon(playIconGold);
					setPressedIcon(playIconGold);
				}
				else {
					setIcon(pauseIconGold);
					setPressedIcon(pauseIconGold);
				}
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	public void toggleIcon() {
		showPlay = !showPlay;
		if(showPlay) {
			if(hovered) {
				setIcon(playIconGold);
				setPressedIcon(playIconGold);
			}
			else {
				setIcon(playIcon);
				setPressedIcon(playIcon);
			}
		}
		else {
			if(hovered) {
				setIcon(pauseIconGold);
				setPressedIcon(pauseIconGold);
			}
			else {
				setIcon(pauseIcon);
				setPressedIcon(pauseIcon);
			}
		}
	}
}
