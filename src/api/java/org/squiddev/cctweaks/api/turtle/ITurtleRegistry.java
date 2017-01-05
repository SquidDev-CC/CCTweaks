package org.squiddev.cctweaks.api.turtle;

import net.minecraft.item.Item;

import javax.annotation.Nonnull;

/**
 * Registry for various turtle features
 */
public interface ITurtleRegistry {
	/**
	 * Register an interaction
	 *
	 * @param interaction The interaction to use
	 */
	void registerInteraction(@Nonnull ITurtleInteraction interaction);

	/**
	 * Register an interaction for a specific item
	 *
	 * @param item        The item to use
	 * @param interaction The interaction to use
	 */
	void registerInteraction(@Nonnull Item item, @Nonnull ITurtleInteraction interaction);
}
