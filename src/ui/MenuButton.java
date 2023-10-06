package ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Area;

import javax.swing.ImageIcon;
import javax.swing.JButton;

public class MenuButton extends JButton{
	
	public MenuButton(String text, int height) {
		super();
		setOpaque(false);
		setBorder(null);
		setBorderPainted(false);
        setContentAreaFilled(false); 
        setFocusPainted(false);
		
		ImageIcon icon = new ImageIcon("./res/ui/btn.png");
		Image img = icon.getImage();
		int width = Math.round(height/0.375f);
		Image newImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
		icon.setImage(newImg);
		setIcon(icon);
		setPressedIcon(icon);
		setHorizontalTextPosition(JButton.CENTER);
		setVerticalTextPosition(JButton.CENTER);
		setBackground(new Color(0,0,0,0));
		
		setText(text);
		int fontSize = height/3;
		setFont(new Font("VCR OSD MONO", 1, fontSize));
		setForeground(new Color(255,255,255,255));
		
		addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				setForeground(new Color(255,255,255,255));
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				setForeground(new Color(255,255,255,255));
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				setForeground(new Color(253,231,111,255));
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
			}
		});
	}
}
