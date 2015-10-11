package aiprojekt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class GrammarChecker {
	private MaxentTagger tagger;
	private static final String REMOVE = "-LSB-_-LRB- ";
	private static final Pattern REMOVE_PATTERN = Pattern.compile(REMOVE);
	private HashMap<String, List<String>> grammarRules = new HashMap<String, List<String>>();
	private static final int MD = 5;
	private static final int VB = 6;
	private static final int DT = 7;
	private static final int NN = 8;
	private static final int VBZ = 12;
	private static final int VBD = 15;
	private static final int RB = 17;
	private static final int PRP = 19;

	public GrammarChecker() {
		tagger = new MaxentTagger("libraries/taggers/english-left3words-distsim.tagger");
	
		List<String> temp = new ArrayList<String>(Arrays.asList(tagger.getTag(MD), tagger.getTag(VBD),tagger.getTag(VB)));
		List<String> temp1 = new ArrayList<String>(Arrays.asList(tagger.getTag(VBZ), tagger.getTag(VBD), tagger.getTag(VB), tagger.getTag(PRP), tagger.getTag(MD)));
		List<String> temp2 = new ArrayList<String>(Arrays.asList(tagger.getTag(VB)));
		List<String> temp3 = new ArrayList<String>(Arrays.asList(tagger.getTag(VBZ), tagger.getTag(VBD)));
		List<String> temp4 = new ArrayList<String>(Arrays.asList(tagger.getTag(VBZ), tagger.getTag(VBD), tagger.getTag(VB)));
		List<String> temp5 = new ArrayList<String>(Arrays.asList(tagger.getTag(PRP)));
		
		grammarRules.put(tagger.getTag(VBZ), temp);
		grammarRules.put(tagger.getTag(DT), temp1);
		grammarRules.put(tagger.getTag(VBD), temp2);
		grammarRules.put(tagger.getTag(VB), temp3);
		grammarRules.put(tagger.getTag(MD), temp4 );
		grammarRules.put(tagger.getTag(NN), temp5);
		
//		for (int i = 0; i<tagger.numTags(); i++) {
//			System.out.println(tagger.getTag(i) + " i:"+ i );
//		}	
	}
	
	public boolean hasCorrectGrammar(NGram writtenGram, NGram proposalGram) {
		String proposal = proposalGram.last().toString();
		String writtenString = writtenGram.last().at(0).toString();
		
		String[] arrayProposal = proposal.split("]"); // Get rid of "]"
		String taggedWritten = tagger.tagString(writtenString);
		String taggedProposal = tagger.tagString(arrayProposal[0]);
		taggedProposal = REMOVE_PATTERN.matcher(taggedProposal).replaceAll("");
		
		taggedWritten = taggedWritten.replaceAll(" ", "");
		String[] arrayWritten = taggedWritten.split("_");
		taggedProposal = taggedProposal.replaceAll(" ", "");
		String[] array = taggedProposal.split("_");

		String lastWrittenWordTag = arrayWritten[arrayWritten.length-1];
		String proposalWordTag = array[array.length-1];
		
		if (arrayWritten[0].equals("<s>")) {
			return true; 
		}
		
		if (grammarRules.containsKey(lastWrittenWordTag)) {
			int numIt = grammarRules.get(lastWrittenWordTag).size(); 
			
			for (int i = 0; i< numIt; i++) {
				if (proposalWordTag.equals(grammarRules.get(lastWrittenWordTag).get(i))) {
					System.out.println(arrayWritten[0] + " " + lastWrittenWordTag + " " + array[0] + " " + proposalWordTag);
					
					return false;
				}
			}
		}
		
		if (lastWrittenWordTag.equals(tagger.getTag(NN)) || proposalWordTag.equals(tagger.getTag(NN))) { // It is ok with two NN's in a row (Nouns)
			return true;
		}
		
		if (lastWrittenWordTag.equals(tagger.getTag(RB)) || proposalWordTag.equals(tagger.getTag(RB))) { // It is ok with two RB's in a row (Nouns)
			return true;
		}
		
		if (lastWrittenWordTag.equals(proposalWordTag)) {
			System.out.println(arrayWritten[0] + " " + lastWrittenWordTag + " " + array[0] + " " + proposalWordTag);
			return false;
		}
		
		return true;
	}
}
