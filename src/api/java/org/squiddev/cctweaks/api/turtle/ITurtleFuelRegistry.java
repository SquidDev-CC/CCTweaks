package org.squiddev.cctweaks.api.turtle;

import dan200.computercraft.api.turtle.ITurtleAccess;
import net.minecraft.item.ItemStack;

/**
 * Register custom fuel providers
 */
public interface ITurtleFuelRegistry {
	/**
	 * Add a fuel provider.
	 *
	 * Ideally this should be done in the init stages of a mod, though it doesn't matter too much.
	 *
	 * @param provider The fuel provider to register with
	 */
	void addFuelProvider(ITurtleFuelProvider provider);

	/**
	 * Get a provider for this fuel
	 *
	 * @param turtle The turtle we will refuel
	 * @param stack  The fuel to refuel with
	 * @return The provider, or {@code null} if none found.
	 * @see ITurtleFuelProvider#canRefuel(ITurtleAccess, ItemStack)
	 */
	ITurtleFuelProvider getProvider(ITurtleAccess turtle, ItemStack stack);
}
