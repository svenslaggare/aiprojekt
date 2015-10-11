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
	private final int[] numNGrams;
	private final int[] totalNGramCounts;
	
	private final int matchThreshold = 0;
	private final int topUnigramsCount = 100;
	
	private final boolean isLoadMode;
		
	/**
	 * The start of sentence unigram
	 */
	public final static NGram START_OF_SENTENCE_UNIGRAM = NGram.fromTokens(
			new Token(TokenType.START_OF_SENTENCE));
	
	/**
	 * The end of sentence unigram
	 */
	public final static NGram END_OF_SENTENCE_UNIGRAM = NGram.fromTokens(
			new Token(TokenType.END_OF_SENTENCE));
	
	//Cached values when executing the getProbability method.
	private final Map<NGram, Double> probabilities = new HashMap<>();
	private final Map<NGram, Double> alphas = new HashMap<>();
	
	/**
	 * The default n-gram max length
	 */
	public static final int DEFAULT_MAX_NGRAM_LENGTH = 3;
	
	private final NGramTree tree = NGramTree.createRootTree();
	
	private final GoodTuringEstimation goodTuringEstimation = new GoodTuringEstimation();
	
	/**
	 * Creates a new N-gram model
	 * @param maxLength The maximum length of a n-gram
	 * @param isLoadMode Load mode means that the n-gram are loaded from a file
	 */
	public NGramModel(int maxLength, boolean isLoadMode) {
		this.maxLength = maxLength;
		this.numNGrams = new int[maxLength];
		this.totalNGramCounts = new int[maxLength];
		this.isLoadMode = isLoadMode;
	}
	
	/**
	 * Creates a new N-gram model
	 * @param maxLength The maximum length of a n-gram
	 */
	public NGramModel(int maxLength) {
		this(maxLength, false);
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
	 * Returns the number of n-gram of the given
	 * @param n The n-gram length
	 */
	public int numberOfNGramLength(int n) {
		if (n >= 1 && n <= this.maxLength) {
			return this.numNGrams[n - 1];
		} else {
			return 0;
		}
	}
	
	/**
	 * Returns the total count for the given n-gram length
	 * @param n The n-gram length
	 */
	public int totalCountForNGramLength(int n) {
		if (n >= 1 && n <= this.maxLength) {
			return this.totalNGramCounts[n - 1];
		} else {
			return 0;
		}
	}
	
	/**
	 * Returns the number of (unique) n-grams
	 */
	public int numNgrams() {
		int total = 0;
		
		for (int count : this.numNGrams) {
			total += count;
		}
		
		return total;
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
 	public void addNGram(NGram ngram, int count) {		
		if (!this.isLoadMode) {
			int currentCount = 0;
			
			if (this.ngrams.containsKey(ngram)) {
				currentCount = this.ngrams.get(ngram);
			} else {
				this.numNGrams[ngram.length() - 1]++;			
			}
			
			this.ngrams.put(ngram, currentCount + count);
		} else {
			if (this.tree.find(ngram) == 0) {
				this.numNGrams[ngram.length() - 1]++;	
			}
		}
		
		this.tree.insert(ngram, count);
		this.totalNGramCounts[ngram.length() - 1] += count;
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
				this.numNGrams[ngram.length() - 1]--;
			} else if (ngram.length() == 1) { // Adding all unigrams
				if (!ngram.equals(START_OF_SENTENCE_UNIGRAM)
					&& !ngram.equals(END_OF_SENTENCE_UNIGRAM)) {
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
		return this.tree.find(ngram);
		
//		if (this.ngrams.containsKey(ngram)) {
//			return this.ngrams.get(ngram);
//		}
//		
//		return 0;
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
	 * Cleares the caches
	 */
	public void clearCache() {
		this.probabilities.clear();
		this.alphas.clear();
	}
		
	/**
	 * Returns the possible unigrams for the given n-gram
	 * @param ngram The n-gram
	 * @param includeTopUnigrams Indicates if to include the top unigrams
	 */
	private Set<NGram> getPossibleUnigrams(NGram ngram, boolean includeTopUnigrams) {
		NGram lastNgram = ngram.last();
		
		Set<NGram> possibleNGrams = new HashSet<>();
		if (includeTopUnigrams) {
			for (NGram unigram : this.topUnigrams) {
				if (unigram.equals(lastNgram)) {
					continue;
				}
				
				possibleNGrams.add(unigram);
			}
		}
		
		for (NGramTree.Result result : this.tree.findResults(ngram)) {
			NGram word = result.getNgram().last();
			if (!(word.equals(START_OF_SENTENCE_UNIGRAM)
				  || word.equals(END_OF_SENTENCE_UNIGRAM))) {
				possibleNGrams.add(word);
			}
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
	 * Returns the probability of observing the given unigram given a n-gram
	 * @param ngram The n-gram
	 * @param unigram The unigram
	 */
	public double getProbability(NGram ngram, NGram unigram) {		
		if (ngram.equals(NGram.EMPTY_GRAM)) {	
			if (this.probabilities.containsKey(unigram)) {
				return this.probabilities.get(unigram);
			}
			
			int count = getCount(unigram);
						
			if (count > 0) {
				double d = this.goodTuringEstimation.estimate(count) / count;		
				return this.addProbability(unigram, d * (double)count / this.totalCountForNGramLength(1));
			} else {				
				return this.addProbability(unigram, this.goodTuringEstimation.estimate(0));
			}
		}
		
		NGram predictedNgram = ngram.append(unigram);
		if (this.probabilities.containsKey(predictedNgram)) {
			return this.probabilities.get(predictedNgram);
		}
		
		int count = getCount(predictedNgram);
		if (count > this.matchThreshold) {							
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
		
		for (NGram unigram : this.getPossibleUnigrams(ngram, true)) {
			NGram predictedNgram = ngram.append(unigram);
			
			int count = getCount(predictedNgram);
			if (count > this.matchThreshold) {
				double d = this.goodTuringEstimation.estimate(count) / count;
				beta -= d * (double)count / ngramCount;
			} else {
				restSum += getProbability(ngram.rest(), unigram);
			}
		}
					
		double alpha = beta / restSum;	
		
		//Current implementation is buggy
		if (restSum == 0 || beta == 0.0) {
			alpha = 1E-6;
		}
		
		if (alpha > 1) {
			alpha = 1.0;
		}
				
		this.alphas.put(ngram, alpha);
		return alpha;
	}
	
	/**
	 * Predicts the most probable (n+1)-gram for the given n-gram
	 * @param ngram The n-gram
	 * @param numResults The number of results
	 */
	public List<Result> predictNext(NGram ngram, int numResults) {
//		this.clearCache();
		List<Result> results = new ArrayList<Result>();
		
		for (NGram unigram : this.getPossibleUnigrams(ngram, true)) {
			if (unigram.equals(START_OF_SENTENCE_UNIGRAM) || unigram.equals(END_OF_SENTENCE_UNIGRAM)) {
				continue;
			}
			
			double probability = this.getProbability(ngram, unigram);
			

			if (probability < 0 
				|| probability > 1
				|| Double.isNaN(probability)
				|| Double.isInfinite(probability)) {
				System.err.println("Invalid probability: " +ngram+" " + unigram + ": " + probability);
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
