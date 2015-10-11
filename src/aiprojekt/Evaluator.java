package aiprojekt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 1. Train model. Use training data to train the NGramModel.
 * 
 * 2. Extract testing sentences from test data != training data
 * 
 * 3. Test if actual next word == any of the top K predicted words.
 * 
 * 4. Store data (the position of the match in the top K list, the word position
 * in the sentence matched)
 * 
 * 5. Print data, ready for evaluation.
 * 
 * @author Andreas
 * 
 */
public class Evaluator {

	private static final String EVALUATE_FILE = "res/evaluation/evaluate.txt";
	private static final String USER_TRAINING_PATH = "res/evaluation/user_input_training/";
	private static final String USER_TESTING_PATH = "res/evaluation/user_input_testing/";
	private static final String OUTPUT_PATH = "res/evaluation/output/";
	private static final int NUM_RESULTS = 10;
	private static final int MAX_SENTENCE_LENGTH = 30;

	private static final String[] userCandidates = new String[] { "jrib",
			"LjL", "soundray", "bruenig", "bimberi", "gnomefreak",
			"dabaR", "crimsun", "nolimitsoya", "Pelo", "kitche",
			"ArrenLex", "defrysk", "Flannel", "apokryphos" };

	private NGramModel model;
	private Map<String, Integer> userCount;
	private int[] countTopResultHit = new int[NUM_RESULTS];
	private int[] countWordPositionHit = new int[MAX_SENTENCE_LENGTH];
	private int testedWords = 0;
	private LinkedList<Double> perplexities = new LinkedList<Double>();

	public static void main(String[] args) {
		// Used for extracting most frequent users
		// Evaluator userAdaptation = new Evaluator();
		// userAdaptation.extractTopCommunicatingUsers(USER_TRAINING_PATH); //
		// path to all (user training data) != training data

		Evaluator evaluator = new Evaluator();

		// evaluate model
		 System.out.println(evaluator.evaluate(EVALUATE_FILE));

		// evaluate user input learning (should loop over userCandidates)
//		for (int i = 0; i < userCandidates.length; i++) {
//			String data = evaluator.evaluateUserInput(userCandidates[i]);
//			writeToFile(data, OUTPUT_PATH + "user" + i + ".txt");
//			System.out.println("user " + userCandidates[i] + " written in "
//					+ OUTPUT_PATH + "user" + i + ".txt");
//		}
	}

	/**
	 * Trains and evaluates the model using training data AND user input from
	 * specified user.
	 * 
	 * @param user
	 *            The user to evaluate
	 */
	private String evaluateUserInput(String user) {
		model = trainModel();
		WordPredictor predictor = new WordPredictor(model, NUM_RESULTS);
		StringBuilder sb = new StringBuilder();
		// 1. extract the user's sentences from user_training_path
		File trainingFile = new File(USER_TRAINING_PATH);
		ArrayList<ArrayList<Token>> trainingSentences = extractUserSentences(
				user, trainingFile);
		sb.append("trainingSentences.size() = " + trainingSentences.size());
		
		// 2. train the model with the user input
		for (ArrayList<Token> sentence : trainingSentences) {
			predictor.addHistory(sentence);
		}
		
		// 3. extract the user's sentences from user_testing_path
		File testingFile = new File(USER_TESTING_PATH);
		ArrayList<ArrayList<Token>> testingSentences = extractUserSentences(
				user, testingFile);
		sb.append("testingSentences.size() = " + testingSentences.size());
		
		// 4. test the learned model with the user input
		sb.append(evaluate(predictor, testingSentences));

		// Compared to:
		model = trainModel();
		predictor = new WordPredictor(model, NUM_RESULTS);
		
		// 5. test the non-learned model with the user input
		sb.append(("NON-learned model for comparison:\n"));
		sb.append(evaluate(predictor, testingSentences));

		return sb.toString();
	}

