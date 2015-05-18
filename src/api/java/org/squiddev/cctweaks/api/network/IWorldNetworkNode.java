package org.squiddev.cctweaks.api.network;

import org.squiddev.cctweaks.api.IWorldPosition;

/**
 * Represents an INetworkNode with a position in the world.
 */
public interface IWorldNetworkNode extends INetworkNode {
	IWorldPosition getPosition();
}
