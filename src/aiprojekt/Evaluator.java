package aiprojekt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
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
	private static final int NUM_RESULTS = 10;
	private static final int MAX_SENTENCE_LENGTH = 30;

	private NGramModel model;
	private Map<String, Integer> userCount;
	private int[] countTopResultHit = new int[NUM_RESULTS];
	private int[] countWordPositionHit = new int[MAX_SENTENCE_LENGTH];
	private int testedWords = 0;

	public static void main(String[] args) {
		// Used for extracting most frequent users
		// Evaluator userAdaptation = new Evaluator();
		// userAdaptation.extractTopCommunicatingUsers(PreProcessor.BIG_DUMP_LOGS_PATH);	// path to all training data

		Evaluator evaluator = new Evaluator();
		evaluator.evaluate();

	}

	/**
	 * Evaluates the NGram model.
	 */
	public void evaluate() {
		// train model
		model = trainModel();
		WordPredictor predictor = new WordPredictor(model, NUM_RESULTS);

		// extract testing sentences from test data != training data
		File file = new File(EVALUATE_FILE);
		ArrayList<ArrayList<Token>> sentences = processFiles(file);

		for (ArrayList<Token> sentence : sentences) {
			List<Token> sentenceBuilder = new ArrayList<Token>();
			// Always have to look at the n+1 word
			int correctWordPosition = 1;
			for (Token token : sentence) {
				// break if reaching the constant max sentence length or if
				// reaching the limit size of this sentence* (* = subtract 1
				// from sentence, because of </s> tag).
				if ((correctWordPosition == MAX_SENTENCE_LENGTH + 1)
						|| (correctWordPosition == sentence.size() - 1)) {
					break;
				}
				sentenceBuilder.add(token);
				ArrayList<String> predictedWords = (ArrayList<String>) predictor
						.predictNextWord(sentenceBuilder, false);
				predictionCorrectness(predictedWords, sentence,
						correctWordPosition);
				correctWordPosition++;
			}
		}
		processResults();
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
		String correctWord = sentence.get(correctWordPosition).toString();
		// System.out.println("correct word: " + correctWord);
		for (int i = 0; i < predictedWords.size(); i++) {
			// System.out.println("Predicted: " + predictedWords.get(i));
			if (predictedWords.get(i).equals(correctWord)) {
				// increment count for match at top-list position i.
				countTopResultHit[i]++;
				// increment count for match at word position.
				countWordPositionHit[correctWordPosition]++;
			}
		}

	}

	/**
	 * Trains the model with preprocessor run method.
	 * 
	 * @return
	 */
	private NGramModel trainModel() {
		PreProcessor processor = new PreProcessor();
		processor.run();
		return processor.getNgramModel();

	}

	/**
	 * Prints the results to System.out
	 */
	private void processResults() {
		int totalCorrectPredicted = 0;
		System.out.println("Count of hits at specified position in top "
				+ NUM_RESULTS + " predictions:");
		for (int i = 0; i < NUM_RESULTS; i++) {
			System.out.println("position " + (i + 1) + ": "
					+ countTopResultHit[i]);
			totalCorrectPredicted += countTopResultHit[i];
		}

		System.out
				.println("\nCount of hits at specified position in the sentences:");
		for (int i = 0; i < MAX_SENTENCE_LENGTH; i++) {
			System.out.println("word " + i + " in the sentence: "
					+ countWordPositionHit[i]);
		}

		System.out.println("Total tested words: " + testedWords);
		System.out.println("Total successfully predicted words: "
				+ totalCorrectPredicted);
		System.out
				.println("Hitrate (totalCorrectPredicted/(double)testedWords): "
						+ totalCorrectPredicted / (double) testedWords);

	}

	/**
	 * This method is used to extract top k commenting users in the given
	 * file(s). This method works on the full 2.4Gb dump of chatlogs.
	 * 
	 * @param filename
	 *            The file(s) to search for users
	 */
	private void extractTopCommunicatingUsers(String filename) {
		// initial capacity of 1 000 000
		userCount = new HashMap<String, Integer>();
		File file = new File(filename);
		int topK = 10;
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
						processFiles(new File(file, fs[i]));
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