	/**
	 * Evaluates the given predictor using the given sentences.
	 */
	public String evaluate(WordPredictor predictor,
			ArrayList<ArrayList<Token>> sentences) {
		if (predictor == null) {
			System.err.println("Error, predictor null");
		}

		for (ArrayList<Token> sentence : sentences) {
			List<Token> sentenceBuilder = new ArrayList<Token>();
			// Always have to look at the n+1 word
			int correctWordPosition = 1;
			for (Token token : sentence) {
				// break if reaching the constant max sentence length or if
				// reaching the limit size of this sentence* (* = subtract 1
				// from sentence, because of </s> tag).
				if ((correctWordPosition == MAX_SENTENCE_LENGTH)
						|| (correctWordPosition == sentence.size() - 1)
						|| token.getType().equals(TokenType.END_OF_SENTENCE)) {
					break;
				}
				
				if (token.getType().equals(TokenType.START_OF_SENTENCE)) {
					continue;
				}
				
				sentenceBuilder.add(token);
				ArrayList<String> predictedWords = (ArrayList<String>) predictor
						.predictNextWord(new ArrayList<Token>(sentenceBuilder), false);
				predictionCorrectness(predictedWords, sentence,
						correctWordPosition);
				correctWordPosition++;
			}
			perplexity(model, sentenceBuilder);
		}
		
		return processResults();
	}

	/**
	 * Evaluates the NGram model.
	 */
	public String evaluate(String filename) {
		// train model
		model = trainModel();
		WordPredictor predictor = new WordPredictor(model, NUM_RESULTS);

		// extract testing sentences from test data != training data
		File file = new File(filename);
		ArrayList<ArrayList<Token>> sentences = processFiles(file);

		for (ArrayList<Token> sentence : sentences) {
			List<Token> sentenceBuilder = new ArrayList<Token>();
			// Always have to look at the n+1 word
			int correctWordPosition = 1;
			for (Token token : sentence) {
				// break if reaching the constant max sentence length or if
				// reaching the limit size of this sentence* (* = subtract 1
				// from sentence, because of </s> tag).
				if ((correctWordPosition == MAX_SENTENCE_LENGTH)
						|| (correctWordPosition == sentence.size() - 1)
						|| token.getType().equals(TokenType.END_OF_SENTENCE)) {
					break;
				}
				
				if (token.getType().equals(TokenType.START_OF_SENTENCE)) {
					continue;
				}
				
				sentenceBuilder.add(token);
				ArrayList<String> predictedWords = (ArrayList<String>) predictor
						.predictNextWord(new ArrayList<Token>(sentenceBuilder), false);
				predictionCorrectness(predictedWords, sentence,
						correctWordPosition);
				correctWordPosition++;

			}
			perplexity(model, sentenceBuilder);
		}
		
		return processResults();
	}

	private void perplexity(NGramModel model, List<Token> sentence) {
		
		List<NGram> ngrams = NGramModel.getNgrams(sentence,
				NGramModel.DEFAULT_MAX_NGRAM_LENGTH);
		NGram unigram;
		double probabilities = 1.0; 
		int numWords = 0;
		int index = 0;
		for (Token word : sentence) {
			
			unigram = new NGram(new Token[] { word });
			// the if-else statements is to get the correct NGram from the generated ngrams list.
			// for example P("friend" | "hello", "my")  shall have ngram <"hello my"> and unigram "friend"
			
			double probability = 0.0;
			NGram ngram = null;
			
			if (numWords == 0) {
				ngram = NGram.EMPTY_GRAM;
				probability = model.getProbability(ngram, unigram);
			} else if(numWords == 1){
				ngram = ngrams.get(0);
				probability = model.getProbability(ngram, unigram);
			} else if (numWords == 2){
				ngram = ngrams.get(1);
				probability = model.getProbability(ngram, unigram);
				index = 1;	// dont mind this.
			} else {
				ngram = ngrams.get(index);
				probability = model.getProbability(ngram, unigram);		
			}
			
			probabilities *= probability;
			
			if (Double.isNaN(probabilities)) {
				System.out.println(word);
			}
			
			if (Double.isInfinite(probability)) {
				System.out.println(word);
			}
					
//			System.out.println(perplexityPart);
			index += 3;	// to get the correct NGram from the generated ngrams list.
			numWords++;

		}
//		System.out.println(probabilities);
//		double perplexity = Math.pow(1/perplexityPart, -numWords);
//		System.out.println(perplexity);
		// 2^ (-1.0/numWords * SUM(( Math.log(perplexityPart)/Math.log(2))))
		perplexities.add(new Double(Math.pow(-1.0/numWords * Math.log(probabilities)/Math.log(2), 2.0)));
//		System.out.println(perplexityPart);
		// double perplexity = Math.pow(-1.0/numWords * perplexityPart, 2);

	}

