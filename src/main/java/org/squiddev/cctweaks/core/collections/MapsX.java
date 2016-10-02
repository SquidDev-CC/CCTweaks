package org.squiddev.cctweaks.core.collections;

import com.google.common.base.Equivalence;

import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Maps.newHashMap;

/**
 * Even more map collection classes
 */
public final class MapsX {
	private MapsX() {
	}

	public static <K, V> void removeAll(Map<K, V> original, Set<? extends K> keys) {
		for (K key : keys) {
			original.remove(key);
		}
	}

	public static <K, V> void removeAll(Map<K, V> original, Map<? extends K, ? extends V> toRemove) {
		for (K key : toRemove.keySet()) {
			original.remove(key);
		}
	}

	public static <K, V> void putAll(Map<K, V> original, Map<? extends K, ? extends V> toPut) {
		original.putAll(toPut);
	}

	public static <K, V> MapChanges<K, V> changes(
		Map<? extends K, ? extends V> left,
		Map<? extends K, ? extends V> right
	) {
		return changes(left, right, Equivalence.equals());
	}

	public static <K, V> MapChanges<K, V> changes(
		Map<? extends K, ? extends V> left,
		Map<? extends K, ? extends V> right,
		Equivalence<? super V> valueEquivalence
	) {
		Map<K, V> removed = newHashMap();
		Map<K, V> added = newHashMap(right);

		for (Map.Entry<? extends K, ? extends V> entry : left.entrySet()) {
			K leftKey = entry.getKey();
			V leftValue = entry.getValue();
			if (right.containsKey(leftKey)) {
				V rightValue = added.get(leftKey);
				if (valueEquivalence.equivalent(leftValue, rightValue)) {
					added.remove(leftKey);
				} else {
					removed.put(leftKey, leftValue);
				}
			} else {
				removed.put(leftKey, leftValue);
			}
		}

		return new MapChanges<K, V>(removed, added);
	}
}
