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
		List<Token> list = parser.tokenize(input);

		// Use the last words of the sentence if if it's longer than biggest
		// n-gram
		if (model.maxLength() > list.size()) {
			int diff = model.maxLength() - list.size();
			for (int i = 0; i < diff; i++) {
				list.remove(i);
			}
		}

		list.remove(0);
		list.remove(list.size() - 1);

		Token[] tokens = list.toArray(new Token[list.size()]);
		NGram ngram = new NGram(tokens);

		List<NGramModel.Result> result = model.predictNext(ngram, numResults);
		List<String> predictedWords = new ArrayList<String>();

		for (int i = 0; i < result.size(); i++) {
			int indexForLast = result.get(i).getNGram().length();
			predictedWords.add(result.get(i).getNGram().at(indexForLast - 1).toString());
		}

		return predictedWords;
	}
}