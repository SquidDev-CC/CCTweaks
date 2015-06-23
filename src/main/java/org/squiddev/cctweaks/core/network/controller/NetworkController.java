package org.squiddev.cctweaks.core.network.controller;

import com.google.common.base.Preconditions;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import dan200.computercraft.api.peripheral.IPeripheral;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.api.SingleTypeUnorderedPair;
import org.squiddev.cctweaks.api.network.INetworkController;
import org.squiddev.cctweaks.api.network.INetworkNode;
import org.squiddev.cctweaks.api.network.IWorldNetworkNode;
import org.squiddev.cctweaks.api.network.Packet;

import java.util.*;

public class NetworkController implements INetworkController {
	protected Map<String, IPeripheral> peripheralsOnNetwork = new HashMap<String, IPeripheral>();

	protected Map<INetworkNode, Point> points = new HashMap<INetworkNode, Point>();

	public NetworkController(INetworkNode node) {
		this(new HashMap<INetworkNode, Point>());
		assimilateNode(node);

		ControllerValidator.validate(this);
	}

	public NetworkController(Map<INetworkNode, Point> points) {
		this.points = Preconditions.checkNotNull(points, "Network points cannot be null");

		for (Point point : points.values()) {
			addPoint(point);
		}

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
	protected void addPoint(Point point) {
		INetworkNode node = point.node;
		if (node.getAttachedNetwork() != null) {
			node.detachFromNetwork();
		}

		points.put(node, point);
		point.controller = this;

		if (point.peripherals == null) point.peripherals = point.node.getConnectedPeripherals();
		peripheralsOnNetwork.putAll(point.peripherals);

		node.attachToNetwork(this);
	}

	/**
	 * Get the point for the node or explode if it doesn't exist
	 *
	 * @param node The node to lookup
	 * @return The point
	 * @throws NullPointerException If the point cannot be found.
	 */
	protected Point getPoint(INetworkNode node) {
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
	protected void assimilateNode(INetworkNode newNode) {
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

			for (SingleTypeUnorderedPair<INetworkNode> connection : controller.getNodeConnections()) {
				new Point.Connection(getPoint(connection.x), getPoint(connection.y));
			}

			handleInvalidation(Collections.<String, IPeripheral>emptyMap(), peripherals);
		}
	}

	/**
	 * Get the list of new peripherals.
	 *
	 * Ideally we wouldn't ever have to do this,
	 * but there might be a time when we have to.
	 *
	 * @return The new peripherals.
	 */
	protected Map<String, IPeripheral> calculatePeripheralsOnNetwork() {
		Map<String, IPeripheral> peripherals = new HashMap<String, IPeripheral>();
		for (Point point : points.values()) {
			peripherals.putAll(point.peripherals = point.node.getConnectedPeripherals());
		}

		return peripherals;
	}

	/**
	 * Handle breaking a network.
	 *
	 * @param networks The networks to split into.
	 */
	protected void handleSplit(Collection<Map<INetworkNode, Point>> networks) {
		// If there are no changes, then we just ignore it.
		if (networks.size() <= 1) return;

		points.clear();
		peripheralsOnNetwork.clear();

		/*
			It is just easier to split the network, rather than keep one and split the others.
			If we ever keep one network, we should ideally keep the largest one.
		*/
		for (Map<INetworkNode, Point> network : networks) {
			new NetworkController(network);
		}
	}

	/**
	 * Invalidate every node on the network
	 *
	 * @param difference The result of {@link Maps#difference(Map, Map)} on oldPeripherals, newPeripherals
	 */
	protected void handleInvalidation(MapDifference<String, IPeripheral> difference) {
		handleInvalidation(difference.entriesOnlyOnLeft(), difference.entriesOnlyOnRight());
	}

	protected void handleInvalidation(Map<String, IPeripheral> removed, Map<String, IPeripheral> added) {
		for (INetworkNode node : points.keySet()) {
			node.networkInvalidated(removed, added);
		}
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
	public void breakConnection(SingleTypeUnorderedPair<INetworkNode> connection) {
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
		removedNode.detachFromNetwork();
		handleSplit(point.breakConnections());

		ControllerValidator.validate(this);
	}
	//endregion

	//region INetworkController invalidate
	@Override
	public void invalidateNetwork() {
		Map<String, IPeripheral> oldPeripherals = peripheralsOnNetwork;
		peripheralsOnNetwork = calculatePeripheralsOnNetwork();

		handleInvalidation(Maps.difference(oldPeripherals, peripheralsOnNetwork));

		ControllerValidator.validate(this);
	}

	@Override
	public void invalidateNode(INetworkNode toInvalidate) {
		Point point = getPoint(toInvalidate);

		Map<String, IPeripheral> oldPeripherals = point.peripherals;
		point.peripherals = toInvalidate.getConnectedPeripherals();

		MapDifference<String, IPeripheral> difference = Maps.difference(oldPeripherals, point.peripherals);

		// Remove old peripherals
		for (String name : difference.entriesOnlyOnRight().keySet()) {
			peripheralsOnNetwork.remove(name);
		}

		// Add new ones
		peripheralsOnNetwork.putAll(difference.entriesOnlyOnLeft());

		handleInvalidation(difference);

		ControllerValidator.validate(this);
	}
	//endregion

	//region INetworkController getters
	@Override
	public Set<INetworkNode> getNodesOnNetwork() {
		return Collections.unmodifiableSet(points.keySet());
	}

	@Override
	public Set<SingleTypeUnorderedPair<INetworkNode>> getNodeConnections() {
		// TODO: Optimise me!
		Set<SingleTypeUnorderedPair<INetworkNode>> connections = new HashSet<SingleTypeUnorderedPair<INetworkNode>>();
		for (Point point : points.values()) {
			for (Point.Connection connection : point.connections) {
				connections.add(new SingleTypeUnorderedPair<INetworkNode>(point.node, connection.other(point).node));
			}
		}

		return connections;
	}

	@Override
	public Map<String, IPeripheral> getPeripheralsOnNetwork() {
		return Collections.unmodifiableMap(peripheralsOnNetwork);
	}
	//endregion

	@Override
	public void transmitPacket(INetworkNode start, Packet packet) {
		// TODO: Use a priority queue to ensure the shortest distance is used.
		Point startPoint = getPoint(start);

		Set<Point> received = new HashSet<Point>(points.size());
		Queue<NodeScanner.TransmitPoint> transmitTo = new PriorityQueue<NodeScanner.TransmitPoint>();
		transmitTo.offer(new NodeScanner.TransmitPoint(startPoint, 0));

		NodeScanner.TransmitPoint nodePair;
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
					IWorldPosition position = ((IWorldNetworkNode) node).getPosition();
					IWorldPosition otherPosition = ((IWorldNetworkNode) otherNode).getPosition();

					int dx = position.getX() - otherPosition.getX();
					int dy = position.getY() - otherPosition.getY();
					int dz = position.getZ() - otherPosition.getZ();

					distance += Math.sqrt(dx * dx + dy * dy + dz * dz);
				}
				transmitTo.offer(new NodeScanner.TransmitPoint(otherPoint, distance));
			}
		}
	}
}
