package org.squiddev.cctweaks.core.network.controller;

import dan200.computercraft.api.peripheral.IPeripheral;
import org.squiddev.cctweaks.api.UnorderedPair;
import org.squiddev.cctweaks.api.network.*;
import org.squiddev.cctweaks.core.collections.MapChanges;
import org.squiddev.cctweaks.core.collections.MapsX;

import java.util.*;

/**
 * A {@link Point} represents one node on the network, and all its connections.
 */
public final class Point implements INetworkAccess {
	protected final INetworkNode node;
	protected INetworkController controller;

	public final Set<Connection> connections = new HashSet<Connection>();

	public Map<String, IPeripheral> peripherals = Collections.emptyMap();

	public Point(INetworkNode node) {
		this.node = node;
		this.controller = node.getAttachedNetwork();
	}

	//region INetworkAccess
	@Override
	public Map<String, IPeripheral> getPeripheralsOnNetwork() {
		if (controller == null) {
			return Collections.emptyMap();
		} else {
			return controller.getPeripheralsOnNetwork();
		}
	}

	@Override
	public void invalidateNetwork() {
		if (controller != null) controller.invalidateNetwork();
	}

	@Override
	public boolean transmitPacket(Packet packet) {
		if (controller == null) {
			return false;
		} else {
			controller.transmitPacket(node, packet);
			return true;
		}
	}
	//endregion

	public Collection<Point> breakConnections() {
		ArrayList<Point> connected = new ArrayList<Point>(connections.size());
		for (Connection connection : connections) {
			Point other = connection.other(this);
			connected.add(other);
			other.connections.remove(connection);
		}

		connections.clear();

		return connected;
	}

	public MapChanges<String, IPeripheral> refreshPeripherals() {
		Map<String, IPeripheral> oldPeripherals = peripherals;
		Map<String, IPeripheral> newPeripherals = peripherals = node.getConnectedPeripherals();

		MapChanges<String, IPeripheral> difference = MapsX.changes(oldPeripherals, newPeripherals);

		for (Map.Entry<String, IPeripheral> entry : difference.removed().entrySet()) {
			IPeripheral peripheral = entry.getValue();
			if (peripheral instanceof INetworkedPeripheral) {
				((INetworkedPeripheral) peripheral).detachFromNetwork(this, entry.getKey());
			}
		}

		for (Map.Entry<String, IPeripheral> entry : difference.added().entrySet()) {
			IPeripheral peripheral = entry.getValue();
			if (peripheral instanceof INetworkedPeripheral) {
				((INetworkedPeripheral) peripheral).attachToNetwork(this, entry.getKey());
			}
		}

		return difference;
	}

	//region Network delegates
	public void detachFromNetwork() {
		if (controller == null) {
			throw new IllegalStateException("Not connected to network for " + this);
		}

		node.detachFromNetwork();
		controller = null;
	}

	public void attachToNetwork(INetworkController controller) {
		if (this.controller != null) {
			throw new IllegalStateException("Already connected for " + this);
		} else if (controller == null) {
			throw new IllegalArgumentException("Cannot connect to <null> for " + this);
		}

		this.controller = controller;
		node.attachToNetwork(controller);
	}

	public void networkInvalidated(Map<String, IPeripheral> oldPeripherals, Map<String, IPeripheral> newPeripherals) {
		if (controller == null) {
			throw new IllegalStateException("Cannot invalidate network when not connected");
		}

		node.networkInvalidated(oldPeripherals, newPeripherals);

		for (Map.Entry<String, IPeripheral> entry : peripherals.entrySet()) {
			IPeripheral value = entry.getValue();

			if (value instanceof INetworkedPeripheral) {
				((INetworkedPeripheral) value).networkInvalidated(this, oldPeripherals, newPeripherals);
			}
		}
	}

	public void receivePacket(Packet packet, double distanceTravelled) {
		if (controller == null) {
			throw new IllegalStateException("Cannot send packet when not connected");
		}

		node.receivePacket(packet, distanceTravelled);
		for (Map.Entry<String, IPeripheral> entry : peripherals.entrySet()) {
			IPeripheral value = entry.getValue();

			if (value instanceof INetworkedPeripheral) {
				((INetworkedPeripheral) value).receivePacket(this, packet, distanceTravelled);
			}
		}
	}
	//endregion

	@Override
	public String toString() {
		return "Point<" + node + '>';
	}

	public static class Connection extends UnorderedPair<Point> {
		public Connection(Point x, Point y) {
			super(x, y);
		}

		public Collection<Map<INetworkNode, Point>> breakConnection() {
			x.connections.remove(this);
			y.connections.remove(this);

			return NodeScanner.scanNetwork(x.controller, x, y);
		}

		public boolean formConnection() {
			if (x.connections.contains(this)) return false;

			x.connections.add(this);
			y.connections.add(this);
			return true;
		}
	}
}
