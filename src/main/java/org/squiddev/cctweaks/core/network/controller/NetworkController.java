package org.squiddev.cctweaks.core.network.controller;

import com.google.common.base.Preconditions;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.util.BlockPos;
import org.squiddev.cctweaks.api.UnorderedPair;
import org.squiddev.cctweaks.api.network.INetworkController;
import org.squiddev.cctweaks.api.network.INetworkNode;
import org.squiddev.cctweaks.api.network.IWorldNetworkNode;
import org.squiddev.cctweaks.api.network.Packet;
import org.squiddev.cctweaks.core.collections.MapChanges;
import org.squiddev.cctweaks.core.collections.MapsX;

import java.util.*;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newHashSetWithExpectedSize;
import static java.util.Collections.*;

public final class NetworkController implements INetworkController {
	private Map<String, IPeripheral> peripherals = newHashMap();

	protected final Map<INetworkNode, Point> points;
	private Set<INetworkNode> nodeView;

	/**
	 * Construct a new network with a single node
	 *
	 * The node should not currently be attached to a network.
	 *
	 * @param node The node to construct it from.
	 */
	public NetworkController(INetworkNode node) {
		if (node.getAttachedNetwork() != null) {
			throw new IllegalArgumentException(String.format("%s is already attached to %s", node, node.getAttachedNetwork()));
		}

		points = newHashMap();

		Point point = new Point(node);
		points.put(node, point);
		point.attachToNetwork(this);
		peripheralsUpdate(point.refreshPeripherals(), null);

		ControllerValidator.validate(this);
	}

	/**
	 * Construct a new network from an existing set of nodes and points.
	 *
	 * These points will probably be already attached to a network.
	 *
	 * @param points         The node to point lookup map
	 * @param oldPeripherals The peripherals on the original network
	 */
	private NetworkController(Map<INetworkNode, Point> points, Map<String, IPeripheral> oldPeripherals) {
		Preconditions.checkNotNull(points, "Network points cannot be null");
		Preconditions.checkNotNull(oldPeripherals, "Old peripheral list cannot be null");

		this.points = points;

		// Add all points.
		Map<String, IPeripheral> peripherals = this.peripherals;
		for (Point point : points.values()) {
			INetworkNode node = point.node;
			if (node.getAttachedNetwork() != null) point.detachFromNetwork();

			point.attachToNetwork(this);
			peripherals.putAll(point.peripherals);
		}

		// Update peripherals
		peripheralsInvalidated(MapsX.changes(oldPeripherals, peripherals), null);

		ControllerValidator.validate(this);
	}

	//region Controller internals

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
	 * Handle breaking a network.
	 *
	 * @param networks The networks to split into.
	 */
	private void handleSplit(Collection<Map<INetworkNode, Point>> networks) {
		// If there are no changes, then we just ignore it.
		if (networks.size() <= 1) return;

		Map<String, IPeripheral> peripheralMap = unmodifiableMap(peripherals);

		// Clear the structure. Make it impossible to add peripherals
		points.clear();
		peripherals = newHashMap();

		/*
			It is just easier to split the network, rather than keep one and split the others.
			If we ever keep one network, we should ideally keep the largest one.
		*/
		for (Map<INetworkNode, Point> network : networks) {
			new NetworkController(network, peripheralMap);
		}
	}
	//endregion

	//region Peripheral handling

	/**
	 * Update the controller's peripherals
	 *
	 * This will update the controller's peripheral map and notify the network.
	 *
	 * @param removed All peripherals which were removed
	 * @param added   All peripherals which were added
	 * @param exclude Set of points to exclude
	 */
	private void peripheralsUpdate(Map<String, IPeripheral> removed, Map<String, IPeripheral> added, Set<Point> exclude) {
		for (String entry : removed.keySet()) {
			peripherals.remove(entry);
		}
		peripherals.putAll(added);

		peripheralsInvalidated(removed, added, exclude);
	}

	/**
	 * Update the controller's peripherals
	 *
	 * This will update the controller's peripheral map and notify the network.
	 *
	 * @param difference Grouped set of peripherals which were removed and added
	 * @param exclude    Set of points to exclude
	 */
	private void peripheralsUpdate(MapChanges<String, IPeripheral> difference, Set<Point> exclude) {
		difference.apply(peripherals);
		peripheralsInvalidated(difference, exclude);
	}

	/**
	 * Notify the network of a peripheral change.
	 *
	 * This will NOT update the controller's peripheral map.
	 *
	 * @param difference Grouped set of peripherals which were removed and added
	 * @param exclude    Set of points to exclude
	 */
	private void peripheralsInvalidated(MapChanges<String, IPeripheral> difference, Set<Point> exclude) {
		peripheralsInvalidated(difference.removed(), difference.added(), exclude);
	}

	/**
	 * Notify the network of a peripheral change.
	 *
	 * This will NOT update the controller's peripheral map.
	 *
	 * @param removed All peripherals which were removed
	 * @param added   All peripherals which were added
	 * @param exclude Set of points to exclude
	 */
	private void peripheralsInvalidated(Map<String, IPeripheral> removed, Map<String, IPeripheral> added, Set<Point> exclude) {
		if (removed.size() == 0 && added.size() == 0) return;

		removed = unmodifiableMap(removed);
		added = unmodifiableMap(added);

		for (Point point : points.values()) {
			if (exclude == null || !exclude.contains(point)) {
				point.networkInvalidated(removed, added);
			}
		}
	}
	//endregion

