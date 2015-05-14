package org.squiddev.cctweaks.api.peripheral;

/**
 * Used to denote a peripheral that targets a TileEntity or other object in game
 *
 * This can be used from the Java side to access additional methods. We inject this into
 * OpenPeripheral's AdapterPeripheral to enable getting the target TileEntity.
 */
public interface IPeripheralTargeted {
	/**
	 * Get the object this peripheral targets. This is generally a TileEntity
	 *
	 * @return The target
	 */
	Object getTarget();
}
