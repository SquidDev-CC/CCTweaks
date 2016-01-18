package org.squiddev.cctweaks.api.pocket;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.util.Set;

/**
 * Wrapper class for pocket computers
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
	 * Get a list of all upgrades for the pocket computer
	 *
	 * @return A collection of all upgrade names
	 */
	Set<ResourceLocation> getUpgrades();
}
