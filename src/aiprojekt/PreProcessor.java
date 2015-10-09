package aiprojekt;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The pre-processor process text files, generates a n-gram model and saves to file
 */
public class PreProcessor {
	private static final String BIG_DUMP_LOGS_PATH = "res/chatlogs/big_dump_logs/"; 
	private static final String SMALL_DUMP_LOGS_PATH = "res/chatlogs/small_dump_logs";
	public static final String WRITE_TO_PATH = "res/bin/";
	public static final String FILE_NAME = "ngrams.bin";
	public static final String FILE_PATH = WRITE_TO_PATH + FILE_NAME;
	
	private final boolean sampleLogs = true;
	private final boolean timer = true;
	
	private int processedSentences = 0;
	private NGramModel ngramModel = new NGramModel(NGramModel.DEFAULT_MAX_NGRAM_LENGTH);
	
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
		
		long startTime = System.currentTimeMillis();
		processFiles(file);
		this.ngramModel.end();
		long stopTime = System.currentTimeMillis();
		
		if (timer) {
			System.out.println("Elapsed time was " + (stopTime - startTime) / 1000.0 + " seconds.");
			System.out.println("Processed "+ processedSentences +" sentences");
			
			System.out.println("Total n-grams: " + ngramModel.getNgrams().size());
			for (int i = 1; i <= ngramModel.maxLength(); i++) {
				System.out.println("Number of n-" + i + " grams: " + ngramModel.countForNGram(i));
			}	
		}
		
		File directory = new File(WRITE_TO_PATH);
		if (!directory.isDirectory()) {
			directory.mkdirs();
		}
		
		writeToFile(FILE_PATH);
		
		if (timer) {
			System.out.println("Memory: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024 + " MB");
		}
	}
	
	/**
	 * Returns the n-gram model
	 * @return
	 */
	public NGramModel getNgramModel() {
		if (processedSentences != 0) {
			return ngramModel;
		} else {
			System.err.println("PreProcessor didn't process any sentences. (check that a file at "+SMALL_DUMP_LOGS_PATH+" or "
					+ BIG_DUMP_LOGS_PATH + " exists)");
			return null;
		}
	}
	
	/**
	 * Writes the data to file
	 */
	public void writeToFile(String path) {
		try (DataOutputStream outputStream = new DataOutputStream(
				new BufferedOutputStream(new FileOutputStream(path)))) {
			//First write all unique tokens
			Map<Token, Integer> tokenToId = new HashMap<>();
			
			Set<Token> tokens = new HashSet<>();
			for (NGram ngram : this.ngramModel.getNgrams().keySet()) {
				if (ngram.length() == 1) {
					tokens.add(ngram.at(0));
				}
			}
			
			outputStream.writeInt(tokens.size());	
			int id = 0;
			for (Token token : tokens) {
				outputStream.writeUTF(token.toString());
				tokenToId.put(token, id);
				id++;
			}
			
			//Then the Good-Turing parameters
			outputStream.writeDouble(this.ngramModel.getGoodTuringEstimation().getA());
			outputStream.writeDouble(this.ngramModel.getGoodTuringEstimation().getB());
			
			//Then the top ranked unigrams
			outputStream.writeInt(this.ngramModel.topUnigrams().size());
			for (NGram ngram : this.ngramModel.topUnigrams()) {
				Token token = ngram.at(0);
				outputStream.writeInt(tokenToId.get(token));
			}
						
			//Then the n-grams, where the token id points to the previous table
			outputStream.writeInt(this.ngramModel.getNgrams().size());
							
			for (Map.Entry<NGram, Integer> current : this.ngramModel.getNgrams().entrySet()) {
				outputStream.writeInt(current.getKey().length());
				
				for (int i = 0; i < current.getKey().length(); i++) {
					Token token = current.getKey().at(i);
					outputStream.writeInt(tokenToId.get(token));
				}
				
				outputStream.writeInt(current.getValue());
			}
			
			outputStream.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Tokenizes and indexes the file @code{file}. If @code{file} is a
	 * directory, all its files and subdirectories are recursively processed.
	 */
	public void processFiles(File file) {
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
						this.ngramModel.processTokens(parser.tokenize(sentence));
						processedSentences++;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