	/**
	 * Test if actual next word equals any of the top K predicted words.
	 * 
	 * Store data (the position of the match in the top K list, the word
	 * position in the sentence matched).
	 * 
	 * @param predictedWords
	 *            Next word predictions.
	 * @param sentence
	 *            The correct sentence.
	 * @param correctWordPosition
	 *            The index of the correct word in the sentence.
	 */
	private void predictionCorrectness(ArrayList<String> predictedWords,
			ArrayList<Token> sentence, int correctWordPosition) {
		testedWords++;
		String correctWord = sentence.get(correctWordPosition+1).toString();
		// System.out.println("correct word: " + correctWord);
//		System.out.println("correct word: " + correctWord);
		for (int i = 0; i < predictedWords.size(); i++) {
			// System.out.println("Predicted: " + predictedWords.get(i));
			if (predictedWords.get(i).equals(correctWord)) {
				// increment count for match at top-list position i.
				countTopResultHit[i]++;
				// increment count for match at word position.
				countWordPositionHit[correctWordPosition]++;
			}
//			System.out.println("guessed word: " + predictedWords.get(i));
		}
	}

	/**
	 * Trains the model with preprocessor run method.
	 * 
	 * @return
	 */
	private NGramModel trainModel() {
		Loader loader = new Loader();
		return loader.load(PreProcessor.FILE_PATH);
	}

	/**
	 * Prints the results to System.out
	 */
	private String processResults() {
		int totalCorrectPredicted = 0;
		StringBuilder sb = new StringBuilder();
		sb.append("Count of hits at specified position in top " + NUM_RESULTS
				+ " predictions:\n");
		for (int i = 0; i < NUM_RESULTS; i++) {
			sb.append("position " + (i + 1) + ": " + countTopResultHit[i]
					+ "\n");
			totalCorrectPredicted += countTopResultHit[i];
		}

		sb.append("\nCount of hits at specified position in the sentences:\n");
		for (int i = 0; i < MAX_SENTENCE_LENGTH; i++) {
			sb.append("word " + i + " in the sentence: "
					+ countWordPositionHit[i] + "\n");
		}

		sb.append("Total tested words: " + testedWords + "\n");
		sb.append("Total successfully predicted words: "
				+ totalCorrectPredicted + "\n");
		sb.append("Hitrate (totalCorrectPredicted/(double)testedWords): "
				+ totalCorrectPredicted / (double) testedWords + "\n");
		double perplexitySum = 0;
		int size = perplexities.size();
		for(double perplexity : perplexities){
//			System.out.println(perplexitySum);
			if(Double.isNaN(perplexity)){
//				System.out.println("PERPLEXITY IS NAN");
				size--;
				continue;
			}
			perplexitySum += perplexity;
//			System.out.println(perplexity);
		}
//		System.out.println(perplexitySum);
		double averagePerplexity = perplexitySum/perplexities.size();
		sb.append("Perplexity = " + averagePerplexity);
		cleanUp();
		return sb.toString();
	}

	private void cleanUp() {
		model = null;
		userCount = null;
		countTopResultHit = new int[NUM_RESULTS];
		countWordPositionHit = new int[MAX_SENTENCE_LENGTH];
		testedWords = 0;
		perplexities = new LinkedList<Double>();
		System.gc();

	}

	/**
	 * This method is used to extract top k commenting users in the given
	 * file(s). This method works on the full 2.4Gb dump of chatlogs.
	 * 
	 * @param filename
	 *            The file(s) to search for users
	 */
	private void extractTopCommunicatingUsers(String filename) {
		userCount = new HashMap<String, Integer>();
		File file = new File(filename);
		int topK = 100;
		populateUsersMap(file);
		Map<String, Integer> sortedMap = sortByValue(userCount);
		int i = 0;
		for (Map.Entry<String, Integer> entry : sortedMap.entrySet()) {
			if (i == topK) {
				break;
			}
			System.out.println(entry.getKey() + ": " + entry.getValue());
			i++;
		}
	}

