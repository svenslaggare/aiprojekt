package aiprojekt;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a text parser
 */
public class TextParser {
	//private static final String REPLACE_ALL_PATTERN = "^\\[[\\d]+:[\\d]+\\] <[\\w]+> |\\.|,|:|;|=";
	// matches the form [19:32]
	private static final String MATCH_TIMESTAMP = "^\\[[\\d]+:[\\d]+\\]";
	// matches the form <svenslaggare>
	private static final String MATCH_USERNAME = "<[\\w]+>";
	private static final String MATCH_TIME_AND_USER = MATCH_TIMESTAMP+" "+MATCH_USERNAME+" ";
	// matches everything but letters a-z - ' (whitespace)
	private static final String MATCH_UNWANTED_CHARS = "[^a-zA-Z\\-\\' ]";
	private static final String MATCH_URL = "((https?|ftp|file|http):(//)+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
	private static final String MATCH_URL2="www\\.[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*";
	private static final String REPLACE_ALL_PATTERN = MATCH_TIME_AND_USER+"|" + MATCH_URL + "|"+ MATCH_URL2 + "|"+MATCH_UNWANTED_CHARS;
	//private static final String REPLACE_ALL_PATTERN = "^\\[[\\d]+:[\\d]+\\] <[\\w]+> |[^a-zA-Z\\-\\' ]";
	
	public List<Token> tokenize(String text) {
		text = text.replaceAll(REPLACE_ALL_PATTERN, "");
		text = text.toLowerCase();
		String[] tokens = text.split("\\s+");
		ArrayList<Token> tokenList = new ArrayList<Token>();
		tokenList.add(new Token(TokenType.START_OF_SENTENCE));
		
		for(int i=0; i<tokens.length; i++){
			if(tokens[i] != null && !tokens[i].isEmpty()){
				tokenList.add(new Token(tokens[i]));
			}
		}
		tokenList.add(new Token(TokenType.END_OF_SENTENCE));
		return tokenList;
		
	}
}
