package ui.toolsPanel;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.plaf.metal.MetalTabbedPaneUI;

public class ToolPanelUI extends MetalTabbedPaneUI {

    @Override
    protected void installDefaults() {
        super.installDefaults();
        if (contentBorderInsets != null) {
            contentBorderInsets = new Insets(contentBorderInsets.top, 0, 0, 0);
        }
        selectColor = new Color(216,138,2);
    }
    
    @Override
    protected void paintContentBorderRightEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w, int h) {
        // Do nothing
    }

    @Override
    protected void paintContentBorderBottomEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w, int h) {
        // Do nothing
    }

    @Override
    protected void paintContentBorderLeftEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w, int h) {
        // Do nothing
    }

}
