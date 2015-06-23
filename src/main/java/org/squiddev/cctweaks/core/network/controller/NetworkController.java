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
	public Map<String, IPeripheral> peripheralsOnNetwork = new HashMap<String, IPeripheral>();

	public Map<INetworkNode, Point> points = new HashMap<INetworkNode, Point>();

	public NetworkController(INetworkNode node) {
		this(new HashMap<INetworkNode, Point>());
		assimilateNode(node);
	}

	public NetworkController(Map<INetworkNode, Point> points) {
		this.points = Preconditions.checkNotNull(points, "Network points cannot be null");

		for (Point point : points.values()) {
			addPoint(point);
		}
	}

	private void addPoint(Point point) {
		INetworkNode node = point.node;
		if (node.getAttachedNetwork() != null) {
			node.detachFromNetwork();
		}

		points.put(node, point);
		point.controller = this;
		peripheralsOnNetwork.putAll(point.peripherals);

		node.attachToNetwork(this);
	}

	protected Point getPoint(INetworkNode node) {
		Preconditions.checkNotNull(node, "Node cannot be null");
		return Preconditions.checkNotNull(points.get(node), "Cannot find point for node %s", node);
	}

	private void assimilateNode(INetworkNode newNode) {
		INetworkController controller = newNode.getAttachedNetwork();

		addPoint(new Point(newNode, this));

		if (controller == null) return;

		if (controller instanceof NetworkController) {
			NetworkController nController = (NetworkController) controller;
			for (Point point : nController.points.values()) {
				addPoint(point);
			}
			nController.points.clear();

			newNode.detachFromNetwork();
			newNode.attachToNetwork(this);
		} else {
			for (INetworkNode node : controller.getNodesOnNetwork()) {
				node.detachFromNetwork();
			}
			for (INetworkNode node : controller.getNodesOnNetwork()) {
				node.attachToNetwork(this);
			}

			// TODO: Add a way of merging other networks
			throw new IllegalStateException("Cannot handle non-NetworkController controller");
		}
	}

	private Map<String, IPeripheral> calculatePeripheralsOnNetwork() {
		Map<String, IPeripheral> peripherals = new HashMap<String, IPeripheral>();
		for (Point point : points.values()) {
			peripherals.putAll(point.peripherals = point.node.getConnectedPeripherals());
		}

		return peripherals;
	}

	public void handleSplit(Collection<Map<INetworkNode, Point>> networks) {
		// If there are no changes, then we just ignore it.
		if (networks.size() <= 1) return;

		for (Map<INetworkNode, Point> network : networks) {
			new NetworkController(network);
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
	}

	@Override
	public void breakConnection(SingleTypeUnorderedPair<INetworkNode> connection) {
		Point xPoint = getPoint(connection.x);
		Point.Connection pointConnection = new Point.Connection(xPoint, getPoint(connection.y));

		if (!xPoint.connections.contains(pointConnection)) return;

		handleSplit(pointConnection.remove());
	}

	@Override
	public void removeNode(INetworkNode removedNode) {
		Point point = getPoint(removedNode);
		removedNode.detachFromNetwork();
		handleSplit(point.remove());
	}
	//endregion

	//region INetworkController invalidate
	@Override
	public void invalidateNetwork() {
		Map<String, IPeripheral> oldPeripherals = peripheralsOnNetwork;
		peripheralsOnNetwork = calculatePeripheralsOnNetwork();

		MapDifference<String, IPeripheral> difference = Maps.difference(peripheralsOnNetwork, oldPeripherals);
		for (INetworkNode node : points.keySet()) {
			node.networkInvalidated(difference.entriesOnlyOnRight(), difference.entriesOnlyOnLeft());
		}
	}

	@Override
	public void invalidateNode(INetworkNode toInvalidate) {
		Point point = getPoint(toInvalidate);

		Map<String, IPeripheral> oldPeripherals = point.peripherals;
		point.peripherals = toInvalidate.getConnectedPeripherals();

		MapDifference<String, IPeripheral> difference = Maps.difference(point.peripherals, oldPeripherals);

		// Remove old peripherals
		for (String name : difference.entriesOnlyOnRight().keySet()) {
			peripheralsOnNetwork.remove(name);
		}

		// Add new ones
		for (Map.Entry<String, IPeripheral> peripheral : difference.entriesOnlyOnLeft().entrySet()) {
			peripheralsOnNetwork.put(peripheral.getKey(), peripheral.getValue());
		}

		for (INetworkNode node : points.keySet()) {
			node.networkInvalidated(difference.entriesOnlyOnRight(), difference.entriesOnlyOnLeft());
		}
	}
	//endregion

	//region INetworkController access
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
