package org.squiddev.cctweaks.api;

public class UnorderedPair<X, Y> {
	public final X x;
	public final Y y;

	public UnorderedPair(X x, Y y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Determines if an object is one of this pair's elements.
	 *
	 * @param z An object to test against.
	 * @return If {@link #x} or {@link #y} {@link #equals(Object)} the passed object.
	 */
	public boolean contains(Object z) {
		return x.equals(z) || y.equals(z);
	}

	/**
	 * Determine equality even if the other object has a different order.
	 */
	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}
		if (other instanceof UnorderedPair) {
			UnorderedPair pair = (UnorderedPair) other;
			if (x.equals(pair.x) && y.equals(pair.y)) {
				return true;
			} else if (y.equals(pair.x) && x.equals(pair.y)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Symmetric hashcode.
	 */
	@Override
	public int hashCode() {
		return x.hashCode() ^ y.hashCode();
	}
}
