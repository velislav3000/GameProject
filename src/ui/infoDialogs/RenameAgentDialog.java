package ui.infoDialogs;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;

import agents.BaseAgent;

public class RenameAgentDialog extends JDialog{
	
	public RenameAgentDialog(BaseAgent agentRef) {
		this.setVisible(true);
		this.setResizable(false);
		this.setLayout(null);
		this.setAlwaysOnTop(true);
		this.setSize(500, 120);
		
		RenameAgentDialog dialogRef = this;

		JLabel nameLabel = new JLabel("Name:");
		nameLabel.setBounds(10, 10, 150, 50);
		nameLabel.setFont(new Font(nameLabel.getFont().getName(), Font.PLAIN, 20));
		this.add(nameLabel);
		
		JTextField nameTextField = new JTextField();
		nameTextField.setBounds(170, 10, 200, 50);
		nameTextField.setFont(new Font(nameLabel.getFont().getName(), Font.PLAIN, 20));
		nameTextField.setText(agentRef.getDisplayName());
		this.add(nameTextField);
		
		JButton saveBtn = new JButton("Save");
		saveBtn.setBounds(380, 10, 100, 50);
		this.add(saveBtn);
		saveBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				agentRef.setDisplayName(nameTextField.getText());
				dialogRef.setVisible(false);
				dialogRef.dispose();
			}
		});
	}
	
}