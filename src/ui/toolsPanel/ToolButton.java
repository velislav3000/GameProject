package ui.toolsPanel;

import java.awt.Color;
import java.awt.Image;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;

public class ToolButton extends JButton{
	
	private boolean activated = false;
	private int size;
	
	private ToolButtonIcon icon;
	private ImageIcon unselectedIcon;
	private ImageIcon selectedIcon;
	
	public ToolButton(int size) {
		super();
		this.setOpaque(false);
		setBorder(null);
		setBorderPainted(false);
        setContentAreaFilled(false); 
        setFocusPainted(false);
		this.size = size;

		icon = new ToolButtonIcon();
		super.setIcon(icon);
		this.setHorizontalAlignment(this.LEFT);
		this.setVerticalAlignment(this.TOP);
		
		unselectedIcon = new ImageIcon("./res/ui/btn_tool.png");
		Image img = unselectedIcon.getImage();
		Image newImg = img.getScaledInstance(size, size, Image.SCALE_SMOOTH);
		unselectedIcon.setImage(newImg);
		this.setBackgroundIcon(unselectedIcon);
		
		selectedIcon = new ImageIcon("./res/ui/btn_tool_selected.png");
		img = selectedIcon.getImage();
		newImg = img.getScaledInstance(size, size, Image.SCALE_SMOOTH);
		selectedIcon.setImage(newImg);
	}

	public boolean isActivated() {
		return activated;
	}

	public void setActivated(boolean activated) {
		this.activated = activated;
		if(activated) {
			this.setBackgroundIcon(selectedIcon);
		}
		else {
			this.setBackgroundIcon(unselectedIcon);
		}
	}

	public void switchActivated() {
		setActivated(!activated);
	}
	
	@Override
	public void setIcon(Icon icon) {
		ImageIcon displayIcon = (ImageIcon) icon;
		Image img = displayIcon.getImage();
		int imgSize = Math.round(size*0.85f);
		Image newImg = img.getScaledInstance(imgSize, imgSize, Image.SCALE_SMOOTH);
		displayIcon.setImage(newImg);
		this.icon.setToolIcon(displayIcon);
	}
	
	private void setBackgroundIcon(ImageIcon icon) {
		this.icon.setBackgroundIcon(icon);
	}
}
