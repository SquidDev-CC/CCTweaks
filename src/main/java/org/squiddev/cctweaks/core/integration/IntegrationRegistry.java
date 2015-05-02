package org.squiddev.cctweaks.core.integration;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModAPIManager;
import org.squiddev.cctweaks.core.integration.multipart.MultipartIntegration;
import org.squiddev.cctweaks.core.registry.IRegisterable;

import java.util.HashSet;
import java.util.Set;

/**
 * Custom integration module
 */
public class IntegrationRegistry {
	public interface IIntegrationModule extends IRegisterable {
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

	public static abstract class APIIntegrationModule implements IIntegrationModule {
		public final String apiName;

		public APIIntegrationModule(String modName) {
			this.apiName = modName;
		}

		@Override
		public boolean canLoad() {
			return ModAPIManager.INSTANCE.hasAPI(apiName);
		}
	}

	private static final Set<IIntegrationModule> modules = new HashSet<IIntegrationModule>();
	private static boolean preInit = false;
	private static boolean init = false;

	public static void addModule(IIntegrationModule module) {
		modules.add(module);

		if (preInit && module.canLoad()) {
			module.preInit();
			if (init) module.init();
		}
	}

	public static void preInit() {
		preInit = true;
		for (IIntegrationModule module : modules) {
			if (module.canLoad()) module.preInit();
		}
	}

	public static void init() {
		init = true;
		for (IIntegrationModule module : modules) {
			if (module.canLoad()) module.init();
		}
	}

	static {
		addModule(new MultipartIntegration.MultipartIntegrationWrapper());
		addModule(new RedstoneFlux());
	}
}
