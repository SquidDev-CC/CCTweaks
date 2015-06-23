package org.squiddev.cctweaks.core.utils;

import java.util.Iterator;

/**
 * An iterator that doubles as an iterable. Named in true Java fashion.
 */
public abstract class IterableIterator<T> implements Iterable<T>, Iterator<T> {
	@Override
	public Iterator<T> iterator() {
		return this;
	}

	@Override
	public void remove() {
		throw new IllegalStateException();
	}
}
