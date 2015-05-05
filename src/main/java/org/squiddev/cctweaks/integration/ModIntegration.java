package org.squiddev.cctweaks.integration;

import cpw.mods.fml.common.Loader;
import org.squiddev.cctweaks.core.registry.IModule;

/**
 * A module that is loaded when a mod is installed
 */
public abstract class ModIntegration implements IModule {
	public final String modName;

	public ModIntegration(String modName) {
		this.modName = modName;
	}

	@Override
	public boolean canLoad() {
		return Loader.isModLoaded(modName);
	}
}
