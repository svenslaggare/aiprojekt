package aiprojekt.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import aiprojekt.NGramModel;
import aiprojekt.Token;
import aiprojekt.TokenType;
import aiprojekt.WordPredictor;

public class WordPredictorTest {
	private final List<Token> sentence1 = Arrays.asList(
			new Token(TokenType.START_OF_SENTENCE),
			new Token("hello"),
			new Token("my"),
			new Token("name"),
			new Token("is"),
			new Token(TokenType.END_OF_SENTENCE));

	private final List<Token> sentence2 = Arrays.asList(
			new Token(TokenType.START_OF_SENTENCE),
			new Token("hello"),
			new Token("my"),
			new Token(TokenType.END_OF_SENTENCE));
	
	private final List<Token> sentence3 = Arrays.asList(
			new Token(TokenType.START_OF_SENTENCE),
			new Token("hello"),
			new Token("bye"),
			new Token(TokenType.END_OF_SENTENCE));
	
	/**
	 * Tests predicting the next word
	 */
	@Test
	public void testPredictNextWord() {
		NGramModel model = new NGramModel(3);
		model.processTokens(sentence1);
		model.processTokens(sentence2);
		model.end();
		
		WordPredictor predictor = new WordPredictor(model, 1);
		assertEquals(Arrays.asList("my"), predictor.predictNextWord("hello"));
	}
	
	/**
	 * Tests predicting the next word
	 */
	@Test
	public void testPredictNextWord2() {
		NGramModel model = new NGramModel(3);
		model.end();	
		model.processTokens(sentence1);
		model.processTokens(sentence2);
		model.processTokens(sentence3);
		
		WordPredictor predictor = new WordPredictor(model, 2);
		
		List<String> results = predictor.predictNextWord("hello");
		Collections.sort(results);
		assertEquals(Arrays.asList("bye", "my"), results);
	}
	
	/**
	 * Tests predicting the next word when there are more n-grams in the input than in the model
	 */
	@Test
	public void testLongerThanMax() {
		//TODO: Fix this test case
//		NGramModel model = new NGramModel(3);
//		model.processTokens(sentence1);
//		model.end();	
//	
//		WordPredictor predictor = new WordPredictor(model, 1);
//		assertEquals(Arrays.asList("name"), predictor.predictNextWord("hello my"));
	}
	
	/**
	 * Tests only using the most recent tokens
	 */
	@Test
	public void testUseRecent() {
		//TODO: Fix this test case
		NGramModel model = new NGramModel(3);
		model.processTokens(sentence1);
		model.end();	
	
		WordPredictor predictor = new WordPredictor(model, 2);
		List<Token> tokens = new ArrayList<>(Arrays.asList(new Token("hello"), new Token("my"), new Token("friend")));
		predictor.useRecentTokens(tokens);
		assertEquals(2, tokens.size());
		assertEquals(new Token("my"), tokens.get(0));
		assertEquals(new Token("friend"), tokens.get(1));
	}
}
