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
	 * Get the network visitor instance
	 *
	 * @return The network visitor
	 */
	public static INetworkVisitor visitor() {
		return VISITOR;
	}

	private static final INetworkRegistry REGISTRY = CCTweaksAPI.instance().networkRegistry();
	private static final INetworkVisitor VISITOR = CCTweaksAPI.instance().networkVisitor();
}
