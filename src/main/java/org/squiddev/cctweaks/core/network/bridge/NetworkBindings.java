package org.squiddev.cctweaks.core.network.bridge;

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import org.squiddev.cctweaks.api.SingleTypeUnorderedPair;
import org.squiddev.cctweaks.api.network.INetworkNode;
import org.squiddev.cctweaks.api.network.IWorldNetworkNode;
import org.squiddev.cctweaks.core.Config;

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
		if (id == null || !Config.Network.WirelessBridge.enabled) return Collections.emptySet();
		return Collections.unmodifiableSet(networks.get(id));
	}

	public static void addNode(UUID id, IWorldNetworkNode node) {
		if (id == null || !Config.Network.WirelessBridge.enabled) return;

		Set<IWorldNetworkNode> nodes = networks.get(id);
		if (nodes.add(node)) {
			for (IWorldNetworkNode n : nodes) {
				if (!n.equals(node) && n.getAttachedNetwork() != null) {
					n.getAttachedNetwork().formConnection(n, node);
				}
			}
		}
	}

	public static void removeNode(UUID id, IWorldNetworkNode node) {
		if (id == null) return;

		Set<IWorldNetworkNode> nodes = networks.get(id);
		if (nodes.remove(node)) {
			if (node.getAttachedNetwork() != null) {
				for (IWorldNetworkNode other : nodes) {
					SingleTypeUnorderedPair<INetworkNode> connection = new SingleTypeUnorderedPair<INetworkNode>(node, other);
					if (node.getAttachedNetwork().getNodeConnections().contains(connection)) {
						node.getAttachedNetwork().breakConnection(connection);
					}
				}
			}
		}
	}
}
