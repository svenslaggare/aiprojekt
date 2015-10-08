package aiprojekt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Evaluator {

	private NGramModel model;
	private static final String EVALUATE_FILE = "res/evaluation/evaluate.txt";
	private static final int NUM_RESULTS = 10;
	public static void main(String[] args) {
		// this is not for user-learning, another method will be used for that case.
		Evaluator evaluator = new Evaluator();
		evaluator.evaluate();
		
		
		

	}

	private void evaluate() {
		// train model
		model = trainModel();
		WordPredictor predictor = new WordPredictor(model, NUM_RESULTS);

		// extract testing sentences from test data != training data
		File file = new File(EVALUATE_FILE);
		ArrayList<ArrayList<Token>> sentences = processFiles(file); 
		for(ArrayList<Token> sentence : sentences){
			List<Token> sentenceBuilder = new ArrayList<Token>();
			List<String> predictedWords = new ArrayList<String>();
			for(Token token : sentence){
				sentenceBuilder.add(token);
				predictedWords = predictor.predictNextWord(sentenceBuilder);
				// test if actual next word == any predicted word.0
			}
			
			
		}
		// for every test sentence:
		//		predict next coming word
		//		check top 10 predictions
		//		If predicted, remember which position in the top 10 list
		//		Otherwise, increase counter of not predicted
		
		
	}

	private NGramModel trainModel() {
		PreProcessor processor = new PreProcessor();
		processor.run();
		return processor.getNgramModel();
		
	}
	
	/**
	 * Tokenizes and indexes the file @code{file}. If @code{file} is a
	 * directory, all its files and subdirectories are recursively processed.
	 */
	private ArrayList<ArrayList<Token>> processFiles(File file) {
		TextParser parser = new TextParser();
		ArrayList<ArrayList<Token>> listOfSentences = new ArrayList<ArrayList<Token>>();
		
		// do not try to tokenize fs that cannot be read
		if (file.canRead()) {
			if (file.isDirectory()) {
				String[] fs = file.list();
				// an IO error could occur
				if (fs != null) {
					for (int i = 0; i < fs.length; i++) {
						processFiles(new File(file, fs[i]));
					}
				}
			} else {
				try (BufferedReader br = new BufferedReader(new FileReader(file))) {
					String sentence;
					while ((sentence = br.readLine()) != null) {
						listOfSentences.add((ArrayList<Token>) parser.tokenize(sentence));
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	return listOfSentences;
	}

}
