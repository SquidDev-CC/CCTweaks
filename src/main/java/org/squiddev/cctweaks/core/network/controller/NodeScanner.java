package org.squiddev.cctweaks.core.network.controller;

import org.squiddev.cctweaks.api.network.INetworkNode;

import java.util.*;

/**
 * Scans a list of points, putting each group of connected nodes in an individual network.
 */
public class NodeScanner {
	/**
	 * Scan a network and create a series of sub networks, based off connections
	 *
	 * @param points The points to start scanning at
	 * @return The created networks
	 */
	public static Collection<Map<INetworkNode, Point>> scanNetwork(Point... points) {
		return scanNetwork(Arrays.asList(points));
	}

	/**
	 * Scan a network and create a series of sub networks, based off connections
	 *
	 * @param points The points to start scanning at
	 * @return The created networks
	 */
	public static Collection<Map<INetworkNode, Point>> scanNetwork(Iterable<Point> points) {
		Set<Point> seen = new HashSet<Point>();
		List<Map<INetworkNode, Point>> networks = new ArrayList<Map<INetworkNode, Point>>();

		for (Point point : points) {
			if (seen.contains(point)) continue;

			Map<INetworkNode, Point> network = new HashMap<INetworkNode, Point>();
			networks.add(network);

			Queue<Point> queue = new LinkedList<Point>();
			queue.add(point);

			while ((point = queue.poll()) != null) {
				if (!seen.add(point)) continue;

				network.put(point.node, point);

				for (Point.Connection connection : point.connections) {
					queue.add(connection.other(point));
				}
			}
		}

		return networks;
	}
}
