package org.squiddev.cctweaks.core.network;

import dan200.computercraft.api.peripheral.IPeripheral;
import org.squiddev.cctweaks.api.network.INetworkController;
import org.squiddev.cctweaks.api.network.INetworkNode;

import java.util.*;

public class NetworkController implements INetworkController {
	public Set<INetworkNode> network;
	public Map<String, IPeripheral> peripheralsOnNetwork = new HashMap<String, IPeripheral>();

	public NetworkController(Set<INetworkNode> severedNetwork) {
		if (severedNetwork == null) {
			throw new NullPointerException("Severed network can't be null");
		}
		this.network = severedNetwork;
	}

	private Map<String, IPeripheral> calculatePeripheralsOnNetwork() {
		Map<String, IPeripheral> peripherals = new HashMap<String, IPeripheral>();
		for (INetworkNode node : network) {
			peripherals.putAll(node.getConnectedPeripherals());
		}
		return peripherals;
	}

	private void assimilateNode(INetworkNode newNode) {
		Queue<INetworkNode> toVisit = new LinkedList<INetworkNode>();
		toVisit.offer(newNode);

		while (!toVisit.isEmpty()) {
			INetworkNode node = toVisit.remove();
			if (!network.contains(node)) {
				node.detachFromNetwork();
				node.attachToNetwork(this);
				network.add(node);
				toVisit.addAll(node.getConnectedNodes());
			}
		}
	}

	// INetworkController

	@Override
	public void breakConnection(INetworkNode node1, INetworkNode node2) {
		if (!(network.contains(node1) && network.contains(node2))) {
			throw new IllegalArgumentException("Both nodes must be on the network.");
		}

		Set<INetworkNode> newNetwork = new HashSet<INetworkNode>();
		Queue<INetworkNode> toVisit = new LinkedList<INetworkNode>();
		toVisit.offer(node1);
		while (!toVisit.isEmpty()) {
			INetworkNode node = toVisit.remove();
			if (!newNetwork.contains(node)) {
				newNetwork.add(node);

				for (INetworkNode nextNode : node.getConnectedNodes()) {
					if (node2.equals(nextNode)) {
						// Network remained unchanged.
						// Severance unnecessary.
						return;
					}
					toVisit.offer(nextNode);
				}
			}
		}

		// Handle network severance.
		Set<INetworkNode> oldNetwork = this.network;
		this.network = newNetwork;
		Set<INetworkNode> severedNetwork = new HashSet<INetworkNode>();

		for (INetworkNode node : oldNetwork) {
			if (!newNetwork.contains(node)) {
				node.detachFromNetwork();
				severedNetwork.add(node);
			}
		}

		INetworkController severedController = new NetworkController(severedNetwork);
		for (INetworkNode node : severedNetwork) {
			node.attachToNetwork(severedController);
		}

		invalidateNetwork();
	}

	@Override
	public void formConnection(INetworkNode existingNode, INetworkNode newNode) {
		if (!network.contains(existingNode)) {
			throw new IllegalArgumentException("Existing node must be on the network");
		}

		if (network.contains(newNode)) {
			// Network already contains new node.
			// No change. Assimilation unnecessary.
			return;
		}
		assimilateNode(newNode);
		invalidateNetwork();
	}

	@Override
	public void removeNode(INetworkNode removedNode) {
		if (!network.contains(removedNode)) {
			throw new IllegalArgumentException("Removed node must be on the network");
		}

		Set<INetworkNode> connectedNodes = removedNode.getConnectedNodes();
		List<Set<INetworkNode>> newNetworks = new ArrayList<Set<INetworkNode>>(connectedNodes.size());

	starting_nodes:
		for (INetworkNode startingNode : connectedNodes) {
			for (Set<INetworkNode> network : newNetworks) {
				if (network.contains(startingNode)) {
					continue starting_nodes;
				}
			}

			Set<INetworkNode> newNetwork = new HashSet<INetworkNode>();
			Queue<INetworkNode> toVisit = new LinkedList<INetworkNode>();
			toVisit.offer(startingNode);

			while (!toVisit.isEmpty()) {
				INetworkNode node = toVisit.remove();
				if (!newNetwork.contains(node)) {
					newNetwork.add(node);
					toVisit.addAll(node.getConnectedNodes());
				}
			}

			newNetworks.add(newNetwork);
		}

		if (newNetworks.size() == 1) {
			// Network unchanged.
			this.network = newNetworks.get(0);
			removedNode.detachFromNetwork();
			return;
		}

		// Create brand new networks
		this.network = null;
		for (Set<INetworkNode> newNetwork : newNetworks) {
			for (INetworkNode node : newNetwork) {
				node.detachFromNetwork();
			}
			INetworkController newController = new NetworkController(newNetwork);
			for (INetworkNode node : newNetwork) {
				node.attachToNetwork(newController);
			}
		}

		invalidateNetwork();
	}

	@Override
	public void addNode(INetworkNode node) {
		if (network.contains(node)) {
			// Network already contains new node.
			// No change. Assimilation unnecessary.
			return;
		}
		assimilateNode(node);
		invalidateNetwork();
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
}
