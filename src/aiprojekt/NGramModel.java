package aiprojekt;

import java.util.ArrayList;
import java.util.Arrays;
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
	private List<NGram> topUnigrams =  new ArrayList<NGram>(); 
	private final String BEGINNING_UNIGRAM = "<s>";
	private final String END_UNIGRAM = "</s>";
	private final int[] ngramCounts;
	
	private final int matchThreshold = 0;
	
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
	 * Returns the unigrams
	 */
	public List<NGram> unigrams() {
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

	/*	if (ngram.length() == 1) {
			this.unigrams.add(ngram);
		}*/
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
			}else if(ngram.length() == 1){ // Adding all unigrams
				if(!ngram.toString().equals(BEGINNING_UNIGRAM) && !ngram.toString().equals(END_UNIGRAM) ){
					topUnigrams.add(ngram);			
				}else{
					System.out.println(ngram.toString());
				}
			}
		}
		
		for (NGram ngram : toRemove) {
			this.ngrams.remove(ngram);
		}
		// Sort the unigrams by count
		Collections.sort(topUnigrams, new Comparator<NGram>(){
			public int compare(NGram a, NGram b){
				return Integer.compare(getCount(b), getCount(a));
			}
		});
		// Removing all except for top 100
		while(topUnigrams.size()>100){
			topUnigrams.remove(topUnigrams.size()-1);
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
