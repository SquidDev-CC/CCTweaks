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
	boolean canLoad();

	/**
	 * @see Mod.EventHandler
	 */
	void preInit();

	/**
	 * @see Mod.EventHandler
	 */
	void init();

	/**
	 * @see Mod.EventHandler
	 */
	void postInit();
}
