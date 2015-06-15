package org.squiddev.cctweaks.core.network.bridge;

import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.api.network.ISearchLoc;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.core.network.visitor.SafeNetworkVisitorIterable;

import java.util.*;

/**
 * Stores list of network bindings
 */
public final class NetworkBindings {
	public static final String BINDING_NAME = "cctweaks.data.networkBinding";
	private static final Map<UUID, Collection<IWorldPosition>> networks = new HashMap<UUID, Collection<IWorldPosition>>();

	public static Iterable<IWorldPosition> getPositions(UUID id) {
		if (id == null || !Config.Network.WirelessBridge.enabled) return null;

		return networks.get(id);
	}

	public static void addPosition(UUID id, IWorldPosition position) {
		if (id == null || !Config.Network.WirelessBridge.enabled) return;

		Collection<IWorldPosition> positions = networks.get(id);
		if (positions == null) {
			positions = new HashSet<IWorldPosition>();
			networks.put(id, positions);
		}

		if (positions.add(position)) {
			for (ISearchLoc loc : new SafeNetworkVisitorIterable(position)) {
				loc.getNode().networkInvalidated();
			}
		}
	}

	public static void removePosition(UUID id, IWorldPosition position) {
		if (id == null) return;

		Collection<IWorldPosition> positions = networks.get(id);

		if (positions != null && positions.remove(position)) {
			for (ISearchLoc loc : new SafeNetworkVisitorIterable(positions)) {
				loc.getNode().networkInvalidated();
			}
		}
	}
}
