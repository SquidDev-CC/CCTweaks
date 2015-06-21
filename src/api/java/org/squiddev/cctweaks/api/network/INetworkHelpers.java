package org.squiddev.cctweaks.api.network;

import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import org.squiddev.cctweaks.api.IWorldPosition;

import java.util.Set;

/**
 * Various helper methods for networks
 */
public interface INetworkHelpers {
	/**
	 * Check if an adjacent block is a node and accepts connections.
	 *
	 * @param world     World the node lies in
	 * @param x         X position of the node we are checking from
	 * @param y         Y position of the node we are checking from
	 * @param z         Z position of the node we are checking from
	 * @param direction Direction we are checking in
	 * @return If the target block is a node and can be connected to
	 */
	boolean canConnect(IBlockAccess world, int x, int y, int z, ForgeDirection direction);

	/**
	 * Check if an adjacent block is a node and accepts connections.
	 *
	 * @param pos       The position of the current node
	 * @param direction Direction we are checking in
	 * @return If the target block is a node and can be connected to
	 */
	boolean canConnect(IWorldPosition pos, ForgeDirection direction);

	/**
	 * Get adjacent nodes that can be connected to
	 *
	 * Checks the current node can connect, and adjacent node can be connected to
	 * in that direction
	 *
	 * @param node The current node
	 * @return The adjacent nodes
	 */
	Set<INetworkNode> getAdjacentNodes(IWorldNetworkNode node);

	/**
	 * Connect to adjacent nodes, or create a network.
	 *
	 * Uses {@link #getAdjacentNodes(IWorldNetworkNode)} and {@link #joinOrCreateNetwork(INetworkNode, Set)}.
	 *
	 * @param node The node to scan with
	 */
	void joinOrCreateNetwork(IWorldNetworkNode node);

	/**
	 * Attempt to connect to all nodes.
	 *
	 * If it cannot find a network it will create a new one and assimilate all nodes in {@code connections}.
	 *
	 * @param node        The node to scan with
	 * @param connections The nodes that can connect
	 */
	void joinOrCreateNetwork(INetworkNode node, Set<? extends INetworkNode> connections);

	/**
	 * Creates a new network for the node.
	 * It will be removed from the current network.
	 *
	 * @param node The node to create the network with.
	 */
	void joinNewNetwork(INetworkNode node);

	/**
	 * Schedule calling {@link #joinOrCreateNetwork(IWorldNetworkNode)} next tick.
	 *
	 * This is the recommended method of attaching nodes to a network.
	 *
	 * @param node The node to schedule
	 */
	void scheduleJoin(final IWorldNetworkNode node);
}
