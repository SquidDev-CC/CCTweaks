package org.squiddev.cctweaks.core.collections;

import java.util.Map;

import static java.util.Collections.unmodifiableMap;

/**
 * Represents keys and values removed/added from pairs
 */
public final class MapChanges<K, V> {
	private final Map<K, V> removed;
	private final Map<K, V> added;

	public MapChanges(Map<K, V> removed, Map<K, V> added) {
		this.removed = unmodifiableMap(removed);
		this.added = unmodifiableMap(added);
	}

	public Map<K, V> removed() {
		return removed;
	}

	public Map<K, V> added() {
		return added;
	}

	public void apply(Map<K, V> map) {
		for (K entry : removed().keySet()) {
			map.remove(entry);
		}
		map.putAll(added);
	}
}
