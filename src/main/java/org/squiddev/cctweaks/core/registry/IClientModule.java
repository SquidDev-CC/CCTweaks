package org.squiddev.cctweaks.core.registry;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * A module that adds custom client functionality
 */
public interface IClientModule extends IModule {
	/**
	 * Register custom handlers on the client
	 */
	@SideOnly(Side.CLIENT)
	void clientInit();
}
