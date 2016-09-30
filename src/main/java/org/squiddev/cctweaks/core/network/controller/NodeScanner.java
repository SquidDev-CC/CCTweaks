package org.squiddev.cctweaks.core.network.controller;

import org.squiddev.cctweaks.api.network.INetworkController;
import org.squiddev.cctweaks.api.network.INetworkNode;

import java.util.*;

/**
 * Scans a list of points, putting each group of connected nodes in an individual network.
 */
public class NodeScanner {
	/**
	 * Scan a network and create a series of sub networks, based off connections
	 *
	 * @param controller The parent controller
	 * @param points     The points to start scanning at
	 * @return The created networks
	 */
	public static Collection<Map<INetworkNode, Point>> scanNetwork(INetworkController controller, Point... points) {
		return scanNetwork(controller, Arrays.asList(points));
	}

	/**
	 * Scan a network and create a series of sub networks, based off connections
	 *
	 * @param controller The parent controller
	 * @param points     The points to start scanning at
	 * @return The created networks. Will return an empty list if no changes are needed.
	 */
	public static Collection<Map<INetworkNode, Point>> scanNetwork(INetworkController controller, Collection<Point> points) {
		if (points.size() <= 1) {
			return Collections.emptyList();
		}

		int nodes = controller.getNodesOnNetwork().size();

		HashSet<Point> seen = new HashSet<Point>(nodes);
		HashSet<Point> remainingPoints = new HashSet<Point>(points);
		List<Map<INetworkNode, Point>> networks = new ArrayList<Map<INetworkNode, Point>>();

		boolean first = true;
		for (Point point : points) {
			if (!seen.add(point)) continue;

			remainingPoints.remove(point);

			HashMap<INetworkNode, Point> network = new HashMap<INetworkNode, Point>(nodes);
			networks.add(network);

			Queue<Point> queue = new LinkedList<Point>();
			queue.add(point);

			while ((point = queue.poll()) != null) {
				network.put(point.node, point);

				for (Point.Connection connection : point.connections) {
					Point other = connection.other(point);
					if (seen.add(other)) {
						// If we've only got one network and we don't need to visit any
						// other nodes then we are fine to abort.
						if (first && remainingPoints.remove(other) && remainingPoints.isEmpty()) {
							return Collections.emptyList();
						}

						queue.add(other);
					}
				}
			}

			first = false;
		}

		return networks;
	}
}
