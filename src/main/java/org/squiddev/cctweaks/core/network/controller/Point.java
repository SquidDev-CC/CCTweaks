package org.squiddev.cctweaks.core.network.controller;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import dan200.computercraft.api.peripheral.IPeripheral;
import org.squiddev.cctweaks.api.SingleTypeUnorderedPair;
import org.squiddev.cctweaks.api.network.*;
import org.squiddev.cctweaks.core.utils.IterableIterator;

import java.util.*;

/**
 * A {@link Point} represents one node on the network, and all its connections.
 */
public class Point implements INetworkAccess {
	protected final INetworkNode node;
	protected INetworkController controller;

	public final Set<Connection> connections = new HashSet<Connection>();

	public Map<String, IPeripheral> peripherals;

	public Point(INetworkNode node, INetworkController controller) {
		this.node = node;
		this.controller = controller;
	}

	@Override
	public Map<String, IPeripheral> getPeripheralsOnNetwork() {
		return controller.getPeripheralsOnNetwork();
	}

	@Override
	public void invalidateNetwork() {
		controller.invalidateNetwork();
	}

	@Override
	public boolean transmitPacket(Packet packet) {
		controller.transmitPacket(node, packet);
		return true;
	}

	public Collection<Map<INetworkNode, Point>> breakConnections() {
		for (Connection connection : connections) {
			connection.other(this).connections.remove(connection);
		}

		final Iterator<Connection> iterator = connections.iterator();

		return NodeScanner.scanNetwork(new IterableIterator<Point>() {
			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public Point next() {
				return iterator.next().other(Point.this);
			}
		});
	}

	public Map<String, IPeripheral> refreshPeripherals() {
		Map<String, IPeripheral> oldPeripherals = peripherals;
		if (oldPeripherals == null) oldPeripherals = Collections.emptyMap();

		Map<String, IPeripheral> newPeripherals = peripherals = node.getConnectedPeripherals();

		MapDifference<String, IPeripheral> difference = Maps.difference(oldPeripherals, newPeripherals);

		for (Map.Entry<String, IPeripheral> entry : difference.entriesOnlyOnLeft().entrySet()) {
			IPeripheral peripheral = entry.getValue();
			if (peripheral instanceof INetworkedPeripheral) {
				((INetworkedPeripheral) peripheral).detachFromNetwork(this, entry.getKey());
			}
		}

		for (Map.Entry<String, IPeripheral> entry : difference.entriesOnlyOnRight().entrySet()) {
			IPeripheral peripheral = entry.getValue();
			if (peripheral instanceof INetworkedPeripheral) {
				((INetworkedPeripheral) peripheral).attachToNetwork(this, entry.getKey());
			}
		}

		return newPeripherals;
	}

	//region Network events
	public void detachFromNetwork() {
		node.detachFromNetwork();
		for (Map.Entry<String, IPeripheral> entry : peripherals.entrySet()) {
			IPeripheral peripheral = entry.getValue();
			if (peripheral instanceof INetworkedPeripheral) {
				((INetworkedPeripheral) peripheral).detachFromNetwork(this, entry.getKey());
			}
		}
	}

	public void attachToNetwork(INetworkController controller) {
		node.attachToNetwork(controller);

		for (Map.Entry<String, IPeripheral> entry : peripherals.entrySet()) {
			IPeripheral peripheral = entry.getValue();
			if (peripheral instanceof INetworkedPeripheral) {
				((INetworkedPeripheral) peripheral).attachToNetwork(this, entry.getKey());
			}
		}
	}

	public void networkInvalidated(Map<String, IPeripheral> oldPeripherals, Map<String, IPeripheral> newPeripherals) {
		node.networkInvalidated(oldPeripherals, newPeripherals);

		for (Map.Entry<String, IPeripheral> entry : peripherals.entrySet()) {
			IPeripheral value = entry.getValue();

			if (value instanceof INetworkedPeripheral) {
				((INetworkedPeripheral) value).networkInvalidated(this, oldPeripherals, newPeripherals);
			}
		}
	}

	public void receivePacket(Packet packet, double distanceTravelled) {
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

	public static class Connection extends SingleTypeUnorderedPair<Point> {
		public Connection(Point x, Point y) {
			super(x, y);

			x.connections.add(this);
			y.connections.add(this);
		}

		public Collection<Map<INetworkNode, Point>> breakConnection() {
			x.connections.remove(this);
			y.connections.remove(this);

			return NodeScanner.scanNetwork(x, y);
		}
	}
}
