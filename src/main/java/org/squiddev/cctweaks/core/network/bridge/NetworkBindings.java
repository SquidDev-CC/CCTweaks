package org.squiddev.cctweaks.core.network.bridge;

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import org.squiddev.cctweaks.api.network.IWorldNetworkNode;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.core.utils.DebugLogger;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

/**
 * Stores list of network bindings
 */
public final class NetworkBindings {
	public static final String BINDING_NAME = "cctweaks.data.networkBinding";
	private static final SetMultimap<UUID, IWorldNetworkNode> networks = MultimapBuilder.hashKeys().hashSetValues().build();

	public static Set<IWorldNetworkNode> getNodes(UUID id) {
		DebugLogger.debug("Getting nodes");
		if (id == null || !Config.Network.WirelessBridge.enabled) return Collections.emptySet();
		DebugLogger.debug("Getting " + id);
		return networks.get(id);
	}

	public static void addNode(UUID id, IWorldNetworkNode node) {
		DebugLogger.debug("Preparing to add");
		if (id == null || !Config.Network.WirelessBridge.enabled) return;

		Set<IWorldNetworkNode> nodes = networks.get(id);
		DebugLogger.debug("Adding " + id);
		if (nodes.add(node)) {
			for (IWorldNetworkNode n : nodes) {
				if (!n.equals(node)) {
					if (n.getAttachedNetwork() != null) n.getAttachedNetwork().formConnection(n, node);
				}
			}
		}
	}

	public static void removeNode(UUID id, IWorldNetworkNode node) {
		DebugLogger.debug("Preparing to remove");
		if (id == null) return;

		DebugLogger.debug("Removing " + id);
		if (networks.remove(id, node)) {
			if (node.getAttachedNetwork() != null) node.getAttachedNetwork().removeNode(node);
		}
	}
}
