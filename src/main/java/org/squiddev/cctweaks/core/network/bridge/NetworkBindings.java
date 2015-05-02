package org.squiddev.cctweaks.core.network.bridge;

import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.api.network.INetworkNode;
import org.squiddev.cctweaks.api.network.NetworkVisitor;
import org.squiddev.cctweaks.core.network.SafeNetworkVisitor;

import java.util.*;

/**
 * Stores list of network bindings
 */
public final class NetworkBindings {
	public static final String BINDING_NAME = "cctweaks.data.networkBinding";
	private static final Map<UUID, Collection<IWorldPosition>> networks = new HashMap<UUID, Collection<IWorldPosition>>();

	private static final SafeNetworkVisitor invalidateVisitor = new SafeNetworkVisitor() {
		public void visitNode(INetworkNode node, int distance) {
			synchronized (node.lock()) {
				node.networkInvalidated();
			}
		}
	};

	public static Iterable<IWorldPosition> getPositions(UUID id) {
		if (id == null) return null;

		return networks.get(id);
	}

	public static void addPosition(UUID id, IWorldPosition position) {
		if (id == null) return;

		Collection<IWorldPosition> positions = networks.get(id);
		if (positions == null) {
			positions = new HashSet<IWorldPosition>();
			networks.put(id, positions);
		}

		if (positions.add(position)) {
			invalidateVisitor.visitNetwork(position.getWorld(), position.getX(), position.getY(), position.getZ());
		}
	}

	public static void removePosition(UUID id, IWorldPosition position) {
		if (id == null) return;

		Collection<IWorldPosition> positions = networks.get(id);

		if (positions != null && positions.remove(position)) {
			Set<NetworkVisitor.SearchLoc> visited = new HashSet<NetworkVisitor.SearchLoc>();
			for (IWorldPosition newPos : positions) {
				// Invalidate the entire network
				invalidateVisitor.visitNetwork(newPos.getWorld(), newPos.getX(), newPos.getY(), newPos.getZ(), visited);
			}
		}
	}
}
