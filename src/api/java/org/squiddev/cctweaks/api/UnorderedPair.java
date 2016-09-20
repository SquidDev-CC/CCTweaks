package org.squiddev.cctweaks.api;

/**
 * An unordered pair of objects with the same type.
 *
 * @param <T> Object type
 */
public class UnorderedPair<T> {
	public final T x;
	public final T y;

	public UnorderedPair(T x, T y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Gets the other object in the pair.
	 *
	 * @param obj The object that you don't want from this pair.
	 * @return If obj is {@link #x}, returns {@link #y}. Else if obj is {@link #y}, returns {@link #x}. Else returns null.
	 */
	public T other(T obj) {
		if (obj.equals(x)) {
			return y;
		} else if (obj.equals(y)) {
			return x;
		} else {
			return null;
		}
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
			UnorderedPair<?> pair = (UnorderedPair) other;
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

	@Override
	public String toString() {
		return String.format("<%s, %s>", x, y);
	}
}
