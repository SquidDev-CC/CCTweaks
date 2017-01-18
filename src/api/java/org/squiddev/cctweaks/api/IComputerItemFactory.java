package org.squiddev.cctweaks.api;

import dan200.computercraft.shared.computer.core.ComputerFamily;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

/**
 * Implemented on {@link net.minecraft.item.Item}s to allow building stacks with specific attributes.
 */
public interface IComputerItemFactory {
	/**
	 * Create a computer with a given ID
	 *
	 * @param id     The id of the computer to create.
	 * @param label  Optional label for the computer
	 * @param family The family of the computer to create. Will be once of the items in {@link #getSupportedFamilies()}.
	 * @return The built item stack.
	 */
	@Nonnull
	ItemStack createComputer(int id, @Nullable String label, @Nonnull ComputerFamily family);

	@Nonnull
	Set<ComputerFamily> getSupportedFamilies();

	@Nonnull
	ComputerFamily getDefaultFamily();
}
