package aiprojekt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Represents n-gram model
 */
public class NGramModel {
	private final int maxLength;
	
	private final Map<NGram, Integer> ngrams = new HashMap<NGram, Integer>();
	private final List<NGram> topUnigrams =  new ArrayList<NGram>(); 
	private final int[] ngramCounts;
	
	private final int matchThreshold = 0;
	private final int topUnigramsCount = 100;
	
	private final static NGram BEGINNING_UNIGRAM = NGram.fromTokens(new Token(TokenType.START_OF_SENTENCE));
	private final static NGram END_UNIGRAM = NGram.fromTokens(new Token(TokenType.END_OF_SENTENCE));
	
	/**
	 * The default n-gram max length
	 */
	public static final int DEFAULT_MAX_NGRAM_LENGTH = 3;
	
	private final NGramTree tree = NGramTree.rootTree();
	
	/**
	 * Creates a new N-gram model
	 * @param maxLength The maximum length of a n-gram
	 */
	public NGramModel(int maxLength) {
		this.maxLength = maxLength;
		this.ngramCounts = new int[maxLength];
	}
	
	/**
	 * Returns the maximum length of a n-gram
	 */
	public int maxLength() {
		return this.maxLength;
	}
	
	/**
	 * Returns the top ranked unigrams
	 */
	public List<NGram> topUnigrams() {
		return this.topUnigrams;
	}
	
	/**
	 * Returns the n-grams
	 */
	public Map<NGram, Integer> getNgrams() {
		return this.ngrams;
	}
	
	/**
	 * Returns the count for the given n-gram length
	 * @param n The n-gram length
	 * @return
	 */
	public int countForNGram(int n) {
		if (n >= 1 && n <= this.maxLength) {
			return this.ngramCounts[n - 1];
		} else {
			return 0;
		}
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
		List<NGram> ngrams = new ArrayList<NGram>();
		getNgrams(tokens, maxLength, ngrams);
		return ngrams;
	}
 	
 	/**
	 * Returns the n-grams in the given tokens
	 * @param tokens The tokens
	 * @param maxLength The maximum length of a n-gram
	 * @param outList The output list
	 */
 	public static void getNgrams(List<Token> tokens, int maxLength, List<NGram> outList) {
		List<Token> ngram = new ArrayList<Token>();
		
		for (int i = 0; i < tokens.size(); i++) {			
			//Creates the unigram, bigrams, trigrams, ...
			for (int j = i; j < Math.min(i + maxLength, tokens.size()); j++) {
				ngram.add(tokens.get(j));
				outList.add(NGram.fromList(ngram));
			}
			
			ngram.clear();
		}
	}
	
 	/**
 	 * Adds the given n-gram to the model
 	 * @param ngram The n-gram
 	 * @param count The count
 	 */
 	private void addNGram(NGram ngram, int count) {
		int currentCount = 0;
		
		if (this.ngrams.containsKey(ngram)) {
			currentCount = this.ngrams.get(ngram);
		} else {
			this.ngramCounts[ngram.length() - 1]++;			
		}
		
		this.ngrams.put(ngram, currentCount + count);
		this.tree.insert(ngram, count);
 	}
 	
	/**
	 * Process the given tokens, adding them to the model
	 * @param tokens The tokens
	 */
	public void processTokens(List<Token> tokens) {
		for (NGram ngram : getNgrams(tokens, this.maxLength)) {
			this.addNGram(ngram, 1);
		}	
	}
	
	/**
	 * Adds the given n-gram map (n-gram to count) to the model
	 * @param ngrams The n-gram map
	 */
	public void addNGrams(Map<NGram, Integer> ngrams) {		
		for (Map.Entry<NGram, Integer> entry : ngrams.entrySet()) {
			NGram ngram = entry.getKey();
			int count = entry.getValue();
			this.addNGram(ngram, count);
		}
	}
	
	/**
	 * Should be called after all tokens has been processed.
	 */
	public void end() {		
		int threshold = 1;
		
		Iterator<Map.Entry<NGram, Integer>> entryIterator = this.ngrams.entrySet().iterator();
		while (entryIterator.hasNext()) {
			Map.Entry<NGram, Integer> current = entryIterator.next();
			NGram ngram = current.getKey();
			//Remove unique n-grams of length > 1
			if (ngram.length() > 1 && current.getValue() <= threshold) {
				entryIterator.remove();
				this.ngramCounts[ngram.length() - 1]--;
			} else if(ngram.length() == 1) { // Adding all unigrams
				if (!ngram.toString().equals(BEGINNING_UNIGRAM) && !ngram.toString().equals(END_UNIGRAM)) {
					topUnigrams.add(ngram);			
				}
			}
		}
				
		// Sort the unigrams by count
		Collections.sort(topUnigrams, new Comparator<NGram>() {
			public int compare(NGram a, NGram b) {
				return Integer.compare(getCount(b), getCount(a));
			}
		});
		
		// Removing all except for top
		while (topUnigrams.size() > this.topUnigramsCount) {
			topUnigrams.remove(topUnigrams.size() - 1);
		}
		//System.out.println("num of unigrams: " + topUnigrams.size() + "first: " + getCount(topUnigrams.get(0)) + " last: " + getCount(topUnigrams.get(topUnigrams.size()-1)));	
	}
	
	/**
	 * Returns the count for the given n-gram
	 * @param ngram The n-gram
	 */
	public int getCount(NGram ngram) {
//		return this.tree.find(ngram);
		
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
	 * Returns the probability of observing the given n-gram
	 * @param ngram The n-gram
	 * @param count The count of the given n-gram
	 */
	private double getProbability(NGram ngram, int count) {
		double d = 1.0;
		double alpha = 0.5 / ngram.length();

		if (ngram.length() == 1) {
			return (alpha * count) / this.topUnigrams.size();
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
		for (NGram unigram : this.topUnigrams) {
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