	//region INetworkController node access
	@Override
	public void formConnection(INetworkNode existingNode, INetworkNode newNode) {
		Preconditions.checkArgument(existingNode != newNode, "Cannot connect a node to itself");
		Preconditions.checkArgument(points.containsKey(existingNode), "Existing node must be on the network");

		Point existingPoint = getPoint(existingNode);

		// Gosh. This is ugly. Well, here goes.

		if (points.containsKey(newNode)) {
			// If we've already got the point then just form the connection
			new Point.Connection(existingPoint, getPoint(newNode));
		} else {
			// We're going to have to add another node. Let's see what happens
			INetworkController controller = newNode.getAttachedNetwork();

			/*
			 * Detach every node on newNode's network
			 * Form connection between {@code existingNode} and {@code newNode}
			 * Attach every node on newNode's network
			 * Call networkInvalidated on the two newly-joined networks with the relevant peripherals
			 */

			// We handle network invalidation and attaching at the end so we prep some stuff here.
			// Added will be given a value when merging existing points.
			Set<Point> pointSet;
			Map<String, IPeripheral> removed;
			Map<String, IPeripheral> added = null;

			if (controller == null) {
				// Well, this is simple: create a new point and add it to the map
				Point point = new Point(newNode);
				points.put(newNode, point);
				pointSet = Collections.singleton(point);
			} else if (controller instanceof NetworkController) {
				// So we can reuse points here which is nice.
				NetworkController oldController = (NetworkController) controller;
				pointSet = newHashSet(oldController.points.values());

				added = newHashMap();

				// We just detach, add them to the map and store new peripherals
				for (Point point : pointSet) {
					point.detachFromNetwork();
					points.put(point.node, point);
					added.putAll(point.peripherals);
				}

				// The connections already exist so we're fine to clear this
				oldController.points.clear();
				oldController.peripherals.clear();
			} else {
				// Copy nodes and connections as they will be modified
				List<INetworkNode> nodes = newArrayList(controller.getNodesOnNetwork());
				List<UnorderedPair<INetworkNode>> connections = newArrayList(controller.getNodeConnections());
				pointSet = newHashSetWithExpectedSize(nodes.size());

				// Create new points and store them
				for (INetworkNode node : nodes) {
					Point point = new Point(node);
					point.detachFromNetwork();
					pointSet.add(point);
					points.put(node, point);
				}

				// Re-build connection map
				for (UnorderedPair<INetworkNode> connection : connections) {
					new Point.Connection(getPoint(connection.x), getPoint(connection.y));
				}
			}

			// Form the connection
			new Point.Connection(existingPoint, getPoint(newNode));

			// And attach everything to the network
			for (Point point : pointSet) {
				point.attachToNetwork(this);
			}

			if (added == null) {
				// We'll have to refresh peripherals, build some maps and do just that
				removed = newHashMap();
				added = newHashMap();

				for (Point point : pointSet) {
					MapChanges<String, IPeripheral> changes = point.refreshPeripherals();
					removed.putAll(changes.removed());
					added.putAll(changes.added());
				}
			} else {
				// We've already got peripherals so setup the empty map
				removed = emptyMap();
			}

			// Cache the existing peripheral map if required
			Map<String, IPeripheral> subAdded = peripherals.size() > 0 ? newHashMap(peripherals) : null;

			// And fire the peripheral update on the global network (excluding new points)
			peripheralsUpdate(removed, added, pointSet);

			// Fire the peripheral update on the added network
			if (subAdded != null) {
				Map<String, IPeripheral> subRemoved = emptyMap();
				for (Point point : pointSet) {
					point.networkInvalidated(subRemoved, subAdded);
				}
			}
		}

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

		if (peripherals.size() > 0) {
			point.networkInvalidated(unmodifiableMap(peripherals), Collections.<String, IPeripheral>emptyMap());
		}
		point.detachFromNetwork();

		handleSplit(NodeScanner.scanNetwork(this, point.breakConnections()));

		// We need to do this as this network might not be cleared.
		// If it is then this won't do anything
		peripheralsUpdate(point.peripherals, Collections.<String, IPeripheral>emptyMap(), null);

		ControllerValidator.validate(this);
	}
	//endregion

	//region INetworkController invalidate
	@Override
	public void invalidateNetwork() {
		Map<String, IPeripheral> removed = newHashMap();
		Map<String, IPeripheral> added = newHashMap();

		for (Point point : points.values()) {
			MapChanges<String, IPeripheral> peripherals = point.refreshPeripherals();
			removed.putAll(peripherals.removed());
			added.putAll(peripherals.added());
		}

		peripheralsUpdate(removed, added, null);
		ControllerValidator.validate(this);
	}

	@Override
	public void invalidateNode(INetworkNode toInvalidate) {
		Point point = getPoint(toInvalidate);
		peripheralsUpdate(point.refreshPeripherals(), null);
		ControllerValidator.validate(this);
	}
	//endregion

	//region INetworkController getters
	@Override
	public Set<INetworkNode> getNodesOnNetwork() {
		Set<INetworkNode> nodeView = this.nodeView;
		if (nodeView == null) {
			nodeView = this.nodeView = unmodifiableSet(points.keySet());
		}
		return nodeView;
	}

	@Override
	public Set<UnorderedPair<INetworkNode>> getNodeConnections() {
		Set<UnorderedPair<INetworkNode>> connections = newHashSet();
		for (Point point : points.values()) {
			for (Point.Connection connection : point.connections) {
				connections.add(new UnorderedPair<INetworkNode>(point.node, connection.other(point).node));
			}
		}

		return unmodifiableSet(connections);
	}

	@Override
	public Map<String, IPeripheral> getPeripheralsOnNetwork() {
		return unmodifiableMap(peripherals);
	}
	//endregion

	@Override
	public void transmitPacket(INetworkNode start, Packet packet) {
		Point startPoint = getPoint(start);

		Set<Point> received = newHashSetWithExpectedSize(points.size());
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
