package aiprojekt.tests;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import aiprojekt.TextParser;
import aiprojekt.Token;
import aiprojekt.TokenType;

/**
 * Tests the text parser
 */
public class TextParserTest {
	// simple tokenize
	private final String sentence1 = "Let's try this thing.";
	private final ArrayList<Token> answer1 = new ArrayList<Token>(Arrays.asList(
					new Token(TokenType.START_OF_SENTENCE),
					new Token("let's"),
					new Token("try"),
					new Token("this"),
					new Token("thing"),
					new Token(TokenType.END_OF_SENTENCE)));

	// sample sentence
	private final String sentence2 = "[21:56] <HedgeMage> Deoovo: I haven't used thin clients much, I'm sorry.  I can answer general questions, though.";
	private final ArrayList<Token> answer2 = new ArrayList<Token>(Arrays.asList(
			new Token(TokenType.START_OF_SENTENCE),
			new Token("deoovo"),
			new Token("i"),
			new Token("haven't"),
			new Token("used"),
			new Token("thin"),
			new Token("clients"),
			new Token("much"),
			new Token("i'm"),
			new Token("sorry"),
			new Token("i"),
			new Token("can"),
			new Token("answer"),
			new Token("general"),
			new Token("questions"),
			new Token("though"),
			new Token(TokenType.END_OF_SENTENCE)));
	
	// sample sentence
	private final String sentence3 = "[21:56] <Ugglan>     testing testing,  is   it    working??!";
	private final ArrayList<Token> answer3 = new ArrayList<Token>(Arrays.asList(
			new Token(TokenType.START_OF_SENTENCE),
			new Token("testing"),
			new Token("testing"),
			new Token("is"),
			new Token("it"),
			new Token("working"),
			new Token(TokenType.END_OF_SENTENCE)));
	
	// URLs
	private final String sentence4 = "[21:56] <svEnSlaGGare> you should search: http://www.google.com";
	private final ArrayList<Token> answer4 = new ArrayList<Token>(Arrays.asList(
			new Token(TokenType.START_OF_SENTENCE),
			new Token("you"),
			new Token("should"),
			new Token("search"),
			new Token(TokenType.END_OF_SENTENCE)));
	
	// URLs
	private final String sentence5 = "[21:56] <gantox> www.lunarstorm.se is good.";
	private final ArrayList<Token> answer5 = new ArrayList<Token>(Arrays.asList(
			new Token(TokenType.START_OF_SENTENCE),
			new Token("is"),
			new Token("good"),
			new Token(TokenType.END_OF_SENTENCE)));
	
	// system sentence
	private final String sentence6 = "=== gantox is now known as noob";
	// answer to sentence6 should be an empty list.
	
	// bot user
	private final String sentence7 = "<ubotu> petro: The linux terminal or command-line interface is very powerful. Open a terminal ...";
	private final String sentence8 = "<ubottu> petro: The linux terminal or command-line interface is very powerful. Open a terminal ...";
	
	TextParser parser = new TextParser();

	@Test
	public void testTokenizeSimple() {
		assertTrue(answer1.equals(parser.tokenize(sentence1)));
	}

	@Test
	public void testTokenizeSentence() {
		assertTrue(answer2.equals(parser.tokenize(sentence2)));
		assertTrue(answer3.equals(parser.tokenize(sentence3)));
	}

	@Test
	public void testTokenizeURL() {
		assertTrue(answer4.equals(parser.tokenize(sentence4)));
		assertTrue(answer5.equals(parser.tokenize(sentence5)));
	}

	@Test
	public void testSystemMessage() {
		assertTrue(parser.tokenize(sentence6).isEmpty());
	}
	
	@Test
	public void testGetUser() {
		// test sentence without user
		assertTrue(parser.getUser(sentence1).equals(""));
		assertTrue(parser.getUser(sentence2).equals("HedgeMage"));
		assertTrue(parser.getUser(sentence3).equals("Ugglan"));
		assertTrue(parser.getUser(sentence4).equals("svEnSlaGGare"));
		assertTrue(parser.getUser(sentence5).equals("gantox"));
		// test system message
		assertTrue(parser.getUser(sentence6).equals(""));
		// test bot user
		assertTrue(parser.getUser(sentence7).equals(""));
		assertTrue(parser.getUser(sentence8).equals(""));	
	}
	
	@Test
	public void testFilterUser() {
		List<Token> answer = new ArrayList<Token>(Arrays.asList(
				new Token(TokenType.START_OF_SENTENCE),
				new Token("i"),
				new Token("haven't"),
				new Token("used"),
				new Token("thin"),
				new Token("clients"),
				new Token("much"),
				new Token("i'm"),
				new Token("sorry"),
				new Token("i"),
				new Token("can"),
				new Token("answer"),
				new Token("general"),
				new Token("questions"),
				new Token("though"),
				new Token(TokenType.END_OF_SENTENCE)));
		
		parser.tokenize("[21:56] <Deoovo>");
		List<Token> tokens = parser.tokenize(
				"[21:56] <HedgeMage> Deoovo: I haven't used thin clients much, I'm sorry.  I can answer general questions, though.");
		
		Assert.assertEquals(answer, tokens);
	}
}
