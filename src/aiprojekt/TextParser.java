package aiprojekt;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Represents a text parser
 */
public class TextParser {
	// matches the form [19:32]
	private static final String MATCH_TIMESTAMP = "^\\[[\\d]+:[\\d]+\\]";
	
	// matches the form <svenslaggare>
	private static final String MATCH_USERNAME = "<[\\w]+>";
	private static final String MATCH_TIME_AND_USER = MATCH_TIMESTAMP + " " + MATCH_USERNAME + " ";
	
	// matches everything but letters a-z - ' (whitespace)
	private static final String MATCH_UNWANTED_CHARS = "[^a-zA-Z\\-\\' ]";
	private static final String MATCH_URL = "((https?|ftp|file|http):(//)+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
	private static final String MATCH_URL2 = "www\\.[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*";
	
	private static final Pattern REPLACE_ALL_PATTERN = Pattern.compile(MATCH_TIME_AND_USER + "|" + MATCH_URL + "|"+ MATCH_URL2 + "|"+ MATCH_UNWANTED_CHARS);
	private static final Pattern WORD_PATTERN = Pattern.compile("\\s+");
	private static final Pattern SYSTEM_MESSAGE_PATTERN = Pattern.compile("^(===).*");
	
	/**
	 * Tokenizes the given text
	 * @param text The text
	 */
	public List<Token> tokenize(String text) {
		if (SYSTEM_MESSAGE_PATTERN.matcher(text).matches()) {
			return new ArrayList<Token>();
		}
		
		text = REPLACE_ALL_PATTERN.matcher(text).replaceAll("");
		
		text = text.toLowerCase();
		String[] tokens = WORD_PATTERN.split(text);
		
		List<Token> tokenList = new ArrayList<Token>();
		tokenList.add(new Token(TokenType.START_OF_SENTENCE));
		
		for (int i=0; i < tokens.length; i++) {
			if (tokens[i] != null && !tokens[i].isEmpty()) {
				tokenList.add(new Token(tokens[i]));
			}
		}
		
		tokenList.add(new Token(TokenType.END_OF_SENTENCE));
		return tokenList;	
	}
}
