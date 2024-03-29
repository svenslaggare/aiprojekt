package aiprojekt;

import java.io.*;
import java.util.*;

/**
 * Loads a saved n-gram model from a file
 */
public class Loader {
	public static void main(String[] args) {
		Loader loader = new Loader();
		long start = System.currentTimeMillis();
		NGramModel ngramModel = loader.load(PreProcessor.FILE_PATH);

		System.out.println("Loaded: " + (System.currentTimeMillis() - start) / 1000.0 + " s");
		System.out.println("Memory: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024 + " MB");
		System.out.println("Total n-grams: " + ngramModel.numNgrams());

		for (int i = 1; i <= ngramModel.maxLength(); i++) {
			System.out.println("Number of " + i + "-grams: " + ngramModel.numberOfNGramLength(i));
		}	
	}

	/**
	 * Loads and returns a n-gram model from disc.
	 * 
	 * @param path File path to file containing stored NGram model.
	 * @return The model or null if not loaded
	 */
	public NGramModel load(String path) {
		NGramModel ngramModel = new NGramModel(NGramModel.DEFAULT_MAX_NGRAM_LENGTH, true);
		File file = new File(path);
		if (!file.exists()) {
			// we have to create file
			PreProcessor processor = new PreProcessor();
			processor.run();
			return processor.getNgramModel();
		} else {
			try (DataInputStream inputStream = new DataInputStream(
					new BufferedInputStream(new FileInputStream(path)))) {

				//The token index
				int numTokens = inputStream.readInt();
				Map<Integer, Token> idToToken = new HashMap<>(numTokens);
				for (int id = 0; id < numTokens; id++) {
					String token = inputStream.readUTF();

					if (token.equals(TokenType.START_OF_SENTENCE.toString())) {
						idToToken.put(id, new Token(TokenType.START_OF_SENTENCE));
					} else if (token.equals(TokenType.END_OF_SENTENCE.toString())) {
						idToToken.put(id, new Token(TokenType.END_OF_SENTENCE));
					} else {
						idToToken.put(id, new Token(token));
					}
				}
				
				//Good-Turing parameters
				int total = inputStream.readInt();
				double a = inputStream.readDouble();
				double b = inputStream.readDouble();
				ngramModel.getGoodTuringEstimation().setTotal(total);
				ngramModel.getGoodTuringEstimation().setLogLinear(a, b);
								
				//The top ranked unigrams
				int topUnigramsCount = inputStream.readInt();
				for (int i = 0; i < topUnigramsCount; i++) {
					int tokenId = inputStream.readInt();
					ngramModel.topUnigrams().add(NGram.fromTokens(idToToken.get(tokenId)));
				}
				
				//The n-grams
				int count = inputStream.readInt();
				List<Token> tokenBuffer = new ArrayList<>();

				for (int i = 0; i < count; i++) {
					int length = inputStream.readInt();
					tokenBuffer.clear();

					for (int j = 0; j < length; j++) {
						int tokenId = inputStream.readInt();
						tokenBuffer.add(idToToken.get(tokenId));
					}

					int ngramCount = inputStream.readInt();
					ngramModel.addNGram(NGram.fromList(tokenBuffer), ngramCount);
				}
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		
		return ngramModel;
	}
}
