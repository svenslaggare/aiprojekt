package aiprojekt.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

import aiprojekt.NGram;
import aiprojekt.NGramTree;
import aiprojekt.NGramTree.Result;
import aiprojekt.TopList;

/**
 * Tests the top list container
 * @author Anton Jansson
 *
 */
public class TopListTest {
	/**
	 * Sorts the given data by count
	 * @param data The data
	 */
	private NGramTree.Result[] sortByCount(NGramTree.Result[] data) {
		Arrays.sort(data, new Comparator<NGramTree.Result>() {
			@Override
			public int compare(Result x, Result y) {
				return y.getCount() - x.getCount();
			}
		});
		
		return data;
	}
	
	/**
	 * Returns the given amount of data
	 */
	private List<NGramTree.Result> getData(Set<NGramTree.Result> data, int count) {
		List<NGramTree.Result> list = new ArrayList<>();
		
		int i = 0;
		for (NGramTree.Result current : data) {			
			list.add(current);
			i++;
			
			if (i >= count) {
				break;
			}
		}
		
		return list;
	}
	
	/**
	 * Tests the container
	 */
	@Test
	public void testContainer() {
		NGram n1 = NGram.fromWords("1");
		NGram n2 = NGram.fromWords("2");
		NGram n3 = NGram.fromWords("3");
		NGram n4 = NGram.fromWords("4");
		NGram n5 = NGram.fromWords("5");
		NGram n6 = NGram.fromWords("6");
		
		TopList topList = new TopList(4);
		topList.add(n1, 1);
		topList.add(n2, 2);
		topList.add(n3, 3);
		topList.add(n4, 4);
		
		assertEquals(4, topList.size());
		assertArrayEquals(
			new NGramTree.Result[] {
					new NGramTree.Result(n4, 4), new NGramTree.Result(n3, 3),
					new NGramTree.Result(n2, 2), new NGramTree.Result(n1, 1)
				},
			sortByCount(topList.getItems()));
		
		topList.add(n5, 5);
		topList.add(n6, 6);
		
		assertEquals(4, topList.size());
		assertArrayEquals(
			new NGramTree.Result[] {
					new NGramTree.Result(n6, 6), new NGramTree.Result(n5, 5),
					new NGramTree.Result(n4, 4), new NGramTree.Result(n3, 3)
				},
			sortByCount(topList.getItems()));
	}
		
	/**
	 * Tests the container with random data
	 */
	@Test
	public void testContainerRandom() {
		Random random = new Random();
		Set<NGramTree.Result> data = new TreeSet<>(new Comparator<NGramTree.Result>() {
			@Override
			public int compare(Result x, Result y) {
				return y.getCount() - x.getCount();
			}
		});
		
		TopList topList = new TopList(50);
		
		for (int i = 0; i < 1000; i++) {
			int x = random.nextInt(1000000);
			NGram ngram = NGram.fromWords(i + "");
			data.add(new NGramTree.Result(ngram, x));
			topList.add(ngram, x);
		}
		
		assertArrayEquals(sortByCount(topList.getItems()), getData(data, topList.size()).toArray());
	}
}
