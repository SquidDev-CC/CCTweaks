package org.squiddev.cctweaks.blocks.debug;

import dan200.computercraft.api.peripheral.IPeripheral;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.api.network.Packet;
import org.squiddev.cctweaks.blocks.network.TileNetworked;
import org.squiddev.cctweaks.core.network.AbstractWorldNode;
import org.squiddev.cctweaks.core.utils.DebugLogger;

import java.util.Map;

/**
 * A network node that logs events to the console
 */
public class TileDebugNode extends TileNetworked {
	protected AbstractWorldNode node = new AbstractWorldNode() {
		@Override
		public IWorldPosition getPosition() {
			return TileDebugNode.this;
		}

		@Override
		public void receivePacket(Packet packet, double distanceTravelled) {
			DebugLogger.debug("Received packet from " + distanceTravelled + " blocks away");
		}

		@Override
		public void networkInvalidated(Map<String, IPeripheral> oldPeripherals) {
			DebugLogger.debug("Node invalidated at " + xCoord + " ," + yCoord + ", " + zCoord);
		}
	};


	@Override
	public AbstractWorldNode getNode() {
		return node;
	}
}
