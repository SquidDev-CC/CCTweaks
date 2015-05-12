package org.squiddev.cctweaks.api.network;

/**
 * An object that hosts a {@link INetworkNode}.
 *
 * Instead of implementing {@link INetworkNode} and delegating methods to it,
 * you can implement this interface instead. This is supported for TileEntities, multiparts
 * and on {@link dan200.computercraft.api.turtle.ITurtleUpgrade}
 */
public interface INetworkNodeHost {
	/**
	 * Get this host's node. This should NEVER be null.
	 *
	 * @return The node this object holds
	 */
	INetworkNode getNode();
}
