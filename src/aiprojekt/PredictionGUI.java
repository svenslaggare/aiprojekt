package aiprojekt;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class PredictionGUI {
	private static JFrame frame;
	private static JTextField inputField;
	private static JButton sendButton;
	private static JLabel nextWordProposals;
	
	public static void main(String[] args) {
		frame = new JFrame("Next Word Predictor");
		frame.setLayout(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(316, 250);
		frame.setResizable(false);
		
		inputField = new JTextField();
		inputField.setBounds(5, 8, 250, 20);
		frame.add(inputField);
		
		sendButton = new JButton("Send");
		sendButton.setBounds(255, 8, 50, 20);
		sendButton.setMargin(new Insets(0, 0, 0, 0));
		frame.add(sendButton);
	    
		// Label for the result.
		nextWordProposals = new JLabel();
		nextWordProposals.setBounds(5, 30, 300, 186);
		nextWordProposals.setBorder(BorderFactory.createTitledBorder("Next Word Proposals"));
		nextWordProposals.setVerticalAlignment(JLabel.TOP);
		nextWordProposals.setText(
				"<html>"
						+ "1. you<br />2. him<br />"
						+ "3. you<br />4. him<br />"
						+ "5. you<br />6. him<br />"
						+ "7. you<br />8. him<br />"
						+ "9. you<br />10. him<br />"
				+ "</html>");
		frame.add(nextWordProposals);
		
		frame.setVisible(true);
	}
}
