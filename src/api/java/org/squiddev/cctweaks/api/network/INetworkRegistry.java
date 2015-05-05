package org.squiddev.cctweaks.api.network;

import net.minecraft.tileentity.TileEntity;
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
	 * @param world The world to check in
	 * @param x     X position of the block
	 * @param y     Y position of the block
	 * @param z     Z position of the block
	 * @return If this block is a node
	 */
	boolean isNode(IBlockAccess world, int x, int y, int z);

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
	 * @param world The world to check in
	 * @param x     X position of the block
	 * @param y     Y position of the block
	 * @param z     Z position of the block
	 * @return The node, or null if there is none
	 */
	INetworkNode getNode(IBlockAccess world, int x, int y, int z);

	/**
	 * Get the node from this tile entity
	 *
	 * @param tile The tile to check
	 * @return The node, or null if there is none
	 */
	INetworkNode getNode(TileEntity tile);

	/**
	 * Get the node for this position
	 *
	 * @param position The position to check
	 * @return The node, or null if there is none
	 */
	INetworkNode getNode(IWorldPosition position);
}
