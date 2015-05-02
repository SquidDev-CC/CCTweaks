package org.squiddev.cctweaks.core.registry;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import org.squiddev.cctweaks.core.blocks.BaseBlock;
import org.squiddev.cctweaks.core.integration.IndustrialCraftIntegration;
import org.squiddev.cctweaks.core.integration.RedstoneFluxIntegration;
import org.squiddev.cctweaks.core.integration.multipart.MultipartIntegration;
import org.squiddev.cctweaks.core.items.BaseItem;
import org.squiddev.cctweaks.core.items.ComputerUpgradeItem;
import org.squiddev.cctweaks.core.items.DataCardItem;
import org.squiddev.cctweaks.core.items.DebuggerItem;
import org.squiddev.cctweaks.core.network.bridge.WirelessBridgeBlock;

import java.util.HashSet;
import java.util.Set;

/**
 * The proxy class
 */
public final class Registry {
	public static final ComputerUpgradeItem itemComputerUpgrade;
	public static final DebuggerItem itemDebugger;
	public static final BaseItem itemNetworkBinder;

	public static final BaseBlock blockWirelessBridge;

	private static final Set<IModule> modules = new HashSet<IModule>();

	private static boolean preInit = false;
	private static boolean init = false;

	static {
		addModule(itemComputerUpgrade = new ComputerUpgradeItem());
		addModule(itemDebugger = new DebuggerItem());

		addModule(itemNetworkBinder = new DataCardItem());
		addModule(blockWirelessBridge = new WirelessBridgeBlock());

		addModule(new MultipartIntegration());

		addModule(new TurtleRegistry());
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
		preInit = true;
		for (IModule module : modules) {
			if (module.canLoad()) module.preInit();
		}
	}

	public static void init() {
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
