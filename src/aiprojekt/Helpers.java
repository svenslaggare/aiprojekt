package aiprojekt;

/**
 * Contains helper methods
 */
public class Helpers {
	/*
	 * To prevent creating instances
	 */
	private Helpers() {
		
	}
	
	/**
	 * Returns the hash code for the given array segment
	 * @param array The array
	 * @param start The start of the segment
	 * @param length The length of the segment
	 */
	public static <T> int arrayHashCode(T[] array, int start, int length) {
		final int prime = 31;
		int result = 1;
		
		for (int i = start; i < start + length; i++) {
			result = prime * result + array[i].hashCode();
		}
		
		return result;
	}
	
	/**
	 * Indicates if the two given array segments are equal
	 * @param array1 The first array
	 * @param start1 The start of the first segment
	 * @param length1 The length of the first segment
	 * @param array2 The second array
	 * @param start2 The start of the second segment
	 * @param length2 The length of the second segment
	 */
	public static <T> boolean arrayEquals(T[] array1, int start1, int length1, T[] array2, int start2, int length2) {	
		if (length1 != length2) {
			return false;
		}
		
		for (int i = 0; i < length1; i++) {
			if (!array1[start1 + i].equals(array2[start2 + i])) {
				return false;
			}
		}
		
		return true;
	}
}
