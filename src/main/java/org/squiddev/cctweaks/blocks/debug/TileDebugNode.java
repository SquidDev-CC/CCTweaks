package org.squiddev.cctweaks.blocks.debug;

import org.squiddev.cctweaks.api.network.INetworkNode;
import org.squiddev.cctweaks.api.network.Packet;
import org.squiddev.cctweaks.blocks.network.TileNetworked;
import org.squiddev.cctweaks.core.utils.DebugLogger;

/**
 * A network node that logs events to the console
 */
public class TileDebugNode extends TileNetworked implements INetworkNode {
	@Override
	public void receivePacket(Packet packet, int distanceTravelled) {
		DebugLogger.debug("Received packet from " + distanceTravelled + " blocks away");
	}

	@Override
	public void networkInvalidated() {
		DebugLogger.debug("Node invalidated at " + xCoord + " ," + yCoord + ", " + zCoord);
	}
}
