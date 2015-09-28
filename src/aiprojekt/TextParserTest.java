package aiprojekt;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;


import aiprojekt.TextParser;
import aiprojekt.Token;
import aiprojekt.TokenType;

/**
 * Tests the text parser
 */
public class TextParserTest {
	String sentence1 = "Let's try this thing.";
	ArrayList<Token> answer1 = new ArrayList<Token>(Arrays.asList(new Token(
			TokenType.START_OF_SENTENCE), new Token("let's"), new Token("try"),
			new Token("this"), new Token("thing"), new Token(
					TokenType.END_OF_SENTENCE)));

	String sentence2 = "[21:56] <HedgeMage> Deoovo: I haven't used thin clients much, I'm sorry.  I can answer general questions, though.";
	ArrayList<Token> answer2 = new ArrayList<Token>(Arrays.asList(new Token(
			TokenType.START_OF_SENTENCE), new Token("deoovo"), new Token("i"),
			new Token("haven't"), new Token("used"), new Token("thin"),
			new Token("clients"), new Token("much"), new Token("i'm"),
			new Token("sorry"), new Token("i"), new Token("can"), new Token(
					"answer"), new Token("general"), new Token("questions"),
			new Token("though"), new Token(TokenType.END_OF_SENTENCE)));

	String sentence3 = "[21:56] <Ugglan>     testing testing,  is   it    working??!";
	ArrayList<Token> answer3 = new ArrayList<Token>(Arrays.asList(new Token(
			TokenType.START_OF_SENTENCE), new Token(
			"testing"), new Token("testing"), new Token("is"), new Token("it"),
			new Token("working"), new Token(TokenType.END_OF_SENTENCE)));

	@Test
	public void testTokenize() {
		TextParser parser = new TextParser();
		assertTrue(answer1.equals(parser.tokenize(sentence1)));
		assertTrue(answer2.equals(parser.tokenize(sentence2)));
		assertTrue(answer3.equals(parser.tokenize(sentence3)));
	}

}
