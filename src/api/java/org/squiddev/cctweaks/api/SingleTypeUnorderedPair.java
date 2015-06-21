package org.squiddev.cctweaks.api;

/**
 * An unordered pair of objects with the same type.
 *
 * @param <T> Object type
 */
public class SingleTypeUnorderedPair<T> extends UnorderedPair<T, T> {
	public SingleTypeUnorderedPair(T t, T t2) {
		super(t, t2);
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
}
