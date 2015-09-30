package aiprojekt.tests;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import org.junit.Test;

import aiprojekt.NGram;
import aiprojekt.NGramModel;
import aiprojekt.Token;
import aiprojekt.WordPredictor;

public class WordPredictorTest {
	private int nGramMaxLength = 2; 
	private int numberOfResults = 1; 
	private final List<Token> tokens = Arrays.asList(new Token("hello"), new Token("my"), new Token("name"), new Token("is"));

	
	@Test
	public void testPredictNextWord() {
		NGramModel model = new NGramModel(nGramMaxLength);		
		model.processTokens(tokens);
		
		WordPredictor predictor = new WordPredictor(model, numberOfResults);
		
		String inputRow1 = "hello";
		String inputRow2 = "name";
		List<String> correct1 = Arrays.asList("my");
		List<String> correct2 = Arrays.asList("is");
		assertEquals(correct2, predictor.predictNextWord(inputRow2));
		assertEquals(correct1, predictor.predictNextWord(inputRow1));
		
		
		
	}

}
