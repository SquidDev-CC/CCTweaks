package org.squiddev.cctweaks.core.registry;

import net.minecraftforge.fml.common.Mod;

/**
 * An item that can be registered
 */
public interface IModule {
	/**
	 * Can this module be loaded
	 *
	 * @return If this module should be loaded
	 */
	default boolean canLoad() {
		return true;
	}

	/**
	 * @see Mod.EventHandler
	 */
	default void preInit() {
	}

	/**
	 * @see Mod.EventHandler
	 */
	default void init() {
	}

	/**
	 * @see Mod.EventHandler
	 */
	default void postInit() {
	}
}
