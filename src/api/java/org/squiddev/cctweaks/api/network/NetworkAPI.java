package org.squiddev.cctweaks.api.network;

import org.squiddev.cctweaks.api.CoreNotFoundException;

/**
 * Holds the main network API code
 */
public class NetworkAPI {
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

	private static final INetworkRegistry REGISTRY;

	private static final INetworkVisitor VISITOR;

	static {
		String rootNamespace = "org.squiddev.cctweaks.core.network.";

		INetworkRegistry registry;
		String registryName = rootNamespace + "NetworkRegistry";
		try {
			Class<?> registryClass = Class.forName(registryName);
			registry = (INetworkRegistry) registryClass.newInstance();
		} catch (ClassNotFoundException e) {
			throw new CoreNotFoundException(e, registryName);
		} catch (InstantiationException e) {
			throw new CoreNotFoundException(e, registryName);
		} catch (IllegalAccessException e) {
			throw new CoreNotFoundException(e, registryName);
		}
		REGISTRY = registry;

		INetworkVisitor visitor;
		String visitorName = rootNamespace + "NetworkVisitor";
		try {
			Class<?> visitorClass = Class.forName(visitorName);
			visitor = (INetworkVisitor) visitorClass.newInstance();
		} catch (ClassNotFoundException e) {
			throw new CoreNotFoundException(e, visitorName);
		} catch (InstantiationException e) {
			throw new CoreNotFoundException(e, visitorName);
		} catch (IllegalAccessException e) {
			throw new CoreNotFoundException(e, visitorName);
		}
		VISITOR = visitor;
	}
}
