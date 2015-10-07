package aiprojekt;

import java.io.*;
import java.util.*;

public class Loader {
	public static void main(String[] args) {
		Loader loader = new Loader();
		long start = System.currentTimeMillis();
		NGramModel ngramModel = loader.load(PreProcessor.FILE_PATH);
		Map<NGram, Integer> ngrams = ngramModel.getNgrams();

		System.out.println("Loaded: " + (System.currentTimeMillis() - start) / 1000.0 + " s");
		System.out.println("Memory: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime()
						.freeMemory()) / 1024 / 1024 + " MB");
		System.out.println("N-grams: " + ngrams.size());

		System.out.println(ngrams.get(NGram.fromTokens(new Token(TokenType.START_OF_SENTENCE), new Token("hello"), new Token("you"))));
		System.out.println(ngrams.get(NGram.fromWords("hello")));
		System.out.println(ngrams.get(NGram.fromWords("greetings")));
	}

	/**
	 * Loads and returns NGram model from disc.
	 * 
	 * @param path File path to file containing stored NGram model.
	 * @return The model or null if not loaded
	 */
	public NGramModel load(String path) {
		NGramModel ngramModel = new NGramModel(NGramModel.DEFAULT_MAX_NGRAM_LENGTH);
		File file = new File(path);
		if (!file.exists()) {
			// we have to create file
			PreProcessor processor = new PreProcessor();
			processor.run();
			return processor.getNgramModel();
		} else {
			try (DataInputStream inputStream = new DataInputStream(
					new BufferedInputStream(new FileInputStream(path)))) {

				int numTokens = inputStream.readInt();
				Map<Integer, Token> idToToken = new HashMap<>(numTokens);
				for (int id = 0; id < numTokens; id++) {
					String token = inputStream.readUTF();

					if (token.equals("<s>")) {
						idToToken.put(id,
								new Token(TokenType.START_OF_SENTENCE));
					} else if (token.equals("</s>")) {
						idToToken.put(id, new Token(TokenType.END_OF_SENTENCE));
					} else {
						idToToken.put(id, new Token(token));
					}
				}

				int count = inputStream.readInt();
				Map<NGram, Integer> ngrams = new HashMap<>(count);

				List<Token> tokenBuffer = new ArrayList<>();

				for (int i = 0; i < count; i++) {
					int length = inputStream.readInt();
					tokenBuffer.clear();

					for (int j = 0; j < length; j++) {
						int tokenId = inputStream.readInt();
						tokenBuffer.add(idToToken.get(tokenId));
					}

					int ngramCount = inputStream.readInt();
					ngrams.put(NGram.fromList(tokenBuffer), ngramCount);
				}

				ngramModel.processNGrams(ngrams);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		
		return ngramModel;
	}
}
