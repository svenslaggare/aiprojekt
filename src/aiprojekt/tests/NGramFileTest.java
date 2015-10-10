package aiprojekt.tests;

import static org.junit.Assert.*;
import java.io.File;
import java.util.Map;

import org.junit.Test;

import aiprojekt.Loader;
import aiprojekt.NGram;
import aiprojekt.NGramModel;
import aiprojekt.PreProcessor;

/**
 * Tests the loader and pre-processor
 */
public class NGramFileTest {
	/**
	 * Tests processing files, writing it to file and loading them
	 */
	@Test
	public void testProcessAndLoad() {
		PreProcessor preProcessor = new PreProcessor();
		preProcessor.processFiles(new File("res/tests/test1.txt"));
		NGramModel actualModel = preProcessor.getNgramModel();
		actualModel.end();
		
		String testFilePath = "res/bin/test.bin";
		
		File dir = new File("res/bin");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		
		preProcessor.writeToFile(testFilePath);
		
		Loader loader = new Loader();
		NGramModel loadedModel = loader.load(testFilePath);
		assertEquals(loadedModel.numNgrams(), actualModel.getNgrams().size());
		
		for (Map.Entry<NGram, Integer> current : actualModel.getNgrams().entrySet()) {
			assertEquals((int)current.getValue(), loadedModel.getCount(current.getKey()));
		}
		
		for (int i = 1; i <= actualModel.maxLength(); i++) {
			assertEquals(loadedModel.numberOfNGramLength(i), actualModel.numberOfNGramLength(i));
		}
		
		for (int i = 0; i < actualModel.topUnigrams().size(); i++) {
			assertEquals(loadedModel.topUnigrams().get(i), actualModel.topUnigrams().get(i));
		}
		
		assertEquals(
			loadedModel.getGoodTuringEstimation().getTotal(),
			actualModel.getGoodTuringEstimation().getTotal());
		
		assertEquals(
			loadedModel.getGoodTuringEstimation().getA(),
			actualModel.getGoodTuringEstimation().getA(), 1E-6);
		
		assertEquals(
			loadedModel.getGoodTuringEstimation().getB(),
			actualModel.getGoodTuringEstimation().getB(), 1E-6);
	}
}
