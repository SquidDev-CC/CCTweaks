package org.squiddev.cctweaks.api.peripheral;

import dan200.computercraft.api.peripheral.IPeripheral;

/**
 * Used to denote a peripheral that delegates to another peripheral.
 *
 * This does not automatically delegate, use {@link IPeripheralHost} for that,
 * this is simply a method of getting the base peripheral if additional
 * processing needs to occur on that instead.
 *
 * You should always implement {@link IPeripheral}.
 * You should implement {@link org.squiddev.cctweaks.api.network.INetworkedPeripheral}
 * if you wish to delegate network events.
 */
public interface IPeripheralProxy extends IPeripheral {
	/**
	 * Get the base peripheral for this peripheral
	 *
	 * @return The peripheral this delegates to
	 */
	IPeripheral getBasePeripheral();
}
