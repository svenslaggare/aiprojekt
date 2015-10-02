package aiprojekt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents n-gram model
 */
public class NGramModel {
	private final int maxLength;
	
	private final Map<NGram, Integer> ngrams = new HashMap<NGram, Integer>();
	private final Set<NGram> unigrams = new HashSet<NGram>();
		
	private final int matchThreshold = 0;
	
	private final NGramTree tree = NGramTree.rootTree();
	
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
	 * Returns the search tree
	 */
	public NGramTree searchTree() {
		return this.tree;
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
			List<Token> ngram = new ArrayList<Token>();
			
			//Creates the unigram, bigrams, trigrams, ...
			for (int j = i; j < Math.min(i + maxLength, tokens.size()); j++) {
				ngram.add(tokens.get(j));
				nGrams.add(NGram.fromList(ngram));
			}
		}
		
		return nGrams;
	}
	
	/**
	 * Process the given tokens, adding them to the model
	 * @param tokens The tokens
	 */
	public void processTokens(List<Token> tokens) {
		for (NGram ngram : getNgrams(tokens, this.maxLength)) {
			int count = 0;
			
			if (this.ngrams.containsKey(ngram)) {
				count = this.ngrams.get(ngram);
			}
			
			this.ngrams.put(ngram, count + 1);
			this.tree.insert(ngram, 1);
			
			if (ngram.length() == 1) {
				this.unigrams.add(ngram);
			}
		}
	}
	
	/**
	 * Returns the count for the given n-gram
	 * @param ngram The n-gram
	 */
	public int getCount(NGram ngram) {
		return this.tree.find(ngram);
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
	 * Returns the probability of observing the given n-gram
	 * @param ngram The n-gram
	 * @param count The count of the given n-gram
	 */
	private double getProbability(NGram ngram, int count) {
		double d = 1.0;
		double alpha = 1.0 / ngram.length();

		if (ngram.length() == 1) {
			return (alpha * count) / this.unigrams.size();
		}
				
		NGram subgram = ngram.subgram(ngram.length() - 1);
		int subgramCount = this.getCount(subgram);

		if (count > this.matchThreshold) {
			return d * ((double)count / subgramCount);
		} else {
			return alpha * this.getProbability(subgram, subgramCount);
		}
	}
		
	/**
	 * Predicts the most probable (n+1)-gram for the given n-gram
	 * @param results The results
	 * @param ngram The current n-gram
	 */
	private void predictNext(List<Result> results, NGram ngram) {	
		for (NGram unigram : this.unigrams) {
			NGram predictedNgram = ngram.append(unigram);
			double probability = this.getProbability(predictedNgram, this.getCount(predictedNgram));

			if (probability > 0) {
				results.add(new Result(predictedNgram, probability));
			}
		}
	}
	
	/**
	 * Predicts the most probable (n+1)-gram for the given n-gram
	 * @param ngram The n-gram
	 * @param numResults The number of results
	 */
	public List<Result> predictNext(NGram ngram, int numResults) {
		List<Result> results = new ArrayList<Result>();
		this.predictNext(results, ngram);

		Collections.sort(results);
		
		for (int i = results.size() - 1; i >= numResults; i--) {
			results.remove(results.size() - 1);
		}
		
		return results; 
	}
}
