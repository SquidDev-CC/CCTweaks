package org.squiddev.cctweaks.api.peripheral;

import dan200.computercraft.api.peripheral.IPeripheral;

import javax.annotation.Nullable;

/**
 * Used to denote a peripheral that targets a TileEntity or other object in game
 *
 * You can get the target of a peripheral using {@link IPeripheralHelpers#getTarget(IPeripheral)}.
 */
public interface IPeripheralTargeted extends IPeripheral {
	/**
	 * Get the object this peripheral targets. This is generally a TileEntity
	 *
	 * @return The target
	 */
	@Nullable
	Object getTarget();
}
