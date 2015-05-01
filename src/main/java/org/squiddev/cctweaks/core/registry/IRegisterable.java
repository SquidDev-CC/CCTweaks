package org.squiddev.cctweaks.core.registry;

/**
 * An item that can be registered
 */
public interface IRegisterable {
	/**
	 * @see cpw.mods.fml.common.Mod.EventHandler
	 */
	void preInit();

	/**
	 * @see cpw.mods.fml.common.Mod.EventHandler
	 */
	void init();
}
