package ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ControlsPanel extends JPanel{
	
	public ControlsPanel(GameFrame frameRef) {
		super();
		this.setVisible(true);
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setBounds(frameRef.getBounds());
		this.setOpaque(false);
		this.setBackground(new Color(0, 0, 0, 100));
		this.setAlignmentX(CENTER_ALIGNMENT);
		
		JPanel alignmentPanel = new JPanel();
		alignmentPanel.setMaximumSize(new Dimension(frameRef.getWidth()/3, frameRef.getHeight()));
		alignmentPanel.setOpaque(false);
		alignmentPanel.setBackground(new Color(0, 0, 0, 100));
		alignmentPanel.setLayout(new BoxLayout(alignmentPanel, BoxLayout.X_AXIS));
		alignmentPanel.setAlignmentX(CENTER_ALIGNMENT);
		this.add(alignmentPanel);
		
		JPanel contentPanel = new JPanel();
		contentPanel.setBackground(Color.black);
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		contentPanel.setAlignmentY(CENTER_ALIGNMENT);
		contentPanel.setMaximumSize(new Dimension(alignmentPanel.getWidth(), frameRef.getHeight()/4));
		alignmentPanel.add(contentPanel);
		
		JLabel text = new JLabel("<html>Left Mouse Button - Place object/Show agent info<br><br>"
				+ "Right Mouse Button - Delete object<br><br> P - Pause/Unpause time</html>");
		text.setFont(new Font("VCR OSD MONO", 1, 20));
		text.setForeground(new Color(253,231,111));
		text.setBounds(10, 10, 300, 50);
		text.setAlignmentX(CENTER_ALIGNMENT);
		contentPanel.add(text);
		
		contentPanel.add(Box.createRigidArea(new Dimension(0,50)));
		
		MenuButton backBtn = new MenuButton("Back", 50);
		backBtn.setSize(500, 300);
		backBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
		contentPanel.add(backBtn);
		backBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				frameRef.backToPauseMenu();
			}
		});
		
		this.addMouseListener(new MouseListener() {
			
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
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	@Override
	public void paint(Graphics g) {
		g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
		super.paint(g);
	}
}