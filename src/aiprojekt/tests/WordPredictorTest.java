package aiprojekt.tests;

import static org.junit.Assert.*;

import java.util.Arrays;
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
			new Token("bye"),
			new Token(TokenType.END_OF_SENTENCE));
	
	private final List<Token> sentence3 = Arrays.asList(
			new Token(TokenType.START_OF_SENTENCE),
			new Token("hello"),
			new Token("my"),
			new Token("name"),
			new Token("is"),
			new Token(TokenType.END_OF_SENTENCE));
	
	/**
	 * Tests predicting the next word
	 */
	@Test
	public void testPredictNextWord() {
		NGramModel model = new NGramModel(4);
		model.processTokens(sentence1);

		WordPredictor predictor = new WordPredictor(model, 1);
		assertEquals(Arrays.asList("my"), predictor.predictNextWord("hello"));
	}
	
	/**
	 * Tests predicting the next word
	 */
	@Test
	public void testPredictNextWord2() {
		NGramModel model = new NGramModel(4);
		model.processTokens(sentence1);
		model.processTokens(sentence2);
		model.processTokens(sentence3);
		
		WordPredictor predictor = new WordPredictor(model, 2);
		assertEquals(Arrays.asList("my", "bye"), predictor.predictNextWord("hello"));
	}
	
	/**
	 * Tests predicting the next word when there are more n-grams in the input than in the model
	 */
	@Test
	public void testLongerThanMax() {
		NGramModel model = new NGramModel(2);
		model.processTokens(sentence1);

		WordPredictor predictor = new WordPredictor(model, 1);
		assertEquals(Arrays.asList("name"), predictor.predictNextWord("hello my"));
	}
}
