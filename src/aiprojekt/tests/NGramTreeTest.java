package aiprojekt.tests;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.Test;

import aiprojekt.NGram;
import aiprojekt.NGramTree;
import aiprojekt.NGramTree.Result;

/**
 * Tests the NGramTree class
 */
public class NGramTreeTest {
	/**
	 * Tests inserting a n-gram into the tree
	 */
	@Test
	public void testInsert() {
		NGram ngram = NGram.fromWords("hello", "my", "name");
		NGramTree tree = NGramTree.rootTree();
		tree.insert(ngram, 2);
		assertEquals(2, tree.find(ngram));
		assertEquals(0, tree.find(NGram.fromWords("hello", "my")));
		assertEquals(0, tree.find(NGram.fromWords("hello")));
		assertEquals(0, tree.find(NGram.fromWords("hello", "hello")));
	}
	
	/**
	 * Tests inserting a n-gram into the tree
	 */
	@Test
	public void testInsert2() {
		NGram ngram1 = NGram.fromWords("hello");
		NGram ngram2 = NGram.fromWords("hello", "my");
		NGram ngram3 = NGram.fromWords("hello", "my", "name");
		NGramTree tree = NGramTree.rootTree();
		
		tree.insert(ngram1, 1);
		tree.insert(ngram2, 1);
		tree.insert(ngram3, 1);
		
		assertEquals(1, tree.find(ngram1));
		assertEquals(1, tree.find(ngram2));
		assertEquals(1, tree.find(ngram3));
		
		assertEquals(0, tree.find(NGram.fromWords("hello", "hello")));
	}
	
	/**
	 * Tests inserting a n-gram into the tree where the n-gram already exists
	 */
	@Test
	public void testInsertAlreadyExists() {
		NGram ngram = NGram.fromWords("hello", "my", "name");
		NGramTree tree = NGramTree.rootTree();
		
		tree.insert(ngram, 2);
		assertEquals(2, tree.find(ngram));
		assertEquals(0, tree.find(NGram.fromWords("hello", "my")));
		assertEquals(0, tree.find(NGram.fromWords("hello")));
		assertEquals(0, tree.find(NGram.fromWords("hello", "hello")));
		
		tree.insert(ngram, 2);
		assertEquals(4, tree.find(ngram));
		assertEquals(0, tree.find(NGram.fromWords("hello", "my")));
		assertEquals(0, tree.find(NGram.fromWords("hello")));
		assertEquals(0, tree.find(NGram.fromWords("hello", "hello")));
	}
	
	/**
	 * Tests inserting a n-gram into the tree where the n-gram already exists
	 */
	@Test
	public void testInsertAlreadyExists2() {
		NGram ngram = NGram.fromWords("hello", "my", "name");
		NGramTree tree = NGramTree.rootTree();
		
		tree.insert(ngram, 2);
		assertEquals(2, tree.find(ngram));
		assertEquals(0, tree.find(NGram.fromWords("hello", "my")));
		assertEquals(0, tree.find(NGram.fromWords("hello")));
		
		tree.insert(NGram.fromWords("hello", "my"), 2);
		assertEquals(2, tree.find(ngram));
		assertEquals(2, tree.find(NGram.fromWords("hello", "my")));
		assertEquals(0, tree.find(NGram.fromWords("hello")));
	}
	
	/**
	 * Sorts the given results
	 * @param results The results
	 */
	private void sortResults(List<NGramTree.Result> results) {
		Collections.sort(results, new Comparator<NGramTree.Result>() {
			@Override
			public int compare(Result x, Result y) {
				return Integer.compare(y.getCount(), x.getCount());
			}			
		});
	}
	
	/**
	 * Tests getting the unigrams
	 */
	@Test
	public void testGetUnigrams() {
		NGramTree tree = NGramTree.rootTree();
		tree.insert(NGram.fromWords("hello", "my"), 1);
		tree.insert(NGram.fromWords("my"), 3);
		tree.insert(NGram.fromWords("name"), 2);
		tree.insert(NGram.fromWords("is"), 1);
		
		List<NGramTree.Result> results = tree.findResults(NGram.EMPTY_GRAM);
		this.sortResults(results);
		
		assertEquals(NGram.fromWords("my"), results.get(0).getNgram());
		assertEquals(NGram.fromWords("name"), results.get(1).getNgram());
		assertEquals(NGram.fromWords("is"), results.get(2).getNgram());
	}	
	
	/**
	 * Tests finding results
	 */
	@Test
	public void testFindResults() {
		NGramTree tree = NGramTree.rootTree();
		tree.insert(NGram.fromWords("hello", "my"), 1);
		tree.insert(NGram.fromWords("hello", "i"), 3);
		
		List<NGramTree.Result> results = tree.findResults(NGram.fromWords("hello"));
		this.sortResults(results);
		
		assertEquals(NGram.fromWords("hello", "i"), results.get(0).getNgram());
		assertEquals(NGram.fromWords("hello", "my"), results.get(1).getNgram());
	}
}