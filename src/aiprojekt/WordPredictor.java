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
	private final double[] ngramAverages;
	
	private final int maxCountPerAverage = 3000;
	
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
		this.ngramAverages = new double[model.maxLength()];
		this.updateNGramsAverages();
		this.userModel = new NGramModel(model.maxLength());
	}
	
	/**
	 * Updates the n-gram averages
	 */
	private void updateNGramsAverages() {
		for (int i = 1; i <= this.model.maxLength(); i++) {
			double sum = 0.0;
			int count = 0;
			for (NGramTree.Result current : this.model.searchTree().findTopNgrams(i, maxCountPerAverage)) {
				if (!(current.equals(NGramModel.START_OF_SENTENCE_UNIGRAM)
					  || current.equals(NGramModel.END_OF_SENTENCE_UNIGRAM) )) {
					sum += current.getCount();
					count++;
				}
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
//		for (NGram ngram : NGramModel.getNgrams(tokens, this.model.maxLength())) {
////			double ngramAverage = 
////				(double)this.model.totalCountForNGramLength(ngram.length())
////				/ this.model.numberOfNGramLength(ngram.length());
//			double ngramAverage = this.ngramAverages[ngram.length() - 1];			
//			
////			System.err.println(ngram + ": " + ngramAverage);
//			
//			this.model.addNGram(ngram, (int)Math.round(ngramAverage));
//		}
//		
//		this.model.clearCache();
		this.userModel.clearCache();
		for (NGram ngram : NGramModel.getNgrams(tokens, this.model.maxLength())) {
			this.userModel.addNGram(ngram, 1);
		}
		
		this.timesUser++;
		this.userModel.end();
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
		List<NGramModel.Result> results = null;
		
		if (this.timesUser > 0) {
			Map<NGram, NGramModel.Result> resultMap = new HashMap<>();
			//The values for a,b,c are the polynomial of degree 2 that fits: [(1, 0.01), (50, 0.15), (100, 0.5)] 
			double a = 4.184704184704183e-05;
			double b = 7.229437229437245e-04;
			double c = 0.009235209235209;
			double alpha = 1 - Math.min(0.5, (a * this.timesUser * this.timesUser + b * this.timesUser + c));
			
			this.combineResults(resultMap, this.model.predictNext(ngram, numResults), alpha);
			this.combineResults(resultMap, this.userModel.predictNext(ngram, numResults), 1 - alpha);
			
			results = new ArrayList<>(resultMap.values());	
			Collections.sort(results);
			for (int i = 0; i < results.size() - this.numResults; i++) {
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