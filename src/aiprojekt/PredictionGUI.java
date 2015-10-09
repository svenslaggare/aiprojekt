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
	
	private final JTextField inputField;
	private final JLabel nextWordProposals;
	
	private static final String SPACE_PRESSED = "spacePressed";
	private static final String BACKSPACE_PRESSED = "backspacePressed";
	
	// Use pre-processed NGrams
	private static final boolean USE_LOADER = false;
	private static final String LOAD_FILE = "res/bin/ngrams.bin";
	
	public PredictionGUI(NGramModel ngramModel) {
		final JFrame frame;
		final JTextArea chat;
		final JScrollPane chatScroll;
		final JButton sendButton;
		
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
		updateNextWordPredictions("");
		
		// Updates the proposed next words when space is pressed.
		InputMap inputMap = inputField.getInputMap(JComponent.WHEN_FOCUSED);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), SPACE_PRESSED);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), BACKSPACE_PRESSED);
		
		ActionMap actionMap = inputField.getActionMap();
		
		actionMap.put(SPACE_PRESSED, new AbstractAction() {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				updateNextWordPredictions(inputField.getText());
			}
		});
		
		actionMap.put(BACKSPACE_PRESSED, new AbstractAction() {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				int caretPosition = inputField.getCaretPosition();
				if (caretPosition != 0) {
					String fieldText = inputField.getText();
					
					int len = fieldText.length();
					if (len > 0) {
						String[] words = fieldText.split(" ");
						int numWords = fieldText.endsWith(" ") ? words.length : words.length - 1;
						
						StringBuilder sb = new StringBuilder();
						for (int i = 0; i < numWords; i++) {
							sb.append(words[i] + " ");
						}
						String oldWords = sb.toString();
						
						caretPosition--;
						
						String textBerfore = fieldText.substring(0, caretPosition);
						
						if (caretPosition == fieldText.length()) {
							fieldText = textBerfore;
						} else {
							fieldText = textBerfore + fieldText.substring(caretPosition + 1, fieldText.length());
						}
						
						words = fieldText.split(" ");
						numWords = fieldText.endsWith(" ") ? words.length : words.length - 1;
						
						sb = new StringBuilder();
						for (int i = 0; i < numWords; i++) {
							sb.append(words[i] + " ");
						}
						String newWords = sb.toString();
						
						if (!oldWords.equals(newWords)) {
							updateNextWordPredictions(newWords);
						}
						
						inputField.setText(fieldText);
						inputField.setCaretPosition(caretPosition);
					}
				}
			}
		});
		
		frame.getRootPane().setDefaultButton(sendButton);
		frame.setVisible(true);
		
		inputField.requestFocusInWindow();
	}
	
	public static void main(String[] args) {
		if (!USE_LOADER) {
			List<List<Token>> sentences = new ArrayList<List<Token>>();
			TextParser parser = new TextParser();
			File file = new File("res/chatlogs/small_dump_logs/ubuntu.txt");
			
			try (BufferedReader br = new BufferedReader(new FileReader(file))) {
				String sentence;
				while ((sentence = br.readLine()) != null) {
					sentences.add(parser.tokenize(sentence));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			NGramModel ngramModel = new NGramModel(NGramModel.DEFAULT_MAX_NGRAM_LENGTH);
			for (List<Token> sentence : sentences) {
				ngramModel.processTokens(sentence);
			}
			ngramModel.end();
			
			new PredictionGUI(ngramModel);
		} else {
			Loader loader = new Loader();
			new PredictionGUI(loader.load(PreProcessor.FILE_PATH));
		}
	}
	
	private void updateNextWordPredictions(String text) {
		long start = System.currentTimeMillis();
		List<String> proposals = wordPredictor.predictNextWord(text);
		System.err.println("Predict time: " + (System.currentTimeMillis() - start) + "ms");
		
		StringBuilder sb = new StringBuilder("<html>");
		
		int place = 1;
		for (String word : proposals) {
			sb.append(place++ + ". " + word + "<br />");
		}
		
		sb.append("</html>");
		
		nextWordProposals.setText(sb.toString());
	}
}
