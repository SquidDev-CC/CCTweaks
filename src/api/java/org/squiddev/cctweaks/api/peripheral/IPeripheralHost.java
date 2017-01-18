package org.squiddev.cctweaks.api.peripheral;

import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An object that hosts a {@link IPeripheral}.
 *
 * Instead of implementing {@link IPeripheral} and delegating methods to it,
 * you can implement this interface instead. This is supported for TileEntities
 * and multiparts.
 */
public interface IPeripheralHost {
	/**
	 * Get this host's peripheral
	 *
	 * @param side The side to get the peripheral from
	 * @return The peripheral this object holds
	 */
	@Nullable
	IPeripheral getPeripheral(@Nonnull EnumFacing side);
}
