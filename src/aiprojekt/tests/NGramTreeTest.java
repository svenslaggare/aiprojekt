package aiprojekt.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import aiprojekt.NGram;
import aiprojekt.NGramTree;

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
		
		tree.insert(ngram, 2);
		assertEquals(4, tree.find(ngram));
		assertEquals(0, tree.find(NGram.fromWords("hello", "my")));
		assertEquals(0, tree.find(NGram.fromWords("hello")));
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
}
