package org.squiddev.cctweaks.api.network;

import net.minecraft.util.EnumFacing;
import org.squiddev.cctweaks.api.IWorldPosition;

import javax.annotation.Nonnull;

/**
 * Represents an INetworkNode with a position in the world.
 */
public interface IWorldNetworkNode extends INetworkNode {
	/**
	 * Get the position the node exists in in the world
	 *
	 * @return The node's position
	 */
	@Nonnull
	IWorldPosition getPosition();

	/**
	 * Get if the node can connect in this direction.
	 *
	 * This does not specify whether a connection exists, but if it can exist.
	 *
	 * @param direction The direction to check
	 * @return If the node can connect
	 */
	boolean canConnect(@Nonnull EnumFacing direction);
}
