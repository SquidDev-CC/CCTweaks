package org.squiddev.cctweaks.core.registry;

/**
 * Default implementation of {@link IModule}
 */
public abstract class Module implements IModule {
	public boolean canLoad() {
		return true;
	}

	public void preInit() {
	}

	public void init() {
	}

	public void postInit() {
	}
}
