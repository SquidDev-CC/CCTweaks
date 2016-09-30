package org.squiddev.cctweaks.core.network.controller;

import com.google.common.base.Preconditions;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.util.BlockPos;
import org.squiddev.cctweaks.api.UnorderedPair;
import org.squiddev.cctweaks.api.network.INetworkController;
import org.squiddev.cctweaks.api.network.INetworkNode;
import org.squiddev.cctweaks.api.network.IWorldNetworkNode;
import org.squiddev.cctweaks.api.network.Packet;

import java.util.*;

public final class NetworkController implements INetworkController {
	protected Map<String, IPeripheral> peripheralsOnNetwork = new HashMap<String, IPeripheral>();

	protected final Map<INetworkNode, Point> points;

	public NetworkController(INetworkNode node) {
		this(new HashMap<INetworkNode, Point>());
		if (node.getAttachedNetwork() != null) {
			throw new IllegalArgumentException(String.format("%s is already attached to %s", node, node.getAttachedNetwork()));
		}
		assimilateNode(node);

		ControllerValidator.validate(this);
	}

	private NetworkController(Map<INetworkNode, Point> points) {
		Preconditions.checkNotNull(points, "Network points cannot be null");
		this.points = points;

		for (Point point : points.values()) {
			addPoint(point);
		}

		ControllerValidator.validate(this);
	}

	private NetworkController(Map<INetworkNode, Point> points, Map<String, IPeripheral> oldPeripherals) {
		this(points);
		Preconditions.checkNotNull(oldPeripherals, "Old peripheral list cannot be null");

		handleInvalidation(Maps.difference(oldPeripherals, peripheralsOnNetwork));

		ControllerValidator.validate(this);
	}

	//region Controller internals

	/**
	 * Add a point into the network.
	 *
	 * This handles detaching the node, merging peripherals and attaching the node to this controller.
	 *
	 * @param point The point to add
	 */
	private void addPoint(Point point) {
		INetworkNode node = point.node;
		if (node.getAttachedNetwork() != null) {
			point.detachFromNetwork();
		}

		points.put(node, point);
		point.controller = this;

		if (point.peripherals == null) point.refreshPeripherals();
		peripheralsOnNetwork.putAll(point.peripherals);

		point.attachToNetwork(this);
	}

	/**
	 * Get the point for the node or explode if it doesn't exist
	 *
	 * @param node The node to lookup
	 * @return The point
	 * @throws NullPointerException If the point cannot be found.
	 */
	private Point getPoint(INetworkNode node) {
		Preconditions.checkNotNull(node, "Node cannot be null");
		return Preconditions.checkNotNull(points.get(node), "Cannot find point for node %s", node);
	}

	/**
	 * Assimilate a node into the network.
	 *
	 * This handles merging its current network.
	 *
	 * @param newNode The node to merge
	 */
	private void assimilateNode(INetworkNode newNode) {
		INetworkController controller = newNode.getAttachedNetwork();

		if (controller == null) {
			Point newPoint = new Point(newNode, this);
			addPoint(newPoint);
			handleInvalidation(Collections.<String, IPeripheral>emptyMap(), newPoint.peripherals);
		} else if (controller instanceof NetworkController) {
			NetworkController nController = (NetworkController) controller;
			Map<String, IPeripheral> peripherals = new HashMap<String, IPeripheral>();

			for (Point point : nController.points.values()) {
				addPoint(point);
				peripherals.putAll(point.peripherals);
			}

			nController.points.clear();
			nController.peripheralsOnNetwork.clear();

			handleInvalidation(Collections.<String, IPeripheral>emptyMap(), peripherals);
		} else {
			Map<String, IPeripheral> peripherals = new HashMap<String, IPeripheral>();

			for (INetworkNode node : controller.getNodesOnNetwork()) {
				Point point = new Point(node, this);
				addPoint(point);
				peripherals.putAll(point.peripherals);
			}

			for (UnorderedPair<INetworkNode> connection : controller.getNodeConnections()) {
				new Point.Connection(getPoint(connection.x), getPoint(connection.y));
			}

			handleInvalidation(Collections.<String, IPeripheral>emptyMap(), peripherals);
		}
	}

	/**
	 * Handle breaking a network.
	 *
	 * @param networks The networks to split into.
	 */
	private void handleSplit(Collection<Map<INetworkNode, Point>> networks) {
		// If there are no changes, then we just ignore it.
		if (networks.size() <= 1) return;

		points.clear();
		Map<String, IPeripheral> oldPeripherals = peripheralsOnNetwork;
		peripheralsOnNetwork = new HashMap<String, IPeripheral>();

		/*
			It is just easier to split the network, rather than keep one and split the others.
			If we ever keep one network, we should ideally keep the largest one.
		*/
		for (Map<INetworkNode, Point> network : networks) {
			new NetworkController(network, oldPeripherals);
		}
	}

	/**
	 * Invalidate every node on the network
	 *
	 * @param difference The result of {@link Maps#difference(Map, Map)} on oldPeripherals, newPeripherals
	 */
	private void handleInvalidation(MapDifference<String, IPeripheral> difference) {
		handleInvalidation(difference.entriesOnlyOnLeft(), difference.entriesOnlyOnRight());
	}

	private void handleInvalidation(Map<String, IPeripheral> removed, Map<String, IPeripheral> added) {
		for (Point point : points.values()) {
			point.networkInvalidated(removed, added);
		}
	}

