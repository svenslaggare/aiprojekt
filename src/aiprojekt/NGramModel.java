package aiprojekt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents N-gram model
 */
public class NGramModel {
	private final Map<NGram, Long> nGrams = new HashMap<NGram, Long>();
	
	/**
	 * Creates a new N-gram model
	 */
	public NGramModel() {
		
	}
	
	/**
	 * Returns the N-grams in the given tokens
	 * @param tokens The tokens
	 * @param maxLength The maximum length of a N-gram
	 * @return The n-grams
	 */
	public static List<NGram> getNGrams(List<Token> tokens, int maxLength) {
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
}
