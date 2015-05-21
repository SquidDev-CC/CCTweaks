package org.squiddev.cctweaks.core.network.bridge;

import org.squiddev.cctweaks.api.network.IWorldNetworkNode;

import java.util.*;

/**
 * Stores list of network bindings
 */
public final class NetworkBindings {
	public static final String BINDING_NAME = "cctweaks.data.networkBinding";
	private static final Map<UUID, Set<IWorldNetworkNode>> networks = new HashMap<UUID, Set<IWorldNetworkNode>>();

	public static Set<IWorldNetworkNode> getNodes(UUID id) {
		if (id == null) return null;

		return networks.get(id);
	}

	public static void addNode(UUID id, IWorldNetworkNode node) {
		if (id == null) return;

		Set<IWorldNetworkNode> nodes = networks.get(id);
		if (nodes == null) {
			nodes = new HashSet<IWorldNetworkNode>();
			networks.put(id, nodes);
		}

		if (nodes.add(node)) {
			for (IWorldNetworkNode n : nodes) {
				if (!n.equals(node)) {
					n.getAttachedNetwork().formConnection(n, node);
				}
			}
		}
	}

	public static void removeNode(UUID id, IWorldNetworkNode node) {
		if (id == null) return;

		Set<IWorldNetworkNode> nodes = networks.get(id);

		if (nodes != null) {
			node.getAttachedNetwork().removeNode(node);
			nodes.remove(node);
		}
	}
}
