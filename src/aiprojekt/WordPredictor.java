package aiprojekt;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a word predictor
 */
public class WordPredictor {
	private final NGramModel model;
	private final int numResults;

	/**
	 * Creates a new word predictor
	 * @param model The n-gram model
	 * @param numResults The number of results
	 */
	public WordPredictor(NGramModel model, int numResults) {
		this.model = model;
		this.numResults = numResults;
	}

	/**
	 * Predicting the next word given a input row.
	 * @param input Words to predict next word from
	 */
	public List<String> predictNextWord(String input) {
		TextParser parser = new TextParser();
		List<Token> tokens = parser.tokenize(input);
		
		//Remove the end of sentence from the tokens
		tokens.remove(tokens.size() - 1);
				
		// Use the last words of the sentence if if it's longer than biggest n-gram
		if (tokens.size() >= model.maxLength()) {
			int diff = (tokens.size() + 1) - model.maxLength();
			for (int i = 0; i < diff; i++) {
				tokens.remove(0);
			}
		}
		
		NGram ngram = new NGram(tokens.toArray(new Token[tokens.size()]));
		List<NGramModel.Result> result = model.predictNext(ngram, numResults);		
		List<String> predictedWords = new ArrayList<String>();

		for (int i = 0; i < result.size(); i++) {
			NGram resultGram = result.get(i).getNGram();
			int indexForLast = resultGram.length() - 1;
			predictedWords.add(resultGram.at(indexForLast).toString());
		}

		return predictedWords;
	}
}