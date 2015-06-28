package org.squiddev.cctweaks.blocks.debug;

import dan200.computercraft.api.peripheral.IPeripheral;
import org.apache.commons.lang3.StringUtils;
import org.squiddev.cctweaks.api.network.INetworkAccess;
import org.squiddev.cctweaks.api.network.INetworkedPeripheral;
import org.squiddev.cctweaks.api.network.Packet;
import org.squiddev.cctweaks.core.utils.DebugLogger;

import java.util.Map;

/**
 * A networked peripheral that logs events to the console
 */
public class TileDebugNetworkedPeripheral extends TileDebugPeripheral {
	@Override
	protected IPeripheral createPeripheral(int side) {
		return new NetworkedPeripheral(side);
	}

	public class NetworkedPeripheral extends SidedPeripheral implements INetworkedPeripheral {
		public NetworkedPeripheral(int side) {
			super(side);
		}

		@Override
		public String getType() {
			return "networked";
		}

		@Override
		public void attachToNetwork(INetworkAccess network, String name) {
			DebugLogger.debug("Attaching to network " + network + " with name " + name);
		}

		@Override
		public void detachFromNetwork(INetworkAccess network, String name) {
			DebugLogger.debug("Detaching from network " + network + " with name " + name);
		}

		@Override
		public void networkInvalidated(INetworkAccess network, Map<String, IPeripheral> oldPeripherals, Map<String, IPeripheral> newPeripherals) {
			DebugLogger.debug(
				"Node invalidated at %s, %s, %s\n - Old: %s\n - New: %s",
				xCoord, yCoord, zCoord,
				StringUtils.join(oldPeripherals.keySet(), ", "),
				StringUtils.join(newPeripherals.keySet(), ", ")
			);
		}

		@Override
		public void receivePacket(INetworkAccess network, Packet packet, double distanceTravelled) {
			DebugLogger.debug("Received packet from " + distanceTravelled + " blocks away");
		}
	}
}
