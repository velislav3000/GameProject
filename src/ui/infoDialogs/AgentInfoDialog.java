package ui.infoDialogs;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.plaf.basic.BasicScrollBarUI;

import agents.BaseAgent;
import agents.BaseEntityAgent;
import agents.heroAgents.BaseHeroAgent;
import agents.heroAgents.CityAgent;
import agents.items.BaseItem;
import agents.items.equipment.Equipment;
import agents.items.potions.BasePotion;
import agents.monsterAgents.BaseMonsterAgent;
import collectiables.materials.BaseMaterial.MaterialType;

public abstract class AgentInfoDialog extends JPanel implements Runnable{

	private final long UPDATE_TIMES_PER_SECOND = 2;
	private final long MILLIS_PER_SECOND = 1000;
	private long nextUpdateTime;
	protected boolean shouldUpdate = true;
	
	private Thread updateThread;
	
	protected JScrollPane logScrollPane;
	protected JLabel logLabel;
	protected boolean scrollbarIsUsed;
	
	protected JButton nameLabel;
	
	protected void init() {
		nextUpdateTime = System.currentTimeMillis();
		setOpaque(false);
		setBackground(new Color(0,0,0,100));
		
		setComponentStyle();
		
		updateThread = new Thread(this);
		updateThread.start();
	}

	@Override
	public void run() {
		while(shouldUpdate) {
			if(needToUpdate()) {
				updateStats();
			}
		}
	}

	private boolean needToUpdate() {
	    if(nextUpdateTime <= System.currentTimeMillis()){
	    	nextUpdateTime = System.currentTimeMillis() + MILLIS_PER_SECOND / UPDATE_TIMES_PER_SECOND;
	        return true;
	    }
	 
	    return false;
	}
	
	protected abstract void updateStats();

	protected boolean checkIfShouldUpdate(BaseAgent agentRef) {
		System.out.println("Update: " + agentRef != null && !agentRef.shouldBeRemoved());
		return (agentRef != null && !agentRef.shouldBeRemoved());
	}
	
	public void close() {
		shouldUpdate = false;
		updateThread.stop();
	}
	
	protected void setComponentStyle() {
		Component[] components = this.getComponents();
		for(int i = 0; i<components.length; i++) {
			components[i].setFont(new Font("VCR OSD MONO", 1, components[i].getFont().getSize()));
			components[i].setForeground(new Color(253,231,111,255));
			components[i].setBackground(new Color(0,0,0,0));
			if(components[i] instanceof JScrollPane) {
				Component component = ((JScrollPane) components[i]).getComponent(0);
				component.setFont(new Font("VCR OSD MONO", 1, components[i].getFont().getSize()));
				component.setForeground(new Color(253,231,111,255));
				component.setBackground(new Color(0,0,0));
				if(component instanceof Container) {
					Component[] innerComponents = ((Container) component).getComponents();
					for(int j = 0; j<innerComponents.length; j++) {
						innerComponents[j].setFont(new Font("VCR OSD MONO", 1, components[i].getFont().getSize()));
						innerComponents[j].setForeground(new Color(253,231,111,255));
						innerComponents[j].setBackground(new Color(0,0,0,0));
					}
				}
				((JScrollPane) components[i]).getVerticalScrollBar().setUI(new BasicScrollBarUI() {
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
				((JScrollPane) components[i]).getHorizontalScrollBar().setUI(new BasicScrollBarUI() {
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
			}
		}
	}
}
