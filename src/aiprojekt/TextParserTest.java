package aiprojekt;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

public class TextParserTest {
	String simpleSentence1 = "Let's try this thing.";
	List<String> answer1 = new LinkedList<String>(Arrays.asList("let's", "try", "this", "thing"));

	@Test
	public void testTokenize() {
		TextParser parser = new TextParser();
		assertTrue(answer1.equals(parser.tokenize(simpleSentence1)));
	}

}