	private void removePeripherals(Map<String, IPeripheral> peripherals) {
		for (String name : peripherals.keySet()) {
			peripheralsOnNetwork.remove(name);
		}
		handleInvalidation(Collections.unmodifiableMap(peripherals), Collections.<String, IPeripheral>emptyMap());
	}
	//endregion

	//region INetworkController node access
	@Override
	public void formConnection(INetworkNode existingNode, INetworkNode newNode) {
		Preconditions.checkArgument(existingNode != newNode, "Cannot connect a node to itself");
		Preconditions.checkArgument(points.containsKey(existingNode), "Existing node must be on the network");

		if (!points.containsKey(newNode)) {
			assimilateNode(newNode);
		}

		new Point.Connection(getPoint(existingNode), getPoint(newNode));

		ControllerValidator.validate(this);
	}

	@Override
	public void breakConnection(UnorderedPair<INetworkNode> connection) {
		Point xPoint = getPoint(connection.x);
		Point.Connection pointConnection = new Point.Connection(xPoint, getPoint(connection.y));

		if (!xPoint.connections.contains(pointConnection)) return;

		handleSplit(pointConnection.breakConnection());

		ControllerValidator.validate(this);
	}

	@Override
	public void removeNode(INetworkNode removedNode) {
		Point point = getPoint(removedNode);

		points.remove(removedNode);
		point.detachFromNetwork();
		removePeripherals(point.peripherals);
		handleSplit(point.breakConnections());

		ControllerValidator.validate(this);
	}
	//endregion

	//region INetworkController invalidate
	@Override
	public void invalidateNetwork() {
		Map<String, IPeripheral> oldPeripherals = peripheralsOnNetwork;

		Map<String, IPeripheral> newPeripherals = peripheralsOnNetwork = new HashMap<String, IPeripheral>();
		for (Point point : points.values()) {
			newPeripherals.putAll(point.refreshPeripherals());
		}

		handleInvalidation(Maps.difference(oldPeripherals, newPeripherals));

		ControllerValidator.validate(this);
	}

	@Override
	public void invalidateNode(INetworkNode toInvalidate) {
		Point point = getPoint(toInvalidate);

		Map<String, IPeripheral> oldPeripherals = point.peripherals;
		Map<String, IPeripheral> newPeripherals = point.refreshPeripherals();

		// Remove old peripherals
		for (String name : oldPeripherals.keySet()) {
			peripheralsOnNetwork.remove(name);
		}

		// Add new ones
		peripheralsOnNetwork.putAll(newPeripherals);

		handleInvalidation(Maps.difference(oldPeripherals, newPeripherals));

		ControllerValidator.validate(this);
	}
	//endregion

	//region INetworkController getters
	@Override
	public Set<INetworkNode> getNodesOnNetwork() {
		return Collections.unmodifiableSet(points.keySet());
	}

	@Override
	public Set<UnorderedPair<INetworkNode>> getNodeConnections() {
		Set<UnorderedPair<INetworkNode>> connections = new HashSet<UnorderedPair<INetworkNode>>();
		for (Point point : points.values()) {
			for (Point.Connection connection : point.connections) {
				connections.add(new UnorderedPair<INetworkNode>(point.node, connection.other(point).node));
			}
		}

		return Collections.unmodifiableSet(connections);
	}

	@Override
	public Map<String, IPeripheral> getPeripheralsOnNetwork() {
		return Collections.unmodifiableMap(peripheralsOnNetwork);
	}
	//endregion

	@Override
	public void transmitPacket(INetworkNode start, Packet packet) {
		Point startPoint = getPoint(start);

		Set<Point> received = new HashSet<Point>(points.size());
		Queue<TransmitPoint> transmitTo = new PriorityQueue<TransmitPoint>();
		transmitTo.offer(new TransmitPoint(startPoint, 0));

		TransmitPoint nodePair;
		while ((nodePair = transmitTo.poll()) != null) {
			Point point = nodePair.point;
			if (!received.add(point)) continue;

			// Starting node shouldn't receive its own packet.
			INetworkNode node = point.node;
			if (!node.equals(start)) node.receivePacket(packet, nodePair.distance);

			for (Point.Connection connection : point.connections) {
				Point otherPoint = connection.other(point);
				INetworkNode otherNode = otherPoint.node;
				double distance = nodePair.distance;
				if (node instanceof IWorldNetworkNode && otherNode instanceof IWorldNetworkNode) {
					BlockPos position = ((IWorldNetworkNode) node).getPosition().getPosition();
					BlockPos otherPosition = ((IWorldNetworkNode) otherNode).getPosition().getPosition();

					int dx = position.getX() - otherPosition.getX();
					int dy = position.getY() - otherPosition.getY();
					int dz = position.getZ() - otherPosition.getZ();

					distance += Math.sqrt(dx * dx + dy * dy + dz * dz);
				}
				transmitTo.offer(new TransmitPoint(otherPoint, distance));
			}
		}
	}

	private static class TransmitPoint implements Comparable<TransmitPoint> {
		public final Point point;
		public final double distance;

		public TransmitPoint(Point point, double distance) {
			this.point = point;
			this.distance = distance;
		}

		@Override
		public int compareTo(TransmitPoint other) {
			return Double.compare(this.distance, other.distance);
		}
	}
}
