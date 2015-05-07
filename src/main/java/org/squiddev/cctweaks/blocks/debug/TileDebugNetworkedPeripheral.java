package org.squiddev.cctweaks.blocks.debug;

import dan200.computercraft.api.peripheral.IPeripheral;
import org.squiddev.cctweaks.api.network.INetworkAccess;
import org.squiddev.cctweaks.api.network.INetworkedPeripheral;
import org.squiddev.cctweaks.api.network.Packet;
import org.squiddev.cctweaks.core.utils.DebugLogger;

/**
 * A networked peripheral that logs events to the console
 */
public class TileDebugNetworkedPeripheral extends TileDebugPeripheral {
	@Override
	protected IPeripheral createPeripheral(int side) {
		return new NetworkedPeripheral(side);
	}

	public static class NetworkedPeripheral extends SidedPeripheral implements INetworkedPeripheral {
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
		public void networkInvalidated(INetworkAccess network) {
			DebugLogger.debug("Network was invalidated");
		}

		@Override
		public void receivePacket(Packet packet, int distanceTravelled) {
			DebugLogger.debug("Received packet from " + distanceTravelled + " blocks away");
		}
	}
}
