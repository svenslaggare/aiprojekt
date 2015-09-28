package aiprojekt;

/**
 * Represents a token
 */
public class Token {
	private final TokenType type;
	private final String word;
	
	/**
	 * Creates a new word token
	 * @param word The word
	 */
	public Token(String word) {
		this.word = word;
		this.type = TokenType.WORD;
	}
	
	/**
	 * Creates a new token of the given type
	 * @param type The type
	 */
	public Token(TokenType type) {
		this.type = type;
		this.word = "";
	}

	/**
	 * Returns the type of the token
	 */
	public TokenType getType() {
		return type;
	}

	/**
	 * Returns the word
	 */
	public String getWord() {
		return word;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((word == null) ? 0 : word.hashCode());
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
		
		if (!(obj instanceof Token)) {
			return false;
		}
		
		Token other = (Token) obj;
		if (type != other.type) {
			return false;
		}
		
		if (word == null) {
			if (other.word != null) {
				return false;
			}
		} else if (!word.equals(other.word)) {
			return false;
		}
		
		return true;
	}	
	
	@Override
	public String toString() {
		switch (this.type) {
		case WORD:
			return this.word;
		case START_OF_SENTENCE:
			return "<s>";
		case END_OF_SENTENCE:
			return "</s>";
		}
		
		return "";
	}
}
