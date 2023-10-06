package ui.toolsPanel;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.ImageIcon;

public class ToolButtonIcon extends ImageIcon{
	private ImageIcon backgroundIcon;
	private ImageIcon toolIcon;
	
	@Override
	public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
		if(backgroundIcon != null) {
			backgroundIcon.paintIcon(c, g, x, y);
		}
		if(toolIcon != null) {
			int offset = Math.round(backgroundIcon.getIconWidth() *0.07f);
			toolIcon.paintIcon(c, g, x + offset, y+offset);
		}
	}

	public void setBackgroundIcon(ImageIcon backgroundIcon) {
		this.backgroundIcon = backgroundIcon;
	}

	public void setToolIcon(ImageIcon toolIcon) {
		this.toolIcon = toolIcon;
	}
}
