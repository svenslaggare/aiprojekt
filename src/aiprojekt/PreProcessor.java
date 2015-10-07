package aiprojekt;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * The main entry point for the preprocessor stage
 */
public class PreProcessor {
	private static final String BIG_DUMP_LOGS_PATH = "res/chatlogs/big_dump_logs/"; 
	private static final String SMALL_DUMP_LOGS_PATH = "res/chatlogs/small_dump_logs"; 
	public static final String WRITE_TO_FILE = "res/bin/ngrams.bin";
	
	// run with 
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
		ngramModel.end();
		long stopTime = System.currentTimeMillis();
		
		if (timer) {
			System.out.println("Elapsed time was " + (stopTime - startTime) / 1000.0 + " seconds.");
			System.out.println("Processed "+ processedSentences +" sentences");
			
			System.out.println("Total n-grams: " + ngramModel.getNgrams().size());
			for (int i = 1; i <= ngramModel.maxLength(); i++) {
				System.out.println("Number of n-" + i + " grams: " + ngramModel.countForNGram(i));
			}	
		}
		
		writeToFile();
		
		if (timer) {
			System.out.println("Memory: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024 + " MB");
			System.out.println(this.ngramModel.getCount(NGram.fromTokens(new Token(TokenType.START_OF_SENTENCE), new Token("hello"), new Token("you"))));
			System.out.println(this.ngramModel.getCount(NGram.fromWords("hello")));
			System.out.println(this.ngramModel.getCount(NGram.fromWords("greetings")));
		}
	}
	
	/**
	 * Writes the data to file
	 */
	private void writeToFile() {
		try (DataOutputStream outputStream = new DataOutputStream(
				new BufferedOutputStream(new FileOutputStream(WRITE_TO_FILE)))) {
			Map<Token, Integer> tokenToId = new HashMap<>();
			
			outputStream.writeInt(this.ngramModel.unigrams().size() - 2);	
			int id = 0;
			for (NGram unigram : this.ngramModel.unigrams()) {
				Token token = unigram.at(0);
				if (token.getType() == TokenType.WORD) {
					outputStream.writeUTF(token.getWord());
					tokenToId.put(token, id);
					id++;
				}
			}
							
			outputStream.writeInt(this.ngramModel.getNgrams().size());
							
			for (Map.Entry<NGram, Integer> current : this.ngramModel.getNgrams().entrySet()) {
				outputStream.writeInt(current.getKey().length());
				
				for (int i = 0; i < current.getKey().length(); i++) {
					Token token = current.getKey().at(i);
					outputStream.writeByte(token.getType().ordinal());
					if (token.getType() == TokenType.WORD) {
						outputStream.writeInt(tokenToId.get(token));
					}
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
