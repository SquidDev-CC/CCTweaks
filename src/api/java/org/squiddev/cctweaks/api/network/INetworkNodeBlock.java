package org.squiddev.cctweaks.api.network;

import net.minecraft.world.IBlockAccess;

/**
 * This block provides a {@link INetworkNode} via a tile entity
 */
public interface INetworkNodeBlock {
	/**
	 * Get the node this block implements
	 *
	 * @param world The world the block is in
	 * @param x     X coordinates of the block
	 * @param y     Y coordinates of the block
	 * @param z     Z coordinates of the block
	 * @param meta  Block metadata
	 * @return The network node or null if it doesn't exist
	 */
	INetworkNode getNode(IBlockAccess world, int x, int y, int z, int meta);
}
