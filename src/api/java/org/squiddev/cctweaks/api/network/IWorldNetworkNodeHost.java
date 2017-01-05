package org.squiddev.cctweaks.api.network;

import javax.annotation.Nonnull;

/**
 * An object that hosts a {@link IWorldNetworkNode}.
 *
 * Instead of implementing {@link IWorldNetworkNode} and delegating methods to it,
 * you can implement this interface instead. This is supported for TileEntities, multiparts
 * and on {@link dan200.computercraft.api.turtle.ITurtleUpgrade}
 */
public interface IWorldNetworkNodeHost {
	/**
	 * Get this host's node. This should NEVER be null.
	 *
	 * @return The node this object holds
	 */
	@Nonnull
	IWorldNetworkNode getNode();
}
