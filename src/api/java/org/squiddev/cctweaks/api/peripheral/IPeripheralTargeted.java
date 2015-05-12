package org.squiddev.cctweaks.api.peripheral;

import dan200.computercraft.api.peripheral.IPeripheral;

/**
 * Used to denote a peripheral that targets a TileEntity or other object in game
 *
 * This can be used from the Java side to access additional methods. We inject this into
 * OpenPeripheral's AdapterPeripheral to enable getting the target TileEntity. It is also
 * recommended you implement it on the peripheral if you use {@link IPeripheralHost}
 */
public interface IPeripheralTargeted extends IPeripheral {
	/**
	 * Get the object this peripheral targets. This is generally a TileEntity
	 *
	 * @return The target
	 */
	Object getTarget();
}
