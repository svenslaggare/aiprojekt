package aiprojekt.tests;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import aiprojekt.NGram;
import aiprojekt.NGramModel;
import aiprojekt.TextParser;
import aiprojekt.Token;

/**
 * Tests the N-gram model
 */
public class NGramModelTest {
	private static final List<Token> tokens1 = Arrays.asList(
		new Token("Hello"), new Token("my"), new Token("friend"),
		new Token("how"), new Token("are"), new Token("you?"));
		
	
	/**
	 * Loads the given tokens from the given file
	 * @param fileName The name of the file
	 */
	private static List<List<Token>> loadTokensFromFile(String fileName) {
		List<List<Token>> sentences = new ArrayList<List<Token>>();
		
		TextParser parser = new TextParser();
		File file = new File(fileName);
		
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String sentence;
			while ((sentence = br.readLine()) != null) {
				sentences.add(parser.tokenize(sentence));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return sentences;
	}
	
	/**
	 * Creates tokens for the given words
	 * @param words The words
	 */
	private static Token[] createTokens(String... words) {
		Token[] tokens = new Token[words.length];
		
		for (int i = 0; i < words.length; i++) {
			tokens[i] = new Token(words[i]);
		}
		
		return tokens;
	}	
	
	/**
	 * Creates a n-gram with the given words
	 * @param words The words
	 */
	private static NGram createNGram(String... words) {
		return new NGram(createTokens(words));
	}
	
	/**
	 * Tests getting unigrams from a list of tokens
	 */
	@Test
	public void testGetNGrams1() {
		List<NGram> nGrams = NGramModel.getNGrams(tokens1, 1);
		assertEquals(6, nGrams.size());
		assertEquals(new NGram(new Token[] { tokens1.get(0) }), nGrams.get(0));
		assertEquals(new NGram(new Token[] { tokens1.get(1) }), nGrams.get(1));
		assertEquals(new NGram(new Token[] { tokens1.get(2) }), nGrams.get(2));
		assertEquals(new NGram(new Token[] { tokens1.get(3) }), nGrams.get(3));
		assertEquals(new NGram(new Token[] { tokens1.get(4) }), nGrams.get(4));
		assertEquals(new NGram(new Token[] { tokens1.get(5) }), nGrams.get(5));
	}
	
	/**
	 * Tests getting unigrams, bigrams from a list of tokens
	 */
	@Test
	public void testGetNGrams2() {
		List<NGram> nGrams = NGramModel.getNGrams(tokens1, 2);
		assertEquals(6 + 5, nGrams.size());
		
		assertEquals(new NGram(new Token[] { tokens1.get(0) }), nGrams.get(0));
		assertEquals(new NGram(new Token[] { tokens1.get(0), tokens1.get(1) }), nGrams.get(1));
		
		assertEquals(new NGram(new Token[] { tokens1.get(1) }), nGrams.get(2));
		assertEquals(new NGram(new Token[] { tokens1.get(1), tokens1.get(2) }), nGrams.get(3));
		
		assertEquals(new NGram(new Token[] { tokens1.get(2) }), nGrams.get(4));
		assertEquals(new NGram(new Token[] { tokens1.get(2), tokens1.get(3) }), nGrams.get(5));
		
		assertEquals(new NGram(new Token[] { tokens1.get(3) }), nGrams.get(6));
		assertEquals(new NGram(new Token[] { tokens1.get(3), tokens1.get(4) }), nGrams.get(7));
		
		assertEquals(new NGram(new Token[] { tokens1.get(4) }), nGrams.get(8));
		assertEquals(new NGram(new Token[] { tokens1.get(4), tokens1.get(5) }), nGrams.get(9));
		
		assertEquals(new NGram(new Token[] { tokens1.get(5) }), nGrams.get(10));
	}
	
	/**
	 * Tests getting unigrams, bigrams, trigrams from a list of tokens
	 */
	@Test
	public void testGetNGrams3() {
		List<NGram> nGrams = NGramModel.getNGrams(tokens1, 3);
		assertEquals(6 + 5 + 4, nGrams.size());
		
		assertEquals(new NGram(new Token[] { tokens1.get(0) }), nGrams.get(0));
		assertEquals(new NGram(new Token[] { tokens1.get(0), tokens1.get(1) }), nGrams.get(1));
		assertEquals(new NGram(new Token[] { tokens1.get(0), tokens1.get(1), tokens1.get(2) }), nGrams.get(2));
	}
	
	/**
	 * Tests the startWith method
	 */
	@Test
	public void testStartsWith() {
		assertTrue(createNGram("hello", "does").startsWith(createNGram("hello")));
		assertTrue(createNGram("hello", "does").startsWith(createNGram("hello", "does")));
		assertFalse(createNGram("all", "does").startsWith(createNGram("hello", "does")));
		assertFalse(createNGram("hello", "does").startsWith(createNGram("hello", "does", "you")));
		
		assertTrue(createNGram("hello", "does").startsWith(createNGram("hello"), false));
		assertFalse(createNGram("hello", "does").startsWith(createNGram("hello", "does"), false));
	}
	
	/**
	 * Tests the subgram method
	 */
	@Test
	public void testSubgram() {
		assertEquals(createNGram("hello", "my"), createNGram("hello", "my", "friend").subgram(2));
	}
	
	/**
	 * Tests creating a n-gram model for a list of tokens
	 */
	@Test
	public void testCreateModel() {
		List<List<Token>> sentences = loadTokensFromFile("res/chatlogs/2006-05-27-#ubuntu.txt");
		
		NGramModel nGramModel = new NGramModel(2);
		for (List<Token> sentence : sentences) {
			nGramModel.processTokens(sentence);
		}
				
		System.out.println(nGramModel.predictNext(new NGram(createTokens("hello")), 5));
	}
}
