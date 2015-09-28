package aiprojekt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * The main entry point for the preprocessor stage
 */
public class Main {
	// the big dump goes in this path
	static final String BIG_DUMP_LOGS_PATH = "res/chatlogs/big_dump_logs/"; 
	// any small dump goes in this path
	static final String SMALL_DUMP_LOGS_PATH = "res/chatlogs/small_dump_logs"; 
	// run with 
	static final boolean SAMPLE_LOGS = true;
	static final boolean TIMER = true;
	
	static int processedSentences = 0;

	public static void main(String[] args) {
		processedSentences = 0;
		if (args.length == 0) {
			File file;
			if (SAMPLE_LOGS) {
				file = new File(SMALL_DUMP_LOGS_PATH);
			} else {
				file = new File(BIG_DUMP_LOGS_PATH);
			}
			if (TIMER) {
				long startTime = System.currentTimeMillis();
				processFiles(file);
				long stopTime = System.currentTimeMillis();
				System.out.println("Elapsed time was " + (stopTime - startTime)/1000.0 + " seconds.");
				System.out.println("Processed "+ processedSentences +" sentences");
			} else {
				processFiles(file);
			}
		} else {
			System.err
					.println("ERROR: too many arguments to main, not implemented.");
		}
	}

	/**
<<<<<<< HEAD

	 * Tokenizes and indexes the file @code{file}. If @code{file} is a directory, all
	 * its files and subdirectories are recursively processed.
=======
	 * Tokenizes and indexes the file @code{file}. If @code{file} is a
	 * directory, all its files and subdirectories are recursively processed.
>>>>>>> e97afa0e997d49f717f7c075992154ecab596d16
	 */
	public static void processFiles(File file) {
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

				try (BufferedReader br = new BufferedReader(
<<<<<<< HEAD
						new FileReader(file))) {
=======
						new FileReader(f))) {
>>>>>>> e97afa0e997d49f717f7c075992154ecab596d16
					String sentence;

					while ((sentence = br.readLine()) != null) {
						List<Token> tokens = parser.tokenize(sentence);
						processedSentences ++;
						// do something with the tokenized sentence.
						//System.out.println(Arrays.toString(tokens.toArray()));
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
