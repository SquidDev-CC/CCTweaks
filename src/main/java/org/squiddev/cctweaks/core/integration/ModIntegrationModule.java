package org.squiddev.cctweaks.core.integration;

import cpw.mods.fml.common.Loader;
import org.squiddev.cctweaks.core.registry.IModule;

/**
 * A module that is loaded when a mod is installed
 */
public abstract class ModIntegrationModule implements IModule {
	public final String modName;

	public ModIntegrationModule(String modName) {
		this.modName = modName;
	}

	@Override
	public boolean canLoad() {
		return Loader.isModLoaded(modName);
	}
}
