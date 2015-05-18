package org.squiddev.cctweaks.api.network;

import dan200.computercraft.api.peripheral.IPeripheral;
import org.squiddev.cctweaks.api.SingleTypeUnorderedPair;

import java.util.Map;
import java.util.Set;

/**
 * Interface to represent the network controller object.
 */
public interface INetworkController {
	/**
	 * Nodes call this when a connection is formed between two nodes.
	 * For example, a cover being removed between two cables.
	 * The nodes' networks will assimilate.
	 *
	 * This is not used when a node comes to exist.
	 * Only when a connection is formed.
	 *
	 * @param existingNode	The node already on this network.
	 * @param newNode		The node that might not already be on this network.
	 */
	void formConnection(INetworkNode existingNode, INetworkNode newNode);

	/**
	 * Call this when a node is added to the world and needs to join the network.
	 * For example, a cable being placed.
	 * The network will assimilate the node and its newly connected networks.
	 *
	 * @param node The node being added.
	 */
	void addNode(INetworkNode node);

	/**
	 * Nodes call this when a connection between two nodes is broken.
	 * For example, a cover being placed between two cables.
	 * The network will reevaluate nodes and create a severed network if necessary.
	 *
	 * This is not used when nodes are removed entirely.
	 * Only when two nodes disconnect.
	 *
	 * @param connection A pair of nodes representing the nodes being disconnected.
	 */
	void breakConnection(SingleTypeUnorderedPair<INetworkNode> connection);

	/**
	 * Nodes call this when they wish to be removed from the network entirely.
	 * For example, a cable being broken.
	 * The network will reevaluate nodes and create as many severed networks as necessary.
	 *
	 * @param node The node being removed.
	 */
	void removeNode(INetworkNode node);

	/**
	 * Gets the peripherals known to be on the network.
	 * This is usually cached, only changing when {@link #invalidateNetwork()} is called.
	 * This is done automatically in the network modification operations.
	 *
	 * @return The cached map of peripherals on the network.
	 */
	Map<String, IPeripheral> getPeripheralsOnNetwork();

	/**
	 * Invalidate the list of peripherals on the network.
	 */
	void invalidateNetwork();

	/**
	 * @return All nodes on the network.
	 */
	Set<INetworkNode> getNodesOnNetwork();

	/**
	 * @return All the pairs of nodes that are connected.
	 */
	Set<SingleTypeUnorderedPair<INetworkNode>> getNodeConnections();
}
