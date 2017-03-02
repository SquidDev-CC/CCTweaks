package org.squiddev.cctweaks.api.computer;

import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

/**
 * A computer item which can be crafted with a disk in order to set a custom ROM.
 *
 * The id used is an that of a disk (and so refers to a folder in the /computercraft/disks directory).
 *
 * This will generate several recipes - you should avoid registering similar recipes in order to avoid conflicts.
 * - Crafting with a disk in order to set the ROM
 * - Crafting without a disk in order to remove the ROM
 *
 * When creating a computer, you should use {@link IExtendedServerComputer#setCustomRom(int)} in order to set the ROM
 * id.
 */
public interface ICustomRomItem {
	/**
	 * Return whether this item has a custom ROM.
	 *
	 * @param stack The stack to check.
	 * @return Whether this stack has a custom ROM.
	 */
	boolean hasCustomRom(@Nonnull ItemStack stack);

	/**
	 * Get the custom ROM id.
	 *
	 * This should only be called if {@link #hasCustomRom(ItemStack)} is true.
	 *
	 * @param stack The stack to get the ROM from.
	 * @return The custom ROM id.
	 */
	int getCustomRom(@Nonnull ItemStack stack);

	/**
	 * Remove the custom ROM from this stack.
	 *
	 * @param stack The stack to clear.
	 */
	void clearCustomRom(@Nonnull ItemStack stack);

	/**
	 * Set the custom ROM for this stack.
	 *
	 * @param stack The stack to set.
	 * @param id    The custom ROM id to set it to.
	 */
	void setCustomRom(@Nonnull ItemStack stack, int id);
}
