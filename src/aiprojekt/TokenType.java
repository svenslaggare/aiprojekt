package aiprojekt;

/**
 * The token types
 */
public enum TokenType {
	WORD("Word"),
	START_OF_SENTENCE("<s>"),
	END_OF_SENTENCE("</s>");
	
	private String str;
	TokenType(String str) {
		this.str = str;
	}
	
	@Override
	public String toString() {
		return this.str;
	}
}
