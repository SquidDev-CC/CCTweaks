package org.squiddev.cctweaks.api.network;

import net.minecraft.tileentity.TileEntity;

/**
 * Register a custom provider for network nodes
 */
public interface INetworkNodeProvider {
	/**
	 * Get the network node for the specific TileEntity
	 *
	 * @param tile The entity to get the node for
	 * @return The node or {@code null} if it cannot be converted
	 */
	INetworkNode getNode(TileEntity tile);

	/**
	 * Checks if this TileEntity is a network node
	 *
	 * @param tile The entity to check
	 * @return True if this can be converted into a node
	 */
	boolean isNode(TileEntity tile);
}