	/**
	 * Extracts tokenized sentences from given user in the given file. If
	 * 
	 * @code{file is a directory, all its files and subdirectories are
	 *            recursively processed.
	 * 
	 * @param user
	 *            Extract sentences only from this user format: <user>
	 */
	private ArrayList<ArrayList<Token>> extractUserSentences(String user,
			File file) {
		TextParser parser = new TextParser();
		ArrayList<ArrayList<Token>> listOfSentences = new ArrayList<ArrayList<Token>>();

		// do not try to tokenize fs that cannot be read
		if (file.canRead()) {
			if (file.isDirectory()) {
				String[] fs = file.list();
				// an IO error could occur
				if (fs != null) {
					for (int i = 0; i < fs.length; i++) {
						listOfSentences.addAll(extractUserSentences(user,
								new File(file, fs[i])));
					}
				}
			} else {
				try (BufferedReader br = new BufferedReader(
						new FileReader(file))) {
					String sentence;
					while ((sentence = br.readLine()) != null) {
						if (parser.getUser(sentence).equals(user)) {
							listOfSentences.add((ArrayList<Token>) parser
									.tokenize(sentence));
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return listOfSentences;
	}

	/**
	 * Extracts tokenized sentences from @code{file}. If @code{file} is a
	 * directory, all its files and subdirectories are recursively processed.
	 */
	private ArrayList<ArrayList<Token>> processFiles(File file) {
		TextParser parser = new TextParser();
		ArrayList<ArrayList<Token>> listOfSentences = new ArrayList<ArrayList<Token>>();

		// do not try to tokenize fs that cannot be read
		if (file.canRead()) {
			if (file.isDirectory()) {
				String[] fs = file.list();
				// an IO error could occur
				if (fs != null) {
					for (int i = 0; i < fs.length; i++) {
						listOfSentences.addAll(processFiles(new File(file,
								fs[i])));
					}
				}
			} else {
				try (BufferedReader br = new BufferedReader(
						new FileReader(file))) {
					String sentence;
					while ((sentence = br.readLine()) != null) {
						listOfSentences.add((ArrayList<Token>) parser
								.tokenize(sentence));
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return listOfSentences;
	}

	/**
	 * Writes the string data to the specified filepath.
	 * 
	 * @param data
	 *            The string data to be written
	 * @param filepath
	 *            The filepath to write the file
	 */
	private static void writeToFile(String data, String filepath) {
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(filepath), StandardCharsets.UTF_8))) {
			writer.write(data);
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Extracts all users from file and populates a map with <user,
	 * #occurrences> If @code{file} is a directory, all its files and
	 * subdirectories are recursively processed.
	 * 
	 * @param file
	 */
	private void populateUsersMap(File file) {
		TextParser parser = new TextParser();

		// do not try to tokenize fs that cannot be read
		if (file.canRead()) {
			if (file.isDirectory()) {
				String[] fs = file.list();
				// an IO error could occur
				if (fs != null) {
					for (int i = 0; i < fs.length; i++) {
						populateUsersMap(new File(file, fs[i]));
					}
				}
			} else {
				try (BufferedReader br = new BufferedReader(
						new FileReader(file))) {
					String sentence;
					while ((sentence = br.readLine()) != null) {
						String user = parser.getUser(sentence);
						if (!user.equals("")) {
							Integer count = userCount.get(user);
							if (count == null) {
								userCount.put(user, 1);
							} else {
								userCount.put(user, count + 1);
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Method for sorting a <string, integer> map by the integers.
	 * 
	 * @param unsortedMap
	 *            The unsorted map
	 * @return Returns a sorted TreeMap
	 */
	private TreeMap<String, Integer> sortByValue(
			Map<String, Integer> unsortedMap) {
		ValueComparator vc = new ValueComparator(unsortedMap);
		TreeMap<String, Integer> sortedMap = new TreeMap<String, Integer>(vc);
		sortedMap.putAll(unsortedMap);
		return sortedMap;
	}

}

/**
 * Comparator needed for sorting the map by value.
 * 
 */
class ValueComparator implements Comparator<String> {
	Map<String, Integer> base;

	public ValueComparator(Map<String, Integer> base) {
		this.base = base;
	}

	// Note: this comparator imposes orderings that are inconsistent with
	// equals.
	public int compare(String a, String b) {
		if (base.get(a) >= base.get(b)) {
			return -1;
		} else {
			return 1;
		} // returning 0 would merge keys
	}
}
