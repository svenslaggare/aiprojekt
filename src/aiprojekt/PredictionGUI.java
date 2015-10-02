package aiprojekt;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

public class PredictionGUI {
	private WordPredictor wordPredictor;
	
	private JFrame frame;
	private JTextArea chat;
	private JScrollPane chatScroll;
	private JTextField inputField;
	private JButton sendButton;
	private JLabel nextWordProposals;
	
	public PredictionGUI(NGramModel ngramModel) {
		wordPredictor = new WordPredictor(ngramModel, 10);
		
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
		frame.add(nextWordProposals);
		
		// Updates the proposed next words when space is pressed.
		InputMap inputMap = inputField.getInputMap(JComponent.WHEN_FOCUSED);
		inputMap.put(KeyStroke.getKeyStroke("SPACE"), "updateWordProposals");
		
		ActionMap actionMap = inputField.getActionMap();
		actionMap.put("updateWordProposals", new AbstractAction(){
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				if (wordPredictor != null) {
					List<String> proposals = wordPredictor.predictNextWord(inputField.getText());
					
					StringBuilder sb = new StringBuilder("<html>");
					
					int place = 1;
					for (String word : proposals) {
						sb.append(place++ + ". " + word + "<br />");
					}
					
					sb.append("</html>");
					
					nextWordProposals.setText(sb.toString());
				}
			}
		});
		
		frame.getRootPane().setDefaultButton(sendButton);
		frame.setVisible(true);
		
		inputField.requestFocusInWindow();
	}
	
	public static void main(String[] args) {
		List<List<Token>> sentences = new ArrayList<List<Token>>();
		
		TextParser parser = new TextParser();
		File file = new File("ubuntu.txt");
		
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String sentence;
			while ((sentence = br.readLine()) != null) {
				sentences.add(parser.tokenize(sentence));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		NGramModel ngramModel = new NGramModel(3);
		for (List<Token> sentence : sentences) {
			ngramModel.processTokens(sentence);
		}
		
		new PredictionGUI(ngramModel);
	}
}
