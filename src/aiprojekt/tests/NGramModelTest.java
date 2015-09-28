package aiprojekt.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import aiprojekt.NGram;
import aiprojekt.NGramModel;
import aiprojekt.Token;

/**
 * Tests the N-gram model
 */
public class NGramModelTest {
	private static final List<Token> tokens1 = Arrays.asList(
		new Token("Hello"), new Token("my"), new Token("friend"),
		new Token("how"), new Token("are"), new Token("you?"));
		
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
}
