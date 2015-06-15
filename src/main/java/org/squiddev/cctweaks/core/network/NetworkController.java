package org.squiddev.cctweaks.core.network;

import com.google.common.base.Optional;
import dan200.computercraft.api.peripheral.IPeripheral;
import org.squiddev.cctweaks.api.SingleTypeUnorderedPair;
import org.squiddev.cctweaks.api.UnorderedPair;
import org.squiddev.cctweaks.api.network.INetworkController;
import org.squiddev.cctweaks.api.network.INetworkNode;
import org.squiddev.cctweaks.api.network.IWorldNetworkNode;
import org.squiddev.cctweaks.api.network.Packet;

import java.util.*;

public class NetworkController implements INetworkController {
	public Set<INetworkNode> network;
	public Set<SingleTypeUnorderedPair<INetworkNode>> networkConnections;
	public Map<String, IPeripheral> peripheralsOnNetwork = new HashMap<String, IPeripheral>();

	public NetworkController(INetworkNode node) {
		this(new HashSet<INetworkNode>(), new HashSet<SingleTypeUnorderedPair<INetworkNode>>(), new HashMap<String, IPeripheral>());
		assimilateNode(node);
	}

	public NetworkController(Set<INetworkNode> network, Set<SingleTypeUnorderedPair<INetworkNode>> networkConnections, Map<String, IPeripheral> peripheralsOnNetwork) {
		if (network == null) {
			throw new NullPointerException("Severed network can't be null");
		}
		if (networkConnections == null) {
			throw new NullPointerException("Network connections can't be null");
		}
		if (peripheralsOnNetwork == null) {
			throw new NullPointerException("Old peripherals can't be null");
		}
		this.network = network;
		this.networkConnections = networkConnections;
		this.peripheralsOnNetwork = peripheralsOnNetwork;
	}

	private void attachNodes() {
		for (INetworkNode node : getNodesOnNetwork()) {
			node.detachFromNetwork();
		}
		for (INetworkNode node : getNodesOnNetwork()) {
			node.attachToNetwork(this);
		}
		invalidateNetwork();
	}

	private Map<String, IPeripheral> calculatePeripheralsOnNetwork() {
		Map<String, IPeripheral> peripherals = new HashMap<String, IPeripheral>();
		for (INetworkNode node : network) {
			peripherals.putAll(node.getConnectedPeripherals());
		}
		return peripherals;
	}

	private void assimilateNode(INetworkNode newNode) {
		network.add(newNode);

		INetworkController controller = newNode.getAttachedNetwork();
		if (controller == null) {
			newNode.attachToNetwork(this);
		} else {
			network.addAll(controller.getNodesOnNetwork());
			networkConnections.addAll(controller.getNodeConnections());
			for (INetworkNode node : controller.getNodesOnNetwork()) {
				node.detachFromNetwork();
			}
			for (INetworkNode node : controller.getNodesOnNetwork()) {
				node.attachToNetwork(this);
			}
		}
	}

	// INetworkController

	@Override
	public void formConnection(INetworkNode existingNode, INetworkNode newNode) {
		if (existingNode == newNode) throw new IllegalArgumentException("Cannot connect a node to itself");
		if (!network.contains(existingNode)) {
			throw new IllegalArgumentException("Existing node must be on the network");
		}

		SingleTypeUnorderedPair<INetworkNode> connection = new SingleTypeUnorderedPair<INetworkNode>(existingNode, newNode);
		networkConnections.add(connection);

		if (network.contains(newNode)) {
			// Network already contains new node.
			// No change. Assimilation unnecessary.
			return;
		}
		assimilateNode(newNode);
		invalidateNetwork();
	}

	@Override
	public void breakConnection(final SingleTypeUnorderedPair<INetworkNode> connection) {
		if (!networkConnections.contains(connection)) {
			throw new IllegalArgumentException("Connection does not exist");
		}

		networkConnections.remove(connection);

		Optional<NetworkController> xNetwork = new CancelOnFindScanner(connection.y).scan(connection.x);

		if (!xNetwork.isPresent()) {
			// Scan was cancelled for finding y.
			// Network unchanged.
			return;
		}

		NetworkController yNetwork = new NeverCancelScanner().scan(connection.y).get();

		xNetwork.get().attachNodes();
		yNetwork.attachNodes();
	}

	@Override
	public void removeNode(INetworkNode removedNode) {
		removedNode.detachFromNetwork();

		List<INetworkNode> connectingNodes = new ArrayList<INetworkNode>();
		Iterator<SingleTypeUnorderedPair<INetworkNode>> i = networkConnections.iterator();
		while (i.hasNext()) {
			SingleTypeUnorderedPair<INetworkNode> pair = i.next();
			if (pair.contains(removedNode)) {
				i.remove();
				connectingNodes.add(pair.other(removedNode));
			}
		}
		network.remove(removedNode);

		List<NetworkController> newNetworks = new ArrayList<NetworkController>(connectingNodes.size());

		starting_nodes:
		for (INetworkNode startingNode : connectingNodes) {
			for (NetworkController controller : newNetworks) {
				if (controller.network.contains(startingNode)) {
					continue starting_nodes;
				}
			}

			NetworkController newNetwork = new NeverCancelScanner().scan(startingNode).get();
			newNetwork.attachNodes();

			newNetworks.add(newNetwork);
		}
	}

