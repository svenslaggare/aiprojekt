package aiprojekt;

import java.io.*;
import java.util.*;

public class Loader {
	public static void main(String[] args) {
		try (DataInputStream inputStream = new DataInputStream(
				new BufferedInputStream(new FileInputStream("E:\\Programmering\\AI\\ngrams.bin")))) {

			long start = System.currentTimeMillis();
			int count = inputStream.readInt();
			Map<NGram, Integer> ngrams = new HashMap<>(count);
					
			List<Token> tokenBuffer = new ArrayList<>();
			Token startToken = new Token(TokenType.START_OF_SENTENCE);
			Token endToken = new Token(TokenType.END_OF_SENTENCE);
						
			for (int i = 0; i < count; i++) {
				int length = inputStream.readInt();
				tokenBuffer.clear();
								
				for (int j = 0; j < length; j++) {
					int type = inputStream.readByte();
					
					if (type == TokenType.START_OF_SENTENCE.ordinal()) {
						tokenBuffer.add(startToken);
					} else if (type == TokenType.END_OF_SENTENCE.ordinal()) {
						tokenBuffer.add(endToken);
					} else {
						String word = inputStream.readUTF();
						tokenBuffer.add(new Token(word));
					}
				}
				
				int ngramCount = inputStream.readInt();			
				ngrams.put(NGram.fromList(tokenBuffer), ngramCount);
			}
			
			System.out.println("Loaded: " + (System.currentTimeMillis() - start) / 1000.0 + " s");
			System.out.println("Memory: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024 + " MB");
			System.out.println("N-grams: " + ngrams.size());
			
			System.out.println(ngrams.get(NGram.fromTokens(new Token(TokenType.START_OF_SENTENCE), new Token("hello"), new Token("you"))));
			System.out.println(ngrams.get(NGram.fromWords("hello")));
			System.out.println(ngrams.get(NGram.fromWords("greetings")));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * Loads and returns NGram model from disc.
	 * @param path File path to file containing stored NGram model. 
	 */
	public NGramModel load(String path){
		Map<NGram, Integer> ngrams = new HashMap<>();
		try (DataInputStream inputStream = new DataInputStream(
				new BufferedInputStream(new FileInputStream(path)))) {

			
			int count = inputStream.readInt();
			List<Token> tokenBuffer = new ArrayList<>();
			Token startToken = new Token(TokenType.START_OF_SENTENCE);
			Token endToken = new Token(TokenType.END_OF_SENTENCE);
						
			for (int i = 0; i < count; i++) {
				int length = inputStream.readInt();
				tokenBuffer.clear();
								
				for (int j = 0; j < length; j++) {
					int type = inputStream.readByte();
					
					if (type == TokenType.START_OF_SENTENCE.ordinal()) {
						tokenBuffer.add(startToken);
					} else if (type == TokenType.END_OF_SENTENCE.ordinal()) {
						tokenBuffer.add(endToken);
					} else {
						String word = inputStream.readUTF();
						tokenBuffer.add(new Token(word));
					}
				}
				
				int ngramCount = inputStream.readInt();			
				ngrams.put(NGram.fromList(tokenBuffer), ngramCount);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		NGramModel ngramModel = new NGramModel(3);
		ngramModel.processNGrams(ngrams);
		return ngramModel;
	}
}
