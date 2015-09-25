package aiprojekt;

import java.util.Arrays;
import java.util.List;

public class TextParser {

	private static final String REPLACE_ALL_PATTERN = "\\.";
	
	public List<String> tokenize(String text) {
		text = text.replaceAll(REPLACE_ALL_PATTERN, "");
		text = text.toLowerCase();
		String[] tokens = text.split("\\s+");
		return Arrays.asList(tokens);
		
	}
	
	
}
