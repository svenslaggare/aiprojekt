package aiprojekt;

import java.util.Arrays;
import java.util.List;

/**
 * Represents a n-gram
 */
public class NGram {
	private final Token[] tokens;
	
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
		if (!copyArray) {
			this.tokens = tokens;
		} else {
			this.tokens = Arrays.copyOf(tokens, tokens.length);
		}
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
	 * Returns the length of the n-gram
	 */
	public int length() {
		return this.tokens.length;
	}

	/**
	 * Returns the token at the given index
	 * @param index The index
	 */
	public Token at(int index) {
		return this.tokens[index];
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
	 * Returns a n-gram with just the first token
	 */
	public NGram first() {
		if (this.length() == 0) {
			return EMPTY_GRAM;
		}
		
		return new NGram(new Token[] { this.tokens[0] }, false);
	}
	
	/**
	 * Returns a n-gram with everything except the first token
	 */
	public NGram rest() {
		if (this.length() == 0) {
			return EMPTY_GRAM;
		}
		
		Token[] tokens = new Token[this.length() - 1];
		
		for (int i = 1; i < this.length(); i++) {
			tokens[i - 1] = this.at(i);
		}
		
		return new NGram(tokens, false);
	}
	
	/**
	 * Appends the given n-gram to the current, returning a new one
	 * @param other The n-gram
	 * @return The new n-gram
	 */
	public NGram append(NGram other) {
		Token[] tokens = new Token[this.length() + other.length()];
		
		int i = 0;
		for (Token token : this.tokens) {
			tokens[i++] = token;
		}
		
		for (Token token : other.tokens) {
			tokens[i++] = token;
		}
		
		return new NGram(tokens, false);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(tokens);
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
		
		NGram other = (NGram) obj;
		if (!Arrays.equals(tokens, other.tokens)) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public String toString() {
		return Arrays.toString(this.tokens);
	}
}
