package aiprojekt;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a text parser
 */
public class TextParser {
	// matches the form [19:32]
	private static final String MATCH_TIMESTAMP = "^\\[[\\d]+:[\\d]+\\]";
	
	// matches the form <svenslaggare>
	private static final String MATCH_USERNAME = "<([\\w]+)>";
	private static final String MATCH_TIME_AND_USER = MATCH_TIMESTAMP + " " + MATCH_USERNAME + " ";
	
	// matches everything but letters a-z - ' (whitespace)
	private static final String MATCH_UNWANTED_CHARS = "[^a-zA-Z\\-\\' ]";
	private static final String MATCH_URL = "((https?|ftp|file|http):(//)+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
	private static final String MATCH_URL2 = "www\\.[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*";
	
	private static final Pattern USERNAME_PATTERN = Pattern.compile(MATCH_USERNAME);
	private static final Pattern REPLACE_ALL_PATTERN = Pattern.compile(MATCH_TIME_AND_USER + "|" + MATCH_URL + "|"+ MATCH_URL2 + "|"+ MATCH_UNWANTED_CHARS);
	private static final Pattern WORD_PATTERN = Pattern.compile("\\s+");
	private static final Pattern SYSTEM_MESSAGE_PATTERN = Pattern.compile("^(===).*");
	
	private final Set<String> userNames = new HashSet<>();
	
	/**
	 * Tokenizes the given text
	 * @param text The text
	 */
	public List<Token> tokenize(String text) {
		if (SYSTEM_MESSAGE_PATTERN.matcher(text).matches()) {
			return new ArrayList<Token>();
		}
		
		String userName = this.getUser(text);
		if (userName != "") {
			userNames.add(userName.toLowerCase());
		}
		
		text = REPLACE_ALL_PATTERN.matcher(text).replaceAll("");
		
		text = text.toLowerCase();
		String[] tokens = WORD_PATTERN.split(text);
		
		List<Token> tokenList = new ArrayList<Token>();
		tokenList.add(new Token(TokenType.START_OF_SENTENCE));
		
		for (int i = 0; i < tokens.length; i++) {
			String token = tokens[i];
			if (token != null
				&& !token.isEmpty()
				&& !this.userNames.contains(token)) {
				tokenList.add(new Token(tokens[i]));
			}
		}
		
		tokenList.add(new Token(TokenType.END_OF_SENTENCE));
		
		return tokenList;	
	}
	
	/**
	 * Used for extracting the user name from the sentence.
	 * A user is considered the first occurrence of <name>.
	 * @param sentence The sentence from the chatlog
	 * @return The extracted user name, or "" if not found.
	 */
	public String getUser(String sentence){
		if (SYSTEM_MESSAGE_PATTERN.matcher(sentence).matches()) {
			return "";
		}
		
		Matcher matcher = USERNAME_PATTERN.matcher(sentence);
		if (matcher.find()) {
			String user = matcher.group(1);
			if(user.equals("ubotu") || user.equals("ubottu")){
				return "";
			}
			return user;
		} else {
			return "";
		}
	}
}
