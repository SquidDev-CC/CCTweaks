package org.squiddev.cctweaks.core.integration;

import cpw.mods.fml.common.Loader;
import org.squiddev.cctweaks.core.integration.multipart.MultipartIntegration;

import java.util.HashSet;
import java.util.Set;

/**
 * Custom integration module
 */
public class IntegrationRegistry {
	public interface IIntegrationModule {
		void load();

		boolean canLoad();
	}

	public static abstract class ModIntegrationModule implements IIntegrationModule {
		public final String modName;

		public ModIntegrationModule(String modName) {
			this.modName = modName;
		}

		@Override
		public boolean canLoad() {
			return Loader.isModLoaded(modName);
		}
	}

	private static final Set<IIntegrationModule> modules = new HashSet<IIntegrationModule>();

	public static void addModule(IIntegrationModule module) {
		modules.add(module);
	}

	public static void init() {
		addModule(new MultipartIntegration());

		for (IIntegrationModule module : modules) {
			if (module.canLoad()) module.load();
		}
	}
}
