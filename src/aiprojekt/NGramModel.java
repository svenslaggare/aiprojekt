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
	//private final Set<NGram> unigrams = new HashSet<NGram>();
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
	private int totalUnigramCount = 0;
	
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

		if (ngram.length() == 1) { // Pretty stupid to iterate over all unigrams
			boolean contains = false; 
			for(int i = 0; i<topUnigrams.size();i++){
				if(topUnigrams.get(i).equals(ngram)){
					contains = true; 
					break;
				}
			}
			if(!contains){
				topUnigrams.add(ngram);			
			}
			
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
	 * Process the given NGrams Map, adding the NGrams one-by-one to the model
	 * @param tokens The NGrams Map
	 */
	public void processNGrams(Map<NGram, Integer> ngrams) {		
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
			}else if(ngram.length() == 1) { // Adding all unigrams
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
		
		for (NGram ngram : toRemove) {
			this.ngrams.remove(ngram);
		}
		// Removing all except for top 100
		while(topUnigrams.size()>100){
			topUnigrams.remove(topUnigrams.size()-1);
		}
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
	private double getProbability(NGram ngram, NGram unigram) {
		double d = 1.0;
		
		if (ngram.equals(NGram.EMPTY_GRAM)) {
			return d * (double)getCount(unigram) / this.topUnigrams.size();
		}
		
		NGram predictedNgram = ngram.append(unigram);
		
		int count = getCount(predictedNgram);
		if (count > matchThreshold) {
			return d * (double)count / getCount(ngram);
		} else {
			return getAlpha(ngram) * getProbability(ngram.rest(), unigram);
		}
	}
	
	private double getAlpha(NGram ngram) {
		double d = 0.5;
		int ngramCount = getCount(ngram);
		
		double beta = 1.0;
		double restSum = 0.0;
		
		for (NGram unigram : topUnigrams) {
			NGram predictedNgram = ngram.append(unigram);
			
			int count = getCount(predictedNgram);
			if (count > matchThreshold) {
				beta -= d * (double)count / ngramCount;
			} else {
				restSum += getProbability(ngram.rest(), unigram);
			}
		}
		
		return beta / restSum;
	}
	
	/**
	 * Predicts the most probable (n+1)-gram for the given n-gram
	 * @param ngram The n-gram
	 * @param numResults The number of results
	 */
	public List<Result> predictNext(NGram ngram, int numResults) {
		List<Result> results = new ArrayList<Result>();

		for (NGram unigram : this.topUnigrams) {
			if (unigram.equals(new NGram(new Token[]{ new Token(TokenType.START_OF_SENTENCE) }))
			|| unigram.equals(new NGram(new Token[]{ new Token(TokenType.END_OF_SENTENCE) }))) {
				continue;
			}
			
			double probability = this.getProbability(ngram, unigram);
			
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