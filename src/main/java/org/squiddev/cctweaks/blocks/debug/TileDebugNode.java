package org.squiddev.cctweaks.blocks.debug;

import dan200.computercraft.api.peripheral.IPeripheral;
import org.apache.commons.lang3.StringUtils;
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
	protected final AbstractWorldNode node = new AbstractWorldNode() {
		@Override
		public IWorldPosition getPosition() {
			return TileDebugNode.this;
		}

		@Override
		public void receivePacket(Packet packet, double distanceTravelled) {
			DebugLogger.debug("Received packet from " + distanceTravelled + " blocks away");
		}

		@Override
		public void networkInvalidated(Map<String, IPeripheral> oldPeripherals, Map<String, IPeripheral> newPeripherals) {
			DebugLogger.debug(
				"Node invalidated at %s, %s, %s\n - Old: %s\n - New: %s",
				pos.getX(), pos.getY(), pos.getZ(),
				StringUtils.join(oldPeripherals.keySet(), ", "),
				StringUtils.join(newPeripherals.keySet(), ", ")
			);
		}
	};

	@Override
	public AbstractWorldNode getNode() {
		return node;
	}
}
