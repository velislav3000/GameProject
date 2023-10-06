package ui;

import java.awt.Color;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;

public class InfoButton extends JButton{

	private boolean hovered;
	private ImageIcon searchIcon;
	private ImageIcon searchIconGold;
	
	public InfoButton() {
		super();
		setOpaque(false);
		setBorder(null);
		setBorderPainted(false);
        setContentAreaFilled(false); 
        setFocusPainted(false);
        
		hovered = false;
		
		searchIcon = new ImageIcon("./res/ui/btn_info.png");
		searchIconGold = new ImageIcon("./res/ui/btn_info_gold.png");
		
		Image img = searchIcon.getImage();
		Image newImg = img.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
		searchIcon.setImage(newImg);
		
		img = searchIconGold.getImage();
		newImg = img.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
		searchIconGold.setImage(newImg);
		
		setIcon(searchIcon);
		setPressedIcon(searchIcon);
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
				setIcon(searchIcon);
				setPressedIcon(searchIcon);
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				hovered = true;
				setIcon(searchIconGold);
				setPressedIcon(searchIconGold);
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
	}
}
