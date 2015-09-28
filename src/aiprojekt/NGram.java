package aiprojekt;

import java.util.Arrays;

/**
 * Represents a N-gram
 */
public class NGram {
	private final Token[] tokens;
	
	/**
	 * Creates a new token
	 * @param tokens The tokens
	 */
	public NGram(Token[] tokens) {
		this(tokens, true);
	}
	
	/**
	 * Creates a new token
	 * @param tokens The tokens
	 * @param copyArray Indicates if the given array is copied
	 */
	public NGram(Token[] tokens, boolean copyArray) {
		if (!copyArray) {
			this.tokens = tokens;
		} else {
			this.tokens = Arrays.copyOf(tokens, tokens.length);
		}
	}

	/**
	 * Returns the length of the N-gram
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
