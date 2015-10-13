package aiprojekt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a word predictor
 */
public class WordPredictor {
	private final NGramModel model;
	private final int numResults;
	private final TextParser parser = new TextParser();
	
	private final NGramModel userModel;
	private int timesUser = 0;
	
	/**
	 * Creates a new word predictor
	 * @param model The n-gram model
	 * @param numResults The number of results
	 */
	public WordPredictor(NGramModel model, int numResults) {
		this.model = model;
		this.numResults = numResults;
		this.userModel = new NGramModel(model.maxLength());
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
		this.userModel.clearCache();
		for (NGram ngram : NGramModel.getNgrams(tokens, this.model.maxLength())) {
			this.userModel.addNGram(ngram, 1);
		}
		
		this.timesUser++;
		this.userModel.end(false);
	}

	/**
	 * Uses only the most recent tokens
	 * @param tokens The tokens
	 */
	public List<Token> useRecentTokens(List<Token> tokens) {
		// Use the last words of the sentence if if it's longer than biggest n-gram
		if (tokens.size() >= model.maxLength()) {
			int diff = (tokens.size() + 1) - model.maxLength();
			for (int i = 0; i < diff; i++) {
				tokens.remove(0);
			}
		}
		return tokens;
	}
	
	/**
	 * Predicting the next word given a input row.
	 * @param input Words to predict next word from
	 */
	public List<String> predictNextWord(String input) {
		return this.predictNextWord(parser.tokenize(input), true);
	}
	
	/**
	 * Combines the results with the given map
	 * @param resultMap The map
	 * @param results The results
	 * @param weight The weight
	 */
	private void combineResults(
			Map<NGram, NGramModel.Result> resultMap,
			List<NGramModel.Result> results,
			double weight) {
		for (NGramModel.Result result : results) {
			NGram ngram = result.getNGram();
			double currentProbability = 0.0;
			
			if (resultMap.containsKey(ngram)) {
				currentProbability = resultMap.get(ngram).getProbability();
			}
			
			resultMap.put(
				ngram,
				new NGramModel.Result(ngram, currentProbability + weight * result.getProbability()));
		}
	}

	/**
	 * Calculates the alpha value
	 */
	private double calculateAlpha() {
		//The values for a, b, c is the polynomial of degree 2 that fits:
		//[(1, 0.01), (50, 0.05), (100, 0.2)] 
		double a = 0.000022057307772;
		double b = -0.000308596165739;
		double c = 0.010286538857967;
		
		return 1 - Math.min(0.2, (a * this.timesUser * this.timesUser + b * this.timesUser + c));
	}
	
	/**
	 * Returns the probability of observing the given unigram given a n-gram
	 * @param ngram The n-gram
	 * @param unigram The unigram
	 */
	public double getProbability(NGram ngram, NGram unigram) {
		if (this.timesUser > 0) {
			double alpha = this.calculateAlpha();
			double userProb = this.userModel.getProbability(ngram, unigram);
			double modelProb = this.model.getProbability(ngram, unigram);
			return alpha * modelProb + (1 - alpha) * userProb;
		} else {
			return this.model.getProbability(ngram, unigram);
		}
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
		tokens = this.useRecentTokens(tokens);

		NGram ngram = new NGram(tokens.toArray(new Token[tokens.size()]));
		List<NGramModel.Result> results = null;
		
		if (this.timesUser > 0) {
			Map<NGram, NGramModel.Result> resultMap = new HashMap<>();
			double alpha = this.calculateAlpha();
			
			this.combineResults(resultMap, this.model.predictNext(ngram, numResults), alpha);
			this.combineResults(resultMap, this.userModel.predictNext(ngram, numResults), 1 - alpha);
						
			results = new ArrayList<>(resultMap.values());	
			Collections.sort(results);
			int size = results.size();
			for (int i = 0; i < size - this.numResults; i++) {
				results.remove(results.size() - 1);
			}
		} else {
			results = this.model.predictNext(ngram, numResults);
		}
				
		List<String> predictedWords = new ArrayList<String>();
		
		for (int i = 0; i < results.size(); i++) {
			NGram resultGram = results.get(i).getNGram();
			int indexForLast = resultGram.length() - 1;
			predictedWords.add(resultGram.at(indexForLast).toString());
		}

		return predictedWords;
	}
}