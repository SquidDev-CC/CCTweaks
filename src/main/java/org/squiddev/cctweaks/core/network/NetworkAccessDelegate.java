package org.squiddev.cctweaks.core.network;

import dan200.computercraft.api.peripheral.IPeripheral;
import org.squiddev.cctweaks.api.network.INetworkAccess;
import org.squiddev.cctweaks.api.network.Packet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A {@link INetworkAccess} implementation that delegates to other networks
 */
public class NetworkAccessDelegate implements INetworkAccess {
	protected final Set<INetworkAccess> networks = new HashSet<INetworkAccess>();

	@Override
	public Map<String, IPeripheral> peripheralsByName() {
		// We can't cache this at all as we can't receive network changed events
		Map<String, IPeripheral> peripherals = new HashMap<String, IPeripheral>();
		for (INetworkAccess network : networks) {
			peripherals.putAll(network.peripheralsByName());
		}
		return peripherals;
	}

	@Override
	public void invalidateNetwork() {
		for (INetworkAccess network : networks) {
			network.invalidateNetwork();
		}
	}

	@Override
	public boolean transmitPacket(Packet packet) {
		// To consider, should we always fail if one fails, or succeed if one succeeds?

		boolean success = false;
		for (INetworkAccess network : networks) {
			success |= network.transmitPacket(packet);
		}
		return success;
	}

	public void add(INetworkAccess network) {
		networks.add(network);
	}

	public void remove(INetworkAccess network) {
		networks.remove(network);
	}
}
