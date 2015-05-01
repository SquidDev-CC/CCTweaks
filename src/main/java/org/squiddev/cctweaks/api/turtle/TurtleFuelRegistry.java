package org.squiddev.cctweaks.api.turtle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The registry for refuel sources
 *
 * @see ITurtleFuelProvider
 */
public class TurtleFuelRegistry {
	private static final List<ITurtleFuelProvider> internalRefuelList = new ArrayList<ITurtleFuelProvider>();
	public static final List<ITurtleFuelProvider> refuelList = Collections.unmodifiableList(internalRefuelList);

	/**
	 * Add a fuel provider.
	 *
	 * Ideally this should be done in the init stages of a mod, though it doesn't matter too much.
	 *
	 * @param provider The fuel provider to register with
	 */
	public static void addFuelProvider(ITurtleFuelProvider provider) {
		internalRefuelList.add(provider);
	}
}
