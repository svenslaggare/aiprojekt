package aiprojekt;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Evaluator2 {		
	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		Loader loader = new Loader();
		NGramModel model = loader.load(PreProcessor.FILE_PATH);
		WordPredictor predictor = new WordPredictor(model, 10);
		
		TextParser parser = new TextParser();
		
		int tests = 0;
		int correct = 0;
		
		long last = System.currentTimeMillis();
		int lines = 0;
		
		//Parse the sentences
		List<List<Token>> sentences = new ArrayList<>();
		
		try (BufferedReader br = new BufferedReader(new FileReader(
				"res/evaluation/user_input_testing/2006-12-01-#ubuntu.txt"))) {
			String sentence;
			while ((sentence = br.readLine()) != null) {
				List<Token> tokens = parser.tokenize(sentence);
				sentences.add(tokens);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//Predict
		List<Token> newTokens = new ArrayList<>();
		for (List<Token> tokens : sentences) {
			newTokens.clear();
			
			for (int i = 0; i < tokens.size() - 2; i++) {
				newTokens.add(tokens.get(i));
				
				String correctWord = tokens.get(i + 1).getWord();
				
				for (String predicted : predictor.predictNextWord(newTokens, false)) {
					if (predicted.equals(correctWord)) {
						correct++;
						break;
					}
				}
				
				tests++;
			}
			
			lines++;
			
			if (System.currentTimeMillis() - last > 10000) {
				System.out.println("Lines: " + lines + "/" + sentences.size());
				System.out.println("Stats: " + correct + "/" + tests);
				last = System.currentTimeMillis();
			}
		}
				
		System.out.println("Final stats: " + correct + "/" + tests);
		System.out.println("Run time: " + (System.currentTimeMillis() - start) / 1000.0 + " s");
	}
}
