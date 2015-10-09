package aiprojekt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
	private final List<NGram> topUnigrams = new ArrayList<NGram>();
	private final int[] ngramCounts;
	private int totalUnigramCount = 0;
	
	private final int matchThreshold = 0;
	private final int topUnigramsCount = 100;
	
	private final static NGram START_OF_SENTENCE_UNIGRAM = NGram.fromTokens(new Token(TokenType.START_OF_SENTENCE));
	private final static NGram END_OF_SENTENCE_UNIGRAM = NGram.fromTokens(new Token(TokenType.END_OF_SENTENCE));
	
	//Cached values when executing the getProbability method. This might needed to be cleared if the model changes.
	private final Map<NGram, Double> probabilities = new HashMap<>();
	private final Map<NGram, Double> alphas = new HashMap<>();
	
	/**
	 * The default n-gram max length
	 */
	public static final int DEFAULT_MAX_NGRAM_LENGTH = 3;
	
	private final NGramTree tree = NGramTree.rootTree();
	
	private final GoodTuringEstimation goodTuringEstimation = new GoodTuringEstimation();
	
	/**
	 * Creates a new N-gram model
	 * @param maxLength The maximum length of a n-gram
	 */
	public NGramModel(int maxLength) {
		this.maxLength = maxLength;
		this.ngramCounts = new int[maxLength];
	}
	
	/**
	 * Returns the Good-Turing estimator
	 */
	public GoodTuringEstimation getGoodTuringEstimation() {
		return this.goodTuringEstimation;
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
		
		for (int i = 0; i < tokens.size(); i++) {
			List<Token> ngram = new ArrayList<Token>();
			
			//Creates the unigram, bigrams, trigrams, ...
			for (int j = i; j < Math.min(i + maxLength, tokens.size()); j++) {
				ngram.add(tokens.get(j));
				ngrams.add(NGram.fromList(ngram));
			}
		}
		
		return ngrams;
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
		
		if (ngram.length() == 1) {
			this.totalUnigramCount++;
		}
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
		
		List<NGram> toRemove = new ArrayList<NGram>();
		
		for (Map.Entry<NGram, Integer> current : this.ngrams.entrySet()) {
			NGram ngram = current.getKey();
			if (ngram.length() > 1 && current.getValue() <= threshold) {
				toRemove.add(ngram);
				this.ngramCounts[ngram.length() - 1]--;
			} else if (ngram.length() == 1) { // Adding all unigrams
				if (!ngram.toString().equals(START_OF_SENTENCE_UNIGRAM)
					&& !ngram.toString().equals(END_OF_SENTENCE_UNIGRAM)) {
					this.topUnigrams.add(ngram);			
				}
				
				this.goodTuringEstimation.addObservation(current.getValue());
			}
		}

		// Sort the unigrams by count
		Collections.sort(topUnigrams, new Comparator<NGram>() {
			public int compare(NGram a, NGram b) {
				return Integer.compare(getCount(b), getCount(a));
			}
		});
		
		for (NGram ngram : toRemove) {
			this.ngrams.remove(ngram);
		}
		
		// Removing all except for top
		while (this.topUnigrams.size() > this.topUnigramsCount) {
			this.topUnigrams.remove(topUnigrams.size() - 1);
		}
		
		this.goodTuringEstimation.fitToData();
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
	 * Returns the possible unigrams for the given n-gram
	 * @param ngram The n-gram
	 */
	private Set<NGram> getPossibleUnigrams(NGram ngram) {
		Set<NGram> possibleNGrams = new HashSet<>();
		for (NGram unigram : this.topUnigrams) {
			possibleNGrams.add(unigram);
		}
		
		for (NGramTree.Result result : this.tree.findResults(ngram)) {
			possibleNGrams.add(result.getNgram().last());
		}
		
		return possibleNGrams;
	}
	
	/**
	 * Adds the given n-gram probability to the cache
	 * @param ngram The n-gram
	 * @param probability The probability of the n-gram
	 * @return The probability
	 */
	private double addProbability(NGram ngram, double probability) {
		this.probabilities.put(ngram, probability);
		return probability;
	}
	
	/**
	 * Returns the probability of observing the given n-gram
	 * @param ngram The n-gram
	 * @param count The count of the given n-gram
	 */
	private double getProbability(NGram ngram, NGram unigram) {		
		if (this.probabilities.containsKey(unigram)) {
			return this.probabilities.get(unigram);
		}
		
		if (ngram.equals(NGram.EMPTY_GRAM)) {		
			int count = getCount(unigram);
			double d = this.goodTuringEstimation.estimate(count) / count;			
			return this.addProbability(unigram, d * (double)getCount(unigram) / this.totalUnigramCount);
		}
		
		NGram predictedNgram = ngram.append(unigram);
		
		int count = getCount(predictedNgram);
		if (count > matchThreshold) {	
			double d = this.goodTuringEstimation.estimate(count) / count;
			return this.addProbability(predictedNgram, d * (double)count / getCount(ngram));
		} else {
			return this.addProbability(predictedNgram, getAlpha(ngram) * getProbability(ngram.rest(), unigram));
		}
	}
	
	/**
	 * Returns the alpha value for the given n-gram
	 * @param ngram The n-gram
	 */
	private double getAlpha(NGram ngram) {
		if (this.alphas.containsKey(ngram)) {
			return this.alphas.get(ngram);
		}
		
		int ngramCount = getCount(ngram);
		
		double beta = 1.0;
		double restSum = 0.0;
		
		for (NGram unigram : this.getPossibleUnigrams(ngram)) {
			NGram predictedNgram = ngram.append(unigram);
			
			int count = getCount(predictedNgram);
			if (count > matchThreshold) {
				double d = this.goodTuringEstimation.estimate(count) / count;
				beta -= d * (double)count / ngramCount;
			} else {
				restSum += getProbability(ngram.rest(), unigram);
			}
		}
					
		double alpha = beta / restSum;		
		this.alphas.put(ngram, alpha);
		return alpha;
	}
	
	/**
	 * Predicts the most probable (n+1)-gram for the given n-gram
	 * @param ngram The n-gram
	 * @param numResults The number of results
	 */
	public List<Result> predictNext(NGram ngram, int numResults) {
		List<Result> results = new ArrayList<Result>();
		
		for (NGram unigram : this.getPossibleUnigrams(ngram)) {
			if (unigram.equals(START_OF_SENTENCE_UNIGRAM) || unigram.equals(END_OF_SENTENCE_UNIGRAM)) {
				continue;
			}
			
			double probability = this.getProbability(ngram, unigram);

			if (probability < 0 || probability > 1 || Double.isNaN(probability)) {
				System.err.println("Invalid probability: " + unigram + ": " + probability);
			}
						
			if (probability > 0) {
				results.add(new Result(ngram.append(unigram), probability));
			}
		}

		Collections.sort(results);
		
		for (int i = results.size() - 1; i >= numResults; i--) {
			results.remove(results.size() - 1);
		}
		
		return results; 
	}
}
