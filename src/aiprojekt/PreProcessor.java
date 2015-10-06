package aiprojekt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * The main entry point for the preprocessor stage
 */
public class PreProcessor {
	// the big dump goes in this path
	static final String BIG_DUMP_LOGS_PATH = "res/chatlogs/big_dump_logs/"; 
	// any small dump goes in this path
	static final String SMALL_DUMP_LOGS_PATH = "res/chatlogs/small_dump_logs"; 
	
	// run with 
	private final boolean sampleLogs = false;
	private final boolean timer = true;
	
	private int processedSentences = 0;
	private NGramModel ngramModel = new NGramModel(3);
	
	public static void main(String[] args) {
		PreProcessor processor = new PreProcessor();
		processor.run();
	}

	/**
	 * Executes the pre-processor stage
	 */
	public void run() {
		File file;
		
		if (sampleLogs) {
			file = new File(SMALL_DUMP_LOGS_PATH);
		} else {
			file = new File(BIG_DUMP_LOGS_PATH);
		}
		
		if (timer) {
			long startTime = System.currentTimeMillis();
			processFiles(file);
			ngramModel.end();
			long stopTime = System.currentTimeMillis();
			
			System.out.println("Elapsed time was " + (stopTime - startTime) / 1000.0 + " seconds.");
			System.out.println("Processed "+ processedSentences +" sentences");
			
			System.out.println("Total n-grams: " + ngramModel.getNgrams().size());
			for (int i = 1; i <= ngramModel.maxLength(); i++) {
				System.out.println("Number of n-" + i + " grams: " + ngramModel.countForNGram(i));
			}	
		} else {
			processFiles(file);
		}
	}
	
	/**
	 * Tokenizes and indexes the file @code{file}. If @code{file} is a
	 * directory, all its files and subdirectories are recursively processed.
	 */
	private void processFiles(File file) {
		TextParser parser = new TextParser();
		
		// do not try to tokenize fs that cannot be read
		if (file.canRead()) {
			if (file.isDirectory()) {
				String[] fs = file.list();
				// an IO error could occur
				if (fs != null) {
					for (int i = 0; i < fs.length; i++) {
						processFiles(new File(file, fs[i]));
					}
				}
			} else {
				try (BufferedReader br = new BufferedReader(new FileReader(file))) {
					String sentence;
					while ((sentence = br.readLine()) != null) {
						ngramModel.processTokens(parser.tokenize(sentence));
						processedSentences++;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
