package org.squiddev.cctweaks.blocks.debug;

import dan200.computercraft.api.peripheral.IPeripheral;
import org.apache.commons.lang3.StringUtils;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.api.network.Packet;
import org.squiddev.cctweaks.blocks.network.TileNetworked;
import org.squiddev.cctweaks.core.network.AbstractWorldNode;
import org.squiddev.cctweaks.core.utils.DebugLogger;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * A network node that logs events to the console
 */
public class TileDebugNode extends TileNetworked {
	protected final AbstractWorldNode node = new AbstractWorldNode() {
		@Nonnull
		@Override
		public IWorldPosition getPosition() {
			return TileDebugNode.this;
		}

		@Override
		public void receivePacket(@Nonnull Packet packet, double distanceTravelled) {
			DebugLogger.debug("Received packet from " + distanceTravelled + " blocks away");
		}

		@Override
		public void networkInvalidated(@Nonnull Map<String, IPeripheral> oldPeripherals, @Nonnull Map<String, IPeripheral> newPeripherals) {
			DebugLogger.debug(
				"Node invalidated at %s, %s, %s\n - Removed: %s\n - Added:   %s",
				pos.getX(), pos.getY(), pos.getZ(),
				StringUtils.join(oldPeripherals.keySet(), ", "),
				StringUtils.join(newPeripherals.keySet(), ", ")
			);
		}
	};

	@Nonnull
	@Override
	public AbstractWorldNode getNode() {
		return node;
	}
}
