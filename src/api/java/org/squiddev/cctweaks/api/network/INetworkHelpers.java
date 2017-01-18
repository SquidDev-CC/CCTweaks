package org.squiddev.cctweaks.api.network;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.squiddev.cctweaks.api.IWorldPosition;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * Various helper methods for networks
 */
public interface INetworkHelpers {
	/**
	 * Check if an adjacent block is a node and accepts connections.
	 *
	 * @param world     World the node lies in
	 * @param position  Position of the node we are checking from
	 * @param direction Direction we are checking in
	 * @return If the target block is a node and can be connected to
	 */
	boolean canConnect(@Nonnull IBlockAccess world, @Nonnull BlockPos position, @Nonnull EnumFacing direction);

	/**
	 * Check if an adjacent block is a node and accepts connections.
	 *
	 * @param pos       The position of the current node
	 * @param direction Direction we are checking in
	 * @return If the target block is a node and can be connected to
	 */
	boolean canConnect(@Nonnull IWorldPosition pos, @Nonnull EnumFacing direction);

	/**
	 * Get adjacent nodes that can be connected to
	 *
	 * Checks the current node can connect, and adjacent node can be connected to
	 * in that direction
	 *
	 * @param node The current node
	 * @return The adjacent nodes
	 */
	@Nonnull
	Set<INetworkNode> getAdjacentNodes(@Nonnull IWorldNetworkNode node);

	/**
	 * Get adjacent nodes that can be connected to
	 *
	 * Checks the current node can connect, and adjacent node can be connected to
	 * in that direction
	 *
	 * @param node        The current node
	 * @param checkExists Check if the block exists. This is {@code true} by default
	 *                    for {@link #getAdjacentNodes(IWorldNetworkNode)}, and controls
	 *                    if we should check if the chunk the neighbouring blocks are loaded
	 * @return The adjacent nodes
	 */
	@Nonnull
	Set<INetworkNode> getAdjacentNodes(@Nonnull IWorldNetworkNode node, boolean checkExists);

	/**
	 * Connect to adjacent nodes, or create a network.
	 *
	 * Uses {@link #getAdjacentNodes(IWorldNetworkNode)} and {@link #joinOrCreateNetwork(INetworkNode, Set)}.
	 *
	 * @param node The node to scan with
	 */
	void joinOrCreateNetwork(@Nonnull IWorldNetworkNode node);

	/**
	 * Attempt to connect to all nodes.
	 *
	 * If it cannot find a network it will create a new one and assimilate all nodes in {@code connections}.
	 *
	 * @param node        The node to scan with
	 * @param connections The nodes that can connect
	 */
	void joinOrCreateNetwork(@Nonnull INetworkNode node, @Nonnull Set<? extends INetworkNode> connections);

	/**
	 * Creates a new network for the node.
	 * It will be removed from the current network.
	 *
	 * @param node The node to create the network with.
	 */
	void joinNewNetwork(@Nonnull INetworkNode node);

	/**
	 * Schedule calling {@link #joinOrCreateNetwork(IWorldNetworkNode)} next tick.
	 *
	 * This is the recommended method of attaching nodes to a network.
	 *
	 * @param node The node to schedule
	 */
	void scheduleJoin(@Nonnull IWorldNetworkNode node);

	/**
	 * Schedule calling {@link #joinOrCreateNetwork(IWorldNetworkNode)} next tick.
	 *
	 * This is the recommended method of attaching nodes to a network.
	 *
	 * @param node The node to schedule
	 * @param tile The tile this node is bound to. We will ensure the tile is still there.
	 */
	void scheduleJoin(@Nonnull IWorldNetworkNode node, @Nonnull TileEntity tile);
}
