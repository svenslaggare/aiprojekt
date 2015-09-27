package aiprojekt;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

public class TextParserTest {
	String sentence1 = "Let's try this thing.";
	List<String> answer1 = new LinkedList<String>(Arrays.asList("let's", "try", "this", "thing"));
	
	String sentence2 = "[21:56] <HedgeMage> Deoovo: I haven't used thin clients much, I'm sorry.  I can answer general questions, though.";
	List<String> answer2 = new LinkedList<String>(Arrays.asList("deoovo", "i", "haven't", "used", "thin", "clients", "much", "i'm", "sorry", "i", "can", "answer", "general", "questions", "though"));
	
	@Test
	public void testTokenize() {
		TextParser parser = new TextParser();
		System.out.println(Arrays.toString(parser.tokenize(sentence1).toArray()));
		assertTrue(answer1.equals(parser.tokenize(sentence1)));
		
		System.out.println(Arrays.toString(parser.tokenize(sentence2).toArray()));
		assertTrue(answer2.equals(parser.tokenize(sentence2)));
	}

}
