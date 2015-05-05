package org.squiddev.cctweaks.integration;

import cpw.mods.fml.common.ModAPIManager;
import org.squiddev.cctweaks.core.registry.IModule;

/**
 * A module that is loaded when an API is on the class path
 */
public abstract class APIIntegration implements IModule {
	public final String apiName;

	public APIIntegration(String modName) {
		this.apiName = modName;
	}

	@Override
	public boolean canLoad() {
		return ModAPIManager.INSTANCE.hasAPI(apiName);
	}
}
