package org.squiddev.cctweaks.api.network;

import dan200.computercraft.api.peripheral.IPeripheral;

import java.util.Map;

/**
 * Access object for a computer network
 */
public interface INetworkAccess {
	/**
	 * Gets all peripherals on the network mapped by their names.
	 * @return The map of these peripherals.
	 */
	Map<String, IPeripheral> peripheralsByName();

	/**
	 * Invalidates the network.
	 * Forces peripherals to be recalculated.
	 */
	void invalidateNetwork();
}
