package de.tinloaf.intervaltree;

/**
 * The interval of the entries in the Interval Tree
 * 
 * @author Lukas Barth
 *
 * @param <E> Anything comparable to itself. This will make up the interval borders.
 */
public interface IInterval <E extends Comparable<E>> {
	/**
	 * Returns the begin of the interval.
	 *
	 * @return The begin of the interval
	 */
	public E getBegin();
	
	/**
	 * Returns the end of the interval.
	 * 
	 * @return The end of the interval
	 */
	public E getEnd();
}
