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

/**
 * The main entry point for the pre-processor stage
 */
public class Main {
	static final String DIRPATH = "res/chatlogs/";

	public static void main(String[] args) {
		if (args.length == 0) {
			File file = new File(DIRPATH);
			processFiles(file);
		} else {
			System.err
					.println("ERROR: too many arguments to main, not implemented.");
		}
	}

	/**
	 * Tokenizes and indexes the file @code{f}. If @code{f} is a directory, all
	 * its files and subdirectories are recursively processed.
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
				try (BufferedReader br = new BufferedReader(new FileReader(f))) {
					String sentence;
					while ((sentence = br.readLine()) != null) {
						List<String> tokens = parser.tokenize(sentence);
						// do something with the tokenized sentence.
						System.out.println(Arrays.toString(tokens.toArray()));
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
