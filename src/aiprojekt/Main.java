package aiprojekt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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
	 * Tokenizes and indexes the file @code{file}. If @code{file} is a
	 * directory, all its files and subdirectories are recursively processed.
	 */
	public static void processFiles(File f) {
		TextParser parser = new TextParser();
		// do not try to tokenize fs that cannot be read
		if (f.canRead()) {
			if (f.isDirectory()) {
				String[] fs = f.list();
				// an IO error could occur
				if (fs != null) {
					for (int i = 0; i < fs.length; i++) {
						processFiles(new File(f, fs[i]));
					}
				}
			} else {

				try (BufferedReader br = new BufferedReader(
						new FileReader(f))) {
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
