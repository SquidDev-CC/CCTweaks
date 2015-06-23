package org.squiddev.cctweaks.core.network.controller;

import dan200.computercraft.api.peripheral.IPeripheral;
import org.squiddev.cctweaks.api.SingleTypeUnorderedPair;
import org.squiddev.cctweaks.api.network.INetworkAccess;
import org.squiddev.cctweaks.api.network.INetworkController;
import org.squiddev.cctweaks.api.network.INetworkNode;
import org.squiddev.cctweaks.api.network.Packet;
import org.squiddev.cctweaks.core.utils.IterableIterator;

import java.util.*;

/**
 * A {@link Point} represents one node on the network, and all its connections.
 */
public class Point implements INetworkAccess {
	protected final INetworkNode node;
	protected INetworkController controller;

	public final Set<Connection> connections = new HashSet<Connection>();

	public Map<String, IPeripheral> peripherals = Collections.emptyMap();

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

	public Collection<Map<INetworkNode, Point>> remove() {
		for (Connection connection : connections) {
			connection.other(this).connections.remove(connection);
		}

		// TODO: Invalidate network
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

	public static class Connection extends SingleTypeUnorderedPair<Point> {
		public Connection(Point x, Point y) {
			super(x, y);

			x.connections.add(this);
			y.connections.add(this);
		}

		public Collection<Map<INetworkNode, Point>> remove() {
			x.connections.remove(this);
			y.connections.remove(this);

			return NodeScanner.scanNetwork(x, y);
		}
	}
}
