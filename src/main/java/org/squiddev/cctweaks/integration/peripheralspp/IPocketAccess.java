package org.squiddev.cctweaks.integration.peripheralspp;

import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Map;

/**
 * Wrapper class for pocket computers
 * See https://github.com/austinv11/PeripheralsPlusPlus/issues/97
 */
public interface IPocketAccess {
	/**
	 * Gets the holding entity of this item
	 *
	 * @return The holding entity, may be {@code null}.
	 */
	Entity getEntity();

	/**
	 * Get if the modem light is turned on
	 *
	 * @return If the modem light is turned on
	 */
	boolean getModemLight();

	/**
	 * Turn on/off the modem light
	 *
	 * @param value If the light should be on
	 */
	void setModemLight(boolean value);

	/**
	 * Get the upgrade specific NBT
	 *
	 * @return The upgrade's NBT
	 */
	NBTTagCompound getUpgradeNBTData();

	/**
	 * Mark the item as dirty
	 */
	void updateUpgradeNBTData();

	/**
	 * Get a lookup of all upgrades for the pocket computer
	 *
	 * @return An upgrade id => peripheral lookup
	 */
	Map<Integer, IPeripheral> getUpgrades();
}
