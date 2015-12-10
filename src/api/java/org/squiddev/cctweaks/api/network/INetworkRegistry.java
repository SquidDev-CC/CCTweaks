package org.squiddev.cctweaks.api.network;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.squiddev.cctweaks.api.IWorldPosition;

/**
 * Handles registration of node providers
 */
public interface INetworkRegistry {
	/**
	 * Add a custom node provider.
	 *
	 * This should occur during the init stages of a mod
	 *
	 * @param provider The provider to register
	 */
	void addNodeProvider(INetworkNodeProvider provider);

	/**
	 * Check if this block is a node
	 *
	 * @param world    The world to check in
	 * @param position Position of the block
	 * @return If this block is a node
	 */
	boolean isNode(IBlockAccess world, BlockPos position);

	/**
	 * Check if this tile is a node
	 *
	 * @param tile The tile to check
	 * @return If this block is a node
	 */
	boolean isNode(TileEntity tile);

	/**
	 * Check if block tile is a node
	 *
	 * @param position The position to check
	 * @return If this block is a node
	 */
	boolean isNode(IWorldPosition position);

	/**
	 * Get the node for this position
	 *
	 * @param world    The world to check in
	 * @param position Position of the block
	 * @return The node, or null if there is none
	 */
	IWorldNetworkNode getNode(IBlockAccess world, BlockPos position);

	/**
	 * Get the node from this tile entity
	 *
	 * @param tile The tile to check
	 * @return The node, or null if there is none
	 */
	IWorldNetworkNode getNode(TileEntity tile);

	/**
	 * Get the node for this position
	 *
	 * @param position The position to check
	 * @return The node, or null if there is none
	 */
	IWorldNetworkNode getNode(IWorldPosition position);
}
