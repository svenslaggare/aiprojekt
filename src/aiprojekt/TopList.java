package aiprojekt;

import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

import aiprojekt.NGramTree.Result;

/**
 * Represents a container for just holding the best results
 */
public class TopList implements Iterable<NGramTree.Result> {
	private final PriorityQueue<NGramTree.Result> container;
	private final int maxSize;
	
	/**
	 * Creates a new top list of the given size
	 * @param size The maximum size
	 */
	public TopList(int size) {
		this.maxSize = size;
		this.container = new PriorityQueue<>(new Comparator<NGramTree.Result>() {
			@Override
			public int compare(Result x, Result y) {
				return x.getCount() - y.getCount();
			}			
		});
	}
	
	/**
	 * Returns the number of elements in the container
	 */
	public int size() {
		return this.container.size();
	}
	
	/**
	 * Tries to add the given n-gram to the list. A n-gram will only be added if the count > min count in the container.
	 * @param ngram The n-gram
	 * @param count The count
	 */
	public void add(NGram ngram, int count) {
		if (this.container.size() < this.maxSize) {
			this.container.add(new NGramTree.Result(ngram, count));
		} else {
			if (count > this.container.peek().getCount()) {
				this.container.remove();
				this.container.add(new NGramTree.Result(ngram, count));
			}
		}
	}
	
	/**
	 * Returns an iterator to the elements
	 */
	public Iterator<NGramTree.Result> iterator() {
		return this.container.iterator();
	}
	
	/**
	 * Returns the items in the container
	 */
	public NGramTree.Result[] getItems() {
		return this.container.toArray(new NGramTree.Result[this.container.size()]);
	}
}
