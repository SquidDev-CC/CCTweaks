package org.squiddev.cctweaks.api.network;

import org.squiddev.cctweaks.api.CCTweaksAPI;

/**
 * Holds the main network API code
 */
public final class NetworkAPI {
	/**
	 * Get the main registry instance
	 *
	 * @return The network registry
	 */
	public static INetworkRegistry registry() {
		return REGISTRY;
	}

	/**
	 * Get the network helper instance
	 *
	 * @return The network helper instance
	 */
	public static INetworkHelpers helpers() {
		return HELPERS;
	}

	private static final INetworkRegistry REGISTRY = CCTweaksAPI.instance().networkRegistry();
	private static final INetworkHelpers HELPERS = CCTweaksAPI.instance().networkHelpers();
}
