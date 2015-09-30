package aiprojekt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents n-gram model
 */
public class NGramModel {
	private final Map<NGram, Integer> ngrams = new HashMap<NGram, Integer>();
	private final int maxLength;
	private int totalCount = 0;
	
	/**
	 * Creates a new N-gram model
	 * @param maxLength The maximum length of a n-gram
	 */
	public NGramModel(int maxLength) {
		this.maxLength = maxLength;
	}
	
	/**
	 * Returns the maximum length of a n-gram
	 */
	public int maxLength() {
		return this.maxLength;
	}
	
	/**
	 * Returns the n-grams
	 */
	public Map<NGram, Integer> getNgrams() {
		return this.ngrams;
	}
	
	/**
	 * Returns the n-grams in the given tokens
	 * @param tokens The tokens
	 * @param maxLength The maximum length of a n-gram
	 * @return The n-grams
	 */
 	public static List<NGram> getNgrams(List<Token> tokens, int maxLength) {
		List<NGram> nGrams = new ArrayList<NGram>();
		
		for (int i = 0; i < tokens.size(); i++) {
			List<Token> nGram = new ArrayList<Token>();
			
			//Creates the unigram, bigrams, trigrams, ...
			for (int j = i; j < Math.min(i + maxLength, tokens.size()); j++) {
				nGram.add(tokens.get(j));
				nGrams.add(new NGram(nGram.toArray(new Token[nGram.size()]), false));
			}
		}
		
		return nGrams;
	}
	
	/**
	 * Process the given tokens, adding them to the model
	 * @param tokens The tokens
	 */
	public void processTokens(List<Token> tokens) {
		for (NGram nGram : getNgrams(tokens, this.maxLength)) {
			int count = 0;
			
			if (this.ngrams.containsKey(nGram)) {
				count = this.ngrams.get(nGram);
			}
			
			this.ngrams.put(nGram, count + 1);
			this.totalCount++;
		}
	}
	
	/**
	 * Returns the count for the given n-gram
	 * @param ngram The n-gram
	 */
	public int getCount(NGram ngram) {
		if (this.ngrams.containsKey(ngram)) {
			return this.ngrams.get(ngram);
		}
		
		return 0;
	}
	
	/**
	 * Returns a result for a word prediction
	 */
	public static class Result implements Comparable<Result> {
		private final NGram nGram;
		private final double probability;
		
		/**
		 * Creates a new result
		 * @param ngram The n-gram
		 * @param probability The probability
		 */
		public Result(NGram ngram, double probability) {
			this.nGram = ngram;
			this.probability = probability;;
		}
		
		/**
		 * Returns the N-gram
		 */
		public NGram getNGram() {
			return nGram;
		}

		/**
		 * Returns the probability
		 */
		public double getProbability() {
			return probability;
		}

		@Override
		public int compareTo(Result other) {
			return Double.compare(other.probability, this.probability);
		}
		
		@Override
		public String toString() {
			return "{ n-gram: " + this.nGram + ", p: " + this.probability + " }";
		}
	}
	
	/**
	 * Predicts the most probable (n+1)-gram for the given n-gram
	 * @param ngram The n-gram
	 * @param numResults The number of results
	 */
	public List<Result> predictNext(NGram ngram, int numResults) {
		//Atm, this method is REALLY stupid :)
		
		List<Result> results = new ArrayList<Result>();
		
		for (Map.Entry<NGram, Integer> current : this.ngrams.entrySet()) {
			NGram currentNgram = current.getKey();
			
			if (currentNgram.startsWith(ngram, false)) {
				int subgramCount = this.getCount(currentNgram.subgram(currentNgram.length() - 1));
				
				if (subgramCount > 0) {
					double probability = (double)current.getValue() / subgramCount;
					results.add(new Result(currentNgram, probability));
				}
			}
		}
		
		Collections.sort(results);
		
		for (int i = results.size() - 1; i >= numResults; i--) {
			results.remove(results.size() - 1);
		}
		
		return results;
	}

	/**
	 * Predicts the most probable (n+1)-gram for the given n-gram
	 * @param tree The search tree
	 * @param ngram The n-gram
	 * @param numResults The number of results
	 */
	public List<Result> predictNext(NGramTree tree, NGram ngram, int numResults) {
		List<Result> results = new ArrayList<Result>();
		
		int subgramCount = this.getCount(ngram);
		
		if (subgramCount > 0) {
			for (NGramTree.Result result : tree.findResults(ngram)) {
				double probability = (double)result.getCount() / subgramCount;
				results.add(new Result(ngram.append(result.getNgram()), probability));
			}
		}
		
		Collections.sort(results);
		
		for (int i = results.size() - 1; i >= numResults; i--) {
			results.remove(results.size() - 1);
		}
		
		return results;
	}
}
