package de.tinloaf.intervaltree;

/**
 * This is the interface that must be implemented by anything that should be an entry in an Interval Tree.
 * 
 * @author Lukas Barth
 *
 * @param <E> Anything comparable to itself, this will make up the interval borders.
 */
public interface HasInterval <E extends Comparable<E>> {
	/**
	 * Returns the interval of the entry.
	 * 
	 * @return The interval of the entry.
	 */
	public IInterval<E> getInterval();
}
