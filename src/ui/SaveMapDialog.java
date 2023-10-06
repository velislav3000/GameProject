package ui;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class SaveMapDialog extends JDialog{
	
	public SaveMapDialog(GameFrame frameRef) {
		this.setVisible(true);
		this.setResizable(false);
		this.setLayout(null);
		this.setAlwaysOnTop(true);
		this.setSize(450, 120);
		
		SaveMapDialog dialogRef = this;

		JLabel saveLabel = new JLabel("Map Name");
		saveLabel.setBounds(10, 10, 100, 50);
		saveLabel.setFont(new Font(saveLabel.getFont().getName(), Font.PLAIN, 20));
		this.add(saveLabel);
		
		JTextField saveTextField = new JTextField();
		saveTextField.setBounds(120, 10, 200, 50);
		saveTextField.setFont(new Font(saveLabel.getFont().getName(), Font.PLAIN, 20));
		saveTextField.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				limitCharAmount(saveTextField);
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				limitCharAmount(saveTextField);
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				limitCharAmount(saveTextField);
			}
		});
		this.add(saveTextField);
		
		JButton saveBtn = new JButton("Save");
		saveBtn.setBounds(330, 10, 100, 50);
		this.add(saveBtn);
		saveBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				frameRef.saveMapTemplate(saveTextField.getText().replace(".", ""));
				dialogRef.setVisible(false);
				dialogRef.dispose();
			}
		});
	}
	
	private void limitCharAmount(JTextField textField) {
		Runnable limitChars = new Runnable() {
	        @Override
	        public void run() {
	    		if(textField.getText().length() > 9) {
	    			String text = textField.getText();
	    			text = text.substring(0, 9);
	    			textField.setText(text);
	    		}
	        }
	    };       
	    SwingUtilities.invokeLater(limitChars);
	}
	
}
