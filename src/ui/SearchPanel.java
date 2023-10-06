package ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicScrollBarUI;

import agents.AgentUtils;
import agents.BaseAgent;

public class SearchPanel extends JPanel implements Runnable{
	
	private GameFrame frameRef;
	
	private JTextField searchTextField;
	private JPanel contentPanel;
	private JPanel resultsPanel;
	private JScrollPane scrollArea;
	
	private PauseButton pauseBtn;
	
	private final long UPDATE_TIMES_PER_SECOND = 3;
	private final long MILLIS_PER_SECOND = 1000;
	private long nextUpdateTime;
	private boolean canUpdate = true;
	
	private Thread updateThread;
	
	private HashMap<String, SearchEntry> shownSearchedEntries;
	
	public SearchPanel(GameFrame frameRef) {
		this.frameRef = frameRef;
		
		this.setVisible(true);
		this.setLayout(null);
		this.setBounds(frameRef.getWidth()-500, 0, 500, 500);
		this.setBackground(new Color(0,0,0,0));
		
		shownSearchedEntries = new HashMap<>();
		
		pauseBtn = new PauseButton();
		pauseBtn.setBounds(400,0,50,50);
		pauseBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				frameRef.pauseMap();
				pauseBtn.toggleIcon();
			}
			
		});
		this.add(pauseBtn);
		
		SearchButton openBtn = new SearchButton();
		openBtn.setBounds(450,0,50,50);
		openBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				contentPanel.setVisible(!contentPanel.isVisible());
			}
		});
		this.add(openBtn);

		contentPanel = new JPanel();
		contentPanel.setBounds(0,50,500,450);
		contentPanel.setVisible(false);
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		contentPanel.setOpaque(true);
		contentPanel.setBackground(new Color(0,0,0,100));
		this.add(contentPanel);
		
		contentPanel.add(Box.createRigidArea(new Dimension(0,10)));
		
		JPanel searchToolsPanel = new JPanel();
		searchToolsPanel.setLayout(new BoxLayout(searchToolsPanel, BoxLayout.X_AXIS));
		searchToolsPanel.setOpaque(false);
		searchToolsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		contentPanel.add(searchToolsPanel);
		
		JLabel searchLabel = new JLabel("Agent Name");
		searchLabel.setMaximumSize(new Dimension(150,50));
		searchLabel.setMinimumSize(new Dimension(150,50));
		searchLabel.setFont(new Font("VCR OSD MONO", 1, 20));
		searchLabel.setForeground(new Color(253,231,111,255));
		searchToolsPanel.add(searchLabel);
		
		searchTextField = new JTextField();
		searchTextField.setMaximumSize(new Dimension(320,50));
		searchTextField.setMinimumSize(new Dimension(320,50));
		searchTextField.setFont(new Font("VCR OSD MONO", 1, 20));
		searchTextField.setForeground(new Color(253,231,111,255));
		searchTextField.setBackground(Color.black);
		searchTextField.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				frameRef.setUsingSearch(false);
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				frameRef.setUsingSearch(true);
			}
		});
		searchToolsPanel.add(searchTextField);
		
		contentPanel.add(Box.createRigidArea(new Dimension(0,20)));
		
		resultsPanel = new JPanel();
		resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
		resultsPanel.setOpaque(true);
		resultsPanel.setBackground(Color.black);
		
		scrollArea = new JScrollPane(resultsPanel);
		scrollArea.setMaximumSize(new Dimension(480,350));
		scrollArea.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollArea.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
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
		scrollArea.setOpaque(true);
		scrollArea.setBackground(Color.black);
		contentPanel.add(scrollArea);
		
		contentPanel.add(Box.createRigidArea(new Dimension(0,10)));
		
		nextUpdateTime = System.currentTimeMillis();
		
		updateThread = new Thread(this);
		updateThread.start();
	}
	
	private void setResults(String filter) {
		int counter = 0;
		
		ArrayList<BaseAgent> agents = new ArrayList<>();
		agents.addAll(AgentUtils.agentsArray);
		
		ArrayList<Component> componentsToRemove = new ArrayList<>();
		
		for(int i = 0; i < resultsPanel.getComponents().length; i++) {
			componentsToRemove.add(resultsPanel.getComponent(i));
		}
		
		for(BaseAgent agent : agents) {
			if(agent.getDisplayName().toLowerCase().contains(filter.toLowerCase())) {
				counter++;
				
				if(shownSearchedEntries.containsKey(agent.getDisplayName())) {
					componentsToRemove.remove(shownSearchedEntries.get(agent.getDisplayName()));
					continue;
				}
				
				SearchEntry entry = new SearchEntry(frameRef, agent);
				resultsPanel.add(entry);
				shownSearchedEntries.put(agent.getDisplayName(), entry);
			}
			else {
				shownSearchedEntries.remove(agent.getDisplayName());
			}
		}
		
		for (Component component : componentsToRemove) {
			resultsPanel.remove(component);
		}
		
		
		int height = Math.max(counter*50, 350);
		resultsPanel.setSize(new Dimension(480, height));
		scrollArea.validate();
		canUpdate = true;
	}

	@Override
	public void run() {
		while(true) {
			if(shouldUpdate()) {
				canUpdate = false;
				setResults(searchTextField.getText());
			}
		}
	}
	
	private boolean shouldUpdate() {
		if(nextUpdateTime <= System.currentTimeMillis()){
	    	nextUpdateTime = System.currentTimeMillis() + MILLIS_PER_SECOND / UPDATE_TIMES_PER_SECOND;
	        return canUpdate;
	    }
	 
	    return false;
	}

	public void stop() {
		updateThread.stop();
	}

	public void updatePauseButton() {
		pauseBtn.toggleIcon();
	}
}
