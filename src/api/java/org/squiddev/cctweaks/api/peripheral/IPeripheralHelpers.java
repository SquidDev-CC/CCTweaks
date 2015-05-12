package org.squiddev.cctweaks.api.peripheral;

import dan200.computercraft.api.peripheral.IPeripheral;

/**
 * Useful helpers for peripherals
 */
public interface IPeripheralHelpers {
	/**
	 * Get the base peripheral by following a chain of {@link IPeripheralProxy}s.
	 *
	 * @param peripheral The peripheral
	 * @return The base peripheral
	 */
	IPeripheral getBasePeripheral(IPeripheral peripheral);
}
