package org.squiddev.cctweaks.api.block;

import net.minecraft.block.Block;

import javax.annotation.Nonnull;

/**
 * A registry for registering rotation handlers.
 */
public interface IRotationRegistry extends IRotationHandler {
	/**
	 * Register a generic rotation handler
	 *
	 * @param handler The handler to register
	 */
	void register(@Nonnull IRotationHandler handler);

	/**
	 * Register a rotation handler for a specific block class
	 *
	 * @param targetClass The class to target. This will not target subclasses.
	 * @param handler     The handler to register.
	 */
	void register(@Nonnull Class<? extends Block> targetClass, @Nonnull IRotationHandler handler);

	/**
	 * Register a rotation handler for a specific block
	 *
	 * @param block   The block to target.
	 * @param handler The handler to register.
	 */
	void register(@Nonnull Block block, @Nonnull IRotationHandler handler);
}
