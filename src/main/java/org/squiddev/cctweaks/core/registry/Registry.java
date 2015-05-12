package org.squiddev.cctweaks.core.registry;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import org.squiddev.cctweaks.blocks.debug.BlockDebug;
import org.squiddev.cctweaks.blocks.network.BlockNetworked;
import org.squiddev.cctweaks.integration.IndustrialCraftIntegration;
import org.squiddev.cctweaks.integration.RedstoneFluxIntegration;
import org.squiddev.cctweaks.integration.multipart.MultipartIntegration;
import org.squiddev.cctweaks.items.ItemComputerUpgrade;
import org.squiddev.cctweaks.items.ItemDataCard;
import org.squiddev.cctweaks.items.ItemDebugger;
import org.squiddev.cctweaks.turtle.TurtleUpgradeWirelessBridge;

import java.util.HashSet;
import java.util.Set;

/**
 * The proxy class
 */
public final class Registry {
	public static final ItemComputerUpgrade itemComputerUpgrade;
	public static final ItemDebugger itemDebugger;
	public static final ItemDataCard itemDataCard;

	public static final BlockNetworked blockNetworked;

	private static final Set<IModule> modules = new HashSet<IModule>();

	private static boolean preInit = false;
	private static boolean init = false;

	static {
		addModule(itemComputerUpgrade = new ItemComputerUpgrade());
		addModule(itemDebugger = new ItemDebugger());

		addModule(itemDataCard = new ItemDataCard());
		addModule(blockNetworked = new BlockNetworked());

		addModule(new BlockDebug());

		addModule(new MultipartIntegration());

		addModule(new TurtleRegistry());
		addModule(new TurtleUpgradeWirelessBridge());
		addModule(new RedstoneFluxIntegration());
		addModule(new IndustrialCraftIntegration());
	}

	public static void addModule(IModule module) {
		if (module instanceof IClientModule) {
			module = new RegisterWrapperClient((IClientModule) module);
		}

		modules.add(module);

		if (preInit && module.canLoad()) {
			module.preInit();
			if (init) module.init();
		}
	}


	public static void preInit() {
		if(preInit) throw new IllegalStateException("Attempting to preInit twice");
		preInit = true;
		for (IModule module : modules) {
			if (module.canLoad()) module.preInit();
		}
	}

	public static void init() {
		if(!preInit) throw new IllegalStateException("Cannot init before preInit");
		if(init) throw new IllegalStateException("Attempting to init twice");

		init = true;
		for (IModule module : modules) {
			if (module.canLoad()) module.init();
		}
	}

	/**
	 * Magic classes to allow calling client only methods
	 */
	private static class RegisterWrapper implements IModule {
		protected final IClientModule base;

		private RegisterWrapper(IClientModule base) {
			this.base = base;
		}

		@Override
		public boolean canLoad() {
			return base.canLoad();
		}

		@Override
		public void preInit() {
			base.preInit();
		}

		@Override
		public void init() {
			base.init();
		}
	}

	/**
	 * Magic classes to allow calling client only methods
	 */
	private static class RegisterWrapperClient extends RegisterWrapper {
		private RegisterWrapperClient(IClientModule base) {
			super(base);
		}

		@Override
		@SideOnly(Side.CLIENT)
		public void init() {
			super.init();
			base.clientInit();
		}
	}
}