	@Override
	public Map<String, IPeripheral> getPeripheralsOnNetwork() {
		return peripheralsOnNetwork;
	}

	@Override
	public void invalidateNetwork() {
		Map<String, IPeripheral> oldPeripherals = peripheralsOnNetwork;
		peripheralsOnNetwork = calculatePeripheralsOnNetwork();

		for (INetworkNode node : network) {
			node.networkInvalidated(oldPeripherals);
		}
	}

	@Override
	public Set<INetworkNode> getNodesOnNetwork() {
		return network;
	}

	@Override
	public Set<SingleTypeUnorderedPair<INetworkNode>> getNodeConnections() {
		return networkConnections;
	}

	@Override
	public void transmitPacket(INetworkNode node, Packet packet) {
		transmitPacket(node, packet, 0);
	}

	private void transmitPacket(INetworkNode startingNode, Packet packet, double i) {
		Set<INetworkNode> received = new HashSet<INetworkNode>(network.size());
		Queue<UnorderedPair<Double, INetworkNode>> transmitTo = new LinkedList<UnorderedPair<Double, INetworkNode>>();
		transmitTo.offer(new UnorderedPair<Double, INetworkNode>(i, startingNode));

		UnorderedPair<Double, INetworkNode> nodePair;
		while ((nodePair = transmitTo.poll()) != null) {
			INetworkNode node = nodePair.y;
			if (received.contains(node)) {
				continue;
			}
			received.add(node);
			if (node.equals(startingNode)) {
				// Starting node shouldn't receive its own packet.
				continue;
			}

			node.receivePacket(packet, nodePair.x);
			for (SingleTypeUnorderedPair<INetworkNode> pair : networkConnections) {
				if (pair.contains(node)) {
					INetworkNode other = pair.other(node);
					double distance = nodePair.x;
					if (node instanceof IWorldNetworkNode && other instanceof IWorldNetworkNode) {
						IWorldNetworkNode worldNode = (IWorldNetworkNode) node;
						IWorldNetworkNode otherNode = (IWorldNetworkNode) other;

						int dx = Math.abs(worldNode.getPosition().getX() - otherNode.getPosition().getX());
						int dy = Math.abs(worldNode.getPosition().getY() - otherNode.getPosition().getY());
						int dz = Math.abs(worldNode.getPosition().getZ() - otherNode.getPosition().getZ());

						distance += Math.sqrt(dx * dx + dy * dy * dz * dz);
					}
					transmitTo.offer(new UnorderedPair<Double, INetworkNode>(distance, other));
				}
			}
		}
	}

	private abstract class NetworkScanner {
		/**
		 * Called when scanning a new node.
		 *
		 * @param node The node being scanned.
		 * @return If the scan should continue.
		 */
		public abstract boolean continueScanning(INetworkNode node);

		/**
		 * Scan a network from a starting node.
		 *
		 * @param startingNode The node to start from.
		 * @return {@link Optional#of(Object)} the scanned network,
		 * or {@link Optional#absent()} if {@link #continueScanning(INetworkNode)} returned false.
		 */
		public Optional<NetworkController> scan(INetworkNode startingNode) {
			Set<SingleTypeUnorderedPair<INetworkNode>> newNetworkConnections = new HashSet<SingleTypeUnorderedPair<INetworkNode>>();
			Set<INetworkNode> newNetwork = new HashSet<INetworkNode>();
			Queue<INetworkNode> toVisit = new LinkedList<INetworkNode>();
			toVisit.offer(startingNode);

			INetworkNode node;
			while ((node = toVisit.poll()) != null) {
				if (!continueScanning(node)) {
					return Optional.absent();
				}

				if (newNetwork.add(node)) {
					for (SingleTypeUnorderedPair<INetworkNode> pair : networkConnections) {
						if (pair.contains(node)) {
							toVisit.offer(pair.other(node));
							newNetworkConnections.add(pair);
						}
					}
				}
			}

			return Optional.of(new NetworkController(newNetwork, newNetworkConnections, peripheralsOnNetwork));
		}
	}

	private class CancelOnFindScanner extends NetworkScanner {
		private final INetworkNode node;

		public CancelOnFindScanner(INetworkNode node) {
			this.node = node;
		}

		@Override
		public boolean continueScanning(INetworkNode node) {
			return !this.node.equals(node);
		}
	}

	private class NeverCancelScanner extends NetworkScanner {
		@Override
		public boolean continueScanning(INetworkNode node) {
			return true;
		}
	}
}
