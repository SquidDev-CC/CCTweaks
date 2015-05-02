package org.squiddev.cctweaks.core.registry;

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
	 * @see cpw.mods.fml.common.Mod.EventHandler
	 */
	void preInit();

	/**
	 * @see cpw.mods.fml.common.Mod.EventHandler
	 */
	void init();
}
