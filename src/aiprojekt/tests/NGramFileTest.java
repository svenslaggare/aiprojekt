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
		preProcessor.getNgramModel().end();
		
		String testFilePath = "res/bin/test.bin";
		
		File dir = new File("res/bin");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		
		preProcessor.writeToFile(testFilePath);
		
		Loader loader = new Loader();
		NGramModel loadedModel = loader.load(testFilePath);
		assertEquals(loadedModel.getNgrams().size(), preProcessor.getNgramModel().getNgrams().size());
		
		for (Map.Entry<NGram, Integer> current : preProcessor.getNgramModel().getNgrams().entrySet()) {
			assertEquals((int)current.getValue(), loadedModel.getCount(current.getKey()));
		}
		
		for (int i = 1; i <= preProcessor.getNgramModel().maxLength(); i++) {
			assertEquals(loadedModel.countForNGram(i), preProcessor.getNgramModel().countForNGram(i));
		}
	}
}
