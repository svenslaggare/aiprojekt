package aiprojekt.tests;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import aiprojekt.NGram;
import aiprojekt.NGramModel;
import aiprojekt.NGramTree;
import aiprojekt.TextParser;
import aiprojekt.Token;
import aiprojekt.TokenType;

/**
 * Tests the N-gram model
 */
public class NGramModelTest {
	private static final List<Token> tokens1 = Arrays.asList(
		new Token("hello"), new Token("my"), new Token("friend"),
		new Token("how"), new Token("are"), new Token("you"));
		
	private static final List<List<Token>> sentences = loadTokensFromFile(
		"res/tests/ubuntu.txt");

	
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
	 * Tests getting unigrams from a list of tokens
	 */
	@Test
	public void testGetNGrams1() {
		List<NGram> nGrams = NGramModel.getNgrams(tokens1, 1);
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
		List<NGram> nGrams = NGramModel.getNgrams(tokens1, 2);
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
		List<NGram> nGrams = NGramModel.getNgrams(tokens1, 3);
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
		assertTrue(NGram.fromWords("hello", "does").startsWith(NGram.fromWords("hello")));
		assertTrue(NGram.fromWords("hello", "does").startsWith(NGram.fromWords("hello", "does")));
		assertFalse(NGram.fromWords("all", "does").startsWith(NGram.fromWords("hello", "does")));
		assertFalse(NGram.fromWords("hello", "does").startsWith(NGram.fromWords("hello", "does", "you")));
		
		assertTrue(NGram.fromWords("hello", "does").startsWith(NGram.fromWords("hello"), false));
		assertFalse(NGram.fromWords("hello", "does").startsWith(NGram.fromWords("hello", "does"), false));
	}
	
	/**
	 * Tests the subgram method
	 */
	@Test
	public void testSubgram() {
		assertEquals(
			NGram.fromWords("hello", "my"),
			NGram.fromWords("hello", "my", "friend").subgram(2));
	}
	
	/**
	 * Tests the first method
	 */
	@Test
	public void testFirst() {
		assertEquals(
			NGram.fromWords("hello"),
			NGram.fromWords("hello", "my", "friend").first());
	}
	
	/**
	 * Tests the first method
	 */
	@Test
	public void testLast() {
		assertEquals(
			NGram.fromWords("friend"),
			NGram.fromWords("hello", "my", "friend").last());
	}
	
	/**
	 * Tests creating a n-gram model for a list of tokens
	 */
	@Test
	public void testCreateModel() {
		NGramModel ngramModel = new NGramModel(3);
		for (List<Token> sentence : sentences) {
			ngramModel.processTokens(sentence);
		}
				
		NGram ngram = NGram.fromWords("hello", "i");
		
		assertEquals(ngramModel.getCount(ngram), (int)ngramModel.getNgrams().get(ngram));
	}
	
	/**
	 * Tests creating a n-gram model for a list of tokens
	 */
	@Test
	public void testCreateModel2() {
		NGramModel ngramModel = new NGramModel(3);
		for (List<Token> sentence : sentences) {
			ngramModel.processTokens(sentence);
		}
		
		NGramTree tree = NGramTree.createTree(ngramModel);
		
		NGram ngram = NGram.fromWords("hello");

		for (NGramTree.Result result : tree.findResults(ngram)) {
			assertEquals(result.getCount(), (int)ngramModel.getNgrams().get(result.getNgram()));
		}
		
		ngram = NGram.fromWords("hello", "i");
		
		for (NGramTree.Result result : tree.findResults(ngram)) {
			assertEquals(result.getCount(), (int)ngramModel.getNgrams().get(result.getNgram()));
		}
	}
	
	/**
	 * Tests creating a n-gram model with NGrams as parameter.
	 */
	@Test
	public void testCreateModel3() {
		NGramModel ngramModel = new NGramModel(3);
		Map<NGram, Integer> ngrams = new HashMap<NGram, Integer>();
		NGram ngram1 = new NGram(new Token[]{new Token(TokenType.START_OF_SENTENCE),new Token("hello")});
		NGram ngram2 = new NGram(new Token[]{new Token("hello"),new Token("i")});
		ngrams.put(ngram1,new Integer(2));
		ngrams.put(ngram2,new Integer(4));
		
		ngramModel.addNGrams(ngrams);
		
		assertEquals(ngramModel.getCount(ngram1), (int)ngramModel.getNgrams().get(ngram1));
		assertEquals(ngramModel.getCount(ngram2), (int)ngramModel.getNgrams().get(ngram2));	
	}
	
	/**
	 * Tests predicting the next word
	 */
	@Test
	public void testPredictNext1() {
		NGramModel ngramModel = new NGramModel(3);
		ngramModel.processTokens(tokens1);		
		ngramModel.end();
		
		NGram ngram = NGram.fromWords("hello");
		List<NGramModel.Result> results = ngramModel.predictNext(ngram, 5);
		assertEquals(NGram.fromWords("hello", "how"), results.get(0).getNGram());
	}
	
	/**
	 * Tests predicting the next word
	 */
	@Test
	public void testPredictNext2() {
//		NGramModel ngramModel = new NGramModel(3);
//		ngramModel.processTokens(tokens1);
//		ngramModel.end();
//		
//		NGram ngram = NGram.fromWords("hello", "you");	
//		System.out.println(ngramModel.predictNext(ngram, 5));
	}
}
