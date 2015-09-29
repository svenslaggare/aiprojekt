package aiprojekt;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.text.DefaultCaret;

public class PredictionGUI {
	private static JFrame frame;
	private static JTextArea chat;
	private static JScrollPane chatScroll;
	private static JTextField inputField;
	private static JButton sendButton;
	private static JLabel nextWordProposals;
	
	public static void main(String[] args) {
		frame = new JFrame("Next Word Predictor");
		frame.setLayout(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(316, 450);
		frame.setResizable(false);
		
		chat = new JTextArea();
		chat.setLineWrap(true);
		chat.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		chat.setEditable(false);
		chatScroll = new JScrollPane(chat, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		chatScroll.setBounds(5, 8, 300, 186);
		frame.add(chatScroll);
		
		inputField = new JTextField();
		inputField.setBounds(5, 200, 250, 20);
		frame.add(inputField);
		
		sendButton = new JButton("Send");
		sendButton.setBounds(255, 200, 50, 20);
		sendButton.setMargin(new Insets(0, 0, 0, 0));
		sendButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				chat.append("- " + inputField.getText() + "\n");
				inputField.setText("");
				inputField.requestFocusInWindow();
			}
		});
		frame.add(sendButton);
	    
		// Label for the result.
		nextWordProposals = new JLabel();
		nextWordProposals.setBounds(5, 230, 300, 186);
		nextWordProposals.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Next Word Proposals"),
				BorderFactory.createEmptyBorder(0, 3, 0, 3)));
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
		
		// Updates the proposed next words when space is pressed.
		InputMap inputMap = inputField.getInputMap(JComponent.WHEN_FOCUSED);
		inputMap.put(KeyStroke.getKeyStroke("SPACE"), "updateWordProposals");
		
		ActionMap actionMap = inputField.getActionMap();
		actionMap.put("updateWordProposals", new AbstractAction(){
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				nextWordProposals.setText(
						"<html>"
								+ "1. test<br />2. him<br />"
								+ "3. test<br />4. him<br />"
								+ "5. test<br />6. him<br />"
								+ "7. test<br />8. him<br />"
								+ "9. test<br />10. him<br />"
						+ "</html>");
			}
		});
		
		frame.getRootPane().setDefaultButton(sendButton);
		frame.setVisible(true);
		
		inputField.requestFocusInWindow();
	}
}
