package ui;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class SaveGameDialog extends JDialog{
	
	public SaveGameDialog(GameFrame frameRef) {
		this.setVisible(true);
		this.setResizable(false);
		this.setLayout(null);
		this.setAlwaysOnTop(true);
		this.setSize(500, 120);
		
		SaveGameDialog dialogRef = this;

		JLabel saveLabel = new JLabel("Save Name");
		saveLabel.setBounds(10, 10, 150, 50);
		saveLabel.setFont(new Font(saveLabel.getFont().getName(), Font.PLAIN, 20));
		this.add(saveLabel);
		
		JTextField saveTextField = new JTextField();
		saveTextField.setBounds(170, 10, 200, 50);
		saveTextField.setFont(new Font(saveLabel.getFont().getName(), Font.PLAIN, 20));
		this.add(saveTextField);
		
		JButton saveBtn = new JButton("Save");
		saveBtn.setBounds(380, 10, 100, 50);
		this.add(saveBtn);
		saveBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				frameRef.saveGame(saveTextField.getText().replace(".", ""));
				dialogRef.setVisible(false);
				dialogRef.dispose();
			}
		});
	}
	
}