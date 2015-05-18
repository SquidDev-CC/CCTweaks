package org.squiddev.cctweaks.api.network;

import net.minecraftforge.common.util.ForgeDirection;
import org.squiddev.cctweaks.api.IWorldPosition;

/**
 * Represents an INetworkNode with a position in the world.
 */
public interface IWorldNetworkNode extends INetworkNode {
	IWorldPosition getPosition();
	boolean canConnect(ForgeDirection direction);
}
