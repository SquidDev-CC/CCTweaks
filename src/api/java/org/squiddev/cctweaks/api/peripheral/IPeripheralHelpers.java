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

	/**
	 * Gets the target peripheral for this peripheral.
	 *
	 * If it does not implement {@link IPeripheralTargeted} then it will attempt to find
	 * the parent peripheral using {@link IPeripheralProxy} and start the search again.
	 *
	 * @param peripheral The peripheral to find
	 * @return The target or the last peripheral we found
	 */
	Object getTarget(IPeripheral peripheral);
}
