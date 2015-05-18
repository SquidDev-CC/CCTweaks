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

	private static final INetworkRegistry REGISTRY = CCTweaksAPI.instance().networkRegistry();
}
