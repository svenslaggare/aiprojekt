package aiprojekt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a n-gram search tree.
 * This tree allows fast lookup to find all the (n+1)-grams that starts with a given n-gram.
 */
public class NGramTree {
	private final NGram current;
	private int count;
	
	private final Map<NGram, NGramTree> children = new HashMap<>();
	
	/**
	 * Creates a new n-gram tree
	 * @param current The current n-gram
	 * @param count The count of the n-gram
	 */
	public NGramTree(NGram current, int count) {
		this.current = current;
		this.count = count;
	}
	
	/**
	 * Adds the given child to current tree
	 * @param ngram The n-gram
	 * @param count The count
	 */
	public void addChild(NGram ngram, int count) {
		if (this.children.containsKey(ngram)) {
			this.children.get(ngram).count += count;
		} else {
			this.children.put(ngram, new NGramTree(ngram, count));
		}
	}
	
	/**
	 * Finds the count for the given n-gram
	 * @param ngram The n-gram
	 */
	public int find(NGram ngram) {
		if (this.current.equals(ngram)) {
			return 0;
		} else if (this.children.containsKey(ngram)) {
			return this.children.get(ngram).count;
		} else {
			NGram first = ngram.first();
			NGram rest = ngram.rest();
			
			if (this.children.containsKey(first)) {
				return this.children.get(first).find(rest);
			}
		}
		
		return 0;
	}
	
	/**
	 * Represents a result
	 */
	public static class Result {
		private final NGram ngram;
		private final int count;
		
		/**
		 * Creates a new result
		 * @param ngram The n-gram
		 * @param count The count
		 */
		public Result(NGram ngram, int count) {
			this.ngram = ngram;
			this.count = count;
		}
		
		/**
		 * Returns the n-gram
		 */
		public NGram getNgram() {
			return ngram;
		}

		/**
		 * Returns the count
		 */
		public int getCount() {
			return count;
		}
		
		@Override
		public String toString() {
			return String.format("{ %s: %s }", this.ngram.toString(), this.getCount() + "");
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + count;
			result = prime * result + ((ngram == null) ? 0 : ngram.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof Result)) {
				return false;
			}
			Result other = (Result) obj;
			if (count != other.count) {
				return false;
			}
			if (ngram == null) {
				if (other.ngram != null) {
					return false;
				}
			} else if (!ngram.equals(other.ngram)) {
				return false;
			}
			return true;
		}
	}
	
	/**
	 * Finds the (n+1)-gram that starts with the given n-gram
	 * @param results The results
	 * @param startGram The start gram
	 * @param ngram The n-gram
	 */
	private void findResults(List<Result> results, NGram startGram, NGram ngram) {
		if (this.current.equals(ngram)) {
			return;
		} else if (this.children.containsKey(ngram)) {
			for (NGramTree subTree : this.children.get(ngram).children.values()) {
				if (subTree.count > 0) {
					results.add(new Result(startGram.append(subTree.current), subTree.count));
				}
			}
		} else {
			NGram first = ngram.first();
			NGram rest = ngram.rest();
			
			if (this.children.containsKey(first)) {
				this.children.get(first).findResults(results, startGram, rest);
			}
		}
	}
	
	/**
	 * Finds the (n+1)-gram that starts with the given n-gram
	 * @param ngram The n-gram
	 * @return The results
	 */
	public List<Result> findResults(NGram ngram) {
		List<Result> results = new ArrayList<>();
		
		if (!ngram.equals(NGram.EMPTY_GRAM)) {
			this.findResults(results, ngram, ngram);
		} else {
			for (NGramTree subTree : this.children.values()) {
				if (subTree.count > 0) {
					results.add(new Result(subTree.current, subTree.count));
				}
			}
		}
		
		return results;
	}
	
	/**
	 * Action for n-gram
	 */
	public interface OnNgramAction {
		/**
		 * Executes the action on the given n-gram
		 * @param ngram The n-gram
		 * @param count The count
		 */
		void execute(NGram ngram, int count);
	}
	
	/**
	 * Finds all the n-grams of the given length
	 * @param action The action to execute on all n-grams
	 * @param startGram The start gram
	 * @param length The length
	 */
	private void findNgrams(OnNgramAction action, NGram startGram, int length) {
		if (length == 1) {
			for (NGramTree tree : this.children.values()) {
				if (tree.count > 0) {
					action.execute(startGram.append(tree.current), tree.count);
				}
			}
		} else {
			for (NGramTree tree : this.children.values()) {
				tree.findNgrams(action, startGram.append(tree.current), length - 1);
			}
		}
	}
	
	/**
	 * Returns all the n-grams of the given length
	 * @param length The length
	 */
	public List<Result> findNgrams(int length) {
		final List<Result> results = new ArrayList<>();
		this.findNgrams(new OnNgramAction() {		
			@Override
			public void execute(NGram ngram, int count) {
				results.add(new Result(ngram, count));
			}
		}, NGram.EMPTY_GRAM, length);
		return results;
	}
	
	/**
	 * Returns the top ranked n-grams of the given length
	 * @param length The length
	 * @param count The count
	 */
	public TopList findTopNgrams(int length, int count) {
		final TopList results = new TopList(count);
		this.findNgrams(new OnNgramAction() {		
			@Override
			public void execute(NGram ngram, int count) {
				results.add(ngram, count);
			}
		}, NGram.EMPTY_GRAM, length);
		return results;
	}
	
	/**
	 * Inserts the given n-gram into the given tree
	 * @param tree The tree
	 * @param ngram The n-gram
	 * @param count The count
	 */
	private static void insert(NGramTree tree, NGram ngram, int count) {
		if (ngram.length() == 0) {
			return;
		} else if (ngram.length() == 1) {
			tree.addChild(ngram, count);
		} else {
			NGram first = ngram.first();
			NGram rest = ngram.rest();
			
			if (!tree.children.containsKey(first)) {
				tree.addChild(first, 0);
			}
			
			insert(tree.children.get(first), rest, count);
		}
	}
	
	/**
	 * Creates a root tree
	 */
	public static NGramTree rootTree() {
		return new NGramTree(NGram.EMPTY_GRAM, 0);
	}
	
	/**
	 * Inserts the given n-gram into the tree
	 * @param ngram The n-gram
	 * @param count The count. If the n-gram already exists, the count is added.
	 */
	public void insert(NGram ngram, int count) {
		insert(this, ngram, count);
	}
	
	/**
	 * Creates a n-gram tree from the given model
	 * @param model The n-gram model
	 */
	public static NGramTree createTree(NGramModel model) {
		List<Map.Entry<NGram, Integer>> ngrams = new ArrayList<>();
		ngrams.addAll(model.getNgrams().entrySet());

		NGramTree rootTree = rootTree();
		
		//Insert each n-gram in the tree.
		for (Map.Entry<NGram, Integer> current : ngrams) {
			NGram ngram = current.getKey();
			int count = current.getValue();
			insert(rootTree, ngram, count);
		}
		
		return rootTree;
	}
}
