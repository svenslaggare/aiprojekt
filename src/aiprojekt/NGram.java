package aiprojekt;

import java.util.Arrays;
import java.util.List;

/**
 * Represents a n-gram
 */
public class NGram {
	private final Token[] tokens;
	private final int start;
	private final int length;
	
	/**
	 * Represents an empty n-gram
	 */
	public static final NGram EMPTY_GRAM = new NGram(new Token[0], false);
	
	/**
	 * Creates a new n-gram
	 * @param tokens The tokens
	 */
	public NGram(Token[] tokens) {
		this(tokens, true);
	}
	
	/**
	 * Creates a new n-gram
	 * @param tokens The tokens
	 * @param copyArray Indicates if the given array is copied
	 */
	private NGram(Token[] tokens, boolean copyArray) {
		this.start = 0;
		this.length = tokens.length;
		
		if (!copyArray) {
			this.tokens = tokens;
		} else {
			this.tokens = Arrays.copyOf(tokens, tokens.length);
		}
	}

	/**
	 * Creates a new n-gram
	 * @param tokens The underlying tokens array
	 * @param start The start of the n-gram in the array
	 * @param length The length of the n-gram
	 */
	private NGram(Token[] tokens, int start, int length) {
		this.tokens = tokens;
		this.start = start;
		this.length = length;
	}
	
	/**
	 * Creates a new n-gram from the given list of tokens
	 * @param tokens The tokens
	 * @return
	 */
	public static NGram fromList(List<Token> tokens) {
		return new NGram(tokens.toArray(new Token[tokens.size()]), false);
	}
	
	/**
	 * Creates a n-gram with the given words
	 * @param first The first word
	 * @param others The other words
	 */
	public static NGram fromWords(String first, String... others) {
		Token[] tokens = new Token[others.length + 1];
		tokens[0] = new Token(first);
		
		for (int i = 0; i < others.length; i++) {
			tokens[i + 1] = new Token(others[i]);
		}
		
		return new NGram(tokens, false);
	}
	
	
	/**
	 * Creates a n-gram with the given tokens
	 * @param first The first token
	 * @param others The other token
	 */
	public static NGram fromTokens(Token first, Token... others) {
		Token[] tokens = new Token[others.length + 1];
		tokens[0] = first;
		
		for (int i = 0; i < others.length; i++) {
			tokens[i + 1] = others[i];
		}
		
		return new NGram(tokens, false);
	}
	/**
	 * Returns the length of the n-gram
	 */
	public int length() {
		return this.length;
	}

	/**
	 * Returns the token at the given index
	 * @param index The index
	 */
	public Token at(int index) {
		return this.tokens[this.start + index];
	}
	
	/**
	 * Indicates if the current n-gram starts with the given
	 * @param other The other n-gram
	 * @param Indicates if the n-grams can be equal
	 */
	public boolean startsWith(NGram other, boolean canBeEqual) {
		if (other.length() > this.length()) {
			return false;
		}
		
		if (!canBeEqual && other.length() == this.length()) {
			return false;
		}
		
		for (int i = 0; i < other.length(); i++) {
			if (!this.at(i).equals(other.at(i))) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Indicates if the current n-gram starts with the given
	 * @param other The other n-gram
	 */
	public boolean startsWith(NGram other) {
		return startsWith(other, true);
	}
	
	/**
	 * Returns a n-gram with the given length
	 * @param length The length
	 * @return The subgram
	 */
	public NGram subgram(int length) {
		if (length > this.length()) {
			throw new IllegalArgumentException("length > length of n-gram");
		}
		
		Token[] tokens = new Token[length];
		
		for (int i = 0; i < length; i++) {
			tokens[i] = this.at(i);
		}
		
		return new NGram(tokens, false);
	}
	
	/**
	 * Returns a unigram with just the first token
	 */
	public NGram first() {
		if (this.length() == 0) {
			return EMPTY_GRAM;
		}
		
		return new NGram(this.tokens, this.start, 1);
	}
	
	/**
	 * Returns a (n-1)-gram with everything except the first token
	 */
	public NGram rest() {
		if (this.length() == 0) {
			return EMPTY_GRAM;
		}
		
		return new NGram(this.tokens, this.start + 1, this.length - 1);
	}
	
	/**
	 * Appends the given n-gram to the current, returning a new one
	 * @param other The n-gram
	 * @return The new n-gram
	 */
	public NGram append(NGram other) {
		Token[] tokens = new Token[this.length() + other.length()];
		
		int i = 0;

		for (int j = 0; j < this.length; j++) {
			tokens[i++] = this.at(j);
		}
		
		for (int j = 0; j < other.length; j++) {
			tokens[i++] = other.at(j);
		}
		
		return new NGram(tokens, false);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Helpers.arrayHashCode(this.tokens, this.start, this.length); 
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		
		if (obj == null) {
			return false;
		}
		
		if (!(obj instanceof NGram)) {
			return false;
		}
		
		NGram other = (NGram)obj;		
		if (!Helpers.arrayEquals(this.tokens, this.start, this.length, other.tokens, other.start, other.length)) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		
		str.append("[");
		for (int i = 0; i < this.length; i++) {
			if (i != 0) {
				str.append(", ");
			}
			
			str.append(this.at(i));
		}
		str.append("]");
		
		return str.toString();
	}
}
