package aiprojekt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a word predictor
 */
public class WordPredictor {
	private final NGramModel model;
	private final int numResults;
	private final TextParser parser = new TextParser();
	private final double[] ngramAverages;
	
	private final int maxCountPerAverage = 3000;
	
	/**
	 * Creates a new word predictor
	 * @param model The n-gram model
	 * @param numResults The number of results
	 */
	public WordPredictor(NGramModel model, int numResults) {
		this.model = model;
		this.numResults = numResults;
		this.ngramAverages = new double[model.maxLength()];
		this.updateNGramsAverages();
	}
	
	/**
	 * Updates the n-gram averages
	 */
	private void updateNGramsAverages() {
		for (int i = 1; i <= this.model.maxLength(); i++) {
			double sum = 0.0;
			int count = 0;
			for (NGramTree.Result current : this.model.searchTree().findTopNgrams(i, maxCountPerAverage)) {
				sum += current.getCount();
				count++;
			}
			
			this.ngramAverages[i - 1] = sum / count;
		}
	}
	
	/**
	 * Returns the n-gram model
	 */
	public NGramModel getModel() {
		return this.model;
	}
	
	/**
	 * Adds the given line to the history (and model)
	 * @param line The line to add
	 */
	public void addHistory(String line) {
		this.addHistory(this.parser.tokenize(line));
	}
	
	/**
	 * Adds the given tokens to the history (and model)
	 * @param tokens The tokens
	 */
	public void addHistory(List<Token> tokens) {	
		for (NGram ngram : NGramModel.getNgrams(tokens, this.model.maxLength())) {
//			double ngramAverage = 
//				(double)this.model.totalCountForNGramLength(ngram.length())
//				/ this.model.numberOfNGramLength(ngram.length());
			double ngramAverage = this.ngramAverages[ngram.length() - 1];			
			
			System.err.println(ngram + ": " + ngramAverage);
			
			this.model.addNGram(ngram, (int)Math.round(ngramAverage));
		}
		
		this.model.clearCache();	
	}

	/**
	 * Uses only the most recent tokens
	 * @param tokens The tokens
	 */
	public void useRecentTokens(List<Token> tokens) {
		// Use the last words of the sentence if if it's longer than biggest n-gram
		if (tokens.size() >= model.maxLength()) {
			int diff = (tokens.size() + 1) - model.maxLength();
			for (int i = 0; i < diff; i++) {
				tokens.remove(0);
			}
		}
	}
	
	/**
	 * Predicting the next word given a input row.
	 * @param input Words to predict next word from
	 */
	public List<String> predictNextWord(String input) {
		return this.predictNextWord(parser.tokenize(input), true);
	}

	/**
	 * Predicting the next word given a input row.
	 * @param input list of Tokens to predict next word from
	 */
	public List<String> predictNextWord(List<Token> tokens, boolean removeLastToken) {
		// Remove the end of sentence from the tokens
		if (removeLastToken && tokens.size() > 0) {
			tokens.remove(tokens.size() - 1);
		}
		
		// Use the last words of the sentence if if it's longer than biggest
		// n-gram
		this.useRecentTokens(tokens);

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