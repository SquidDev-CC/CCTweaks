package org.squiddev.cctweaks.core.registry;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.squiddev.cctweaks.blocks.debug.BlockDebug;
import org.squiddev.cctweaks.blocks.network.BlockNetworked;
import org.squiddev.cctweaks.client.ModelLoader;
import org.squiddev.cctweaks.client.render.RenderNetworkOverlay;
import org.squiddev.cctweaks.client.render.RenderSquidOverlay;
import org.squiddev.cctweaks.core.block.VanillaRotationHandlers;
import org.squiddev.cctweaks.core.peripheral.PeripheralHostProvider;
import org.squiddev.cctweaks.core.pocket.CraftingPocketUpgrade;
import org.squiddev.cctweaks.core.turtle.DefaultTurtleProviders;
import org.squiddev.cctweaks.core.visualiser.NetworkPlayerWatcher;
import org.squiddev.cctweaks.integration.IndustrialCraftIntegration;
import org.squiddev.cctweaks.integration.RedstoneFluxIntegration;
import org.squiddev.cctweaks.integration.multipart.MultipartIntegration;
import org.squiddev.cctweaks.items.ItemComputerUpgrade;
import org.squiddev.cctweaks.items.ItemDataCard;
import org.squiddev.cctweaks.items.ItemDebugger;
import org.squiddev.cctweaks.items.ItemToolHost;
import org.squiddev.cctweaks.lua.lib.ApiRegister;
import org.squiddev.cctweaks.pocket.PocketEnderModem;
import org.squiddev.cctweaks.pocket.PocketWirelessBridge;
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
	public static final ItemToolHost itemToolHost;

	public static final BlockNetworked blockNetworked;
	public static final BlockDebug blockDebug;

	private static final Set<IModule> modules = new HashSet<IModule>();

	private static boolean preInit = false;
	private static boolean init = false;
	private static boolean postInit = false;

	static {
		addModule(itemComputerUpgrade = new ItemComputerUpgrade());
		addModule(itemDebugger = new ItemDebugger());
		addModule(itemToolHost = new ItemToolHost());

		addModule(itemDataCard = new ItemDataCard());
		addModule(blockNetworked = new BlockNetworked());

		addModule(blockDebug = new BlockDebug());

		// CC Providers
		addModule(new PeripheralHostProvider());
		addModule(new Module() {
			@Override
			public void init() {
				ApiRegister.init();
			}
		});
		addModule(new TurtleUpgradeWirelessBridge());

		// CCTweaks providers
		addModule(new DefaultTurtleProviders());
		addModule(new PocketWirelessBridge());
		addModule(new PocketEnderModem());
		addModule(new CraftingPocketUpgrade());
		addModule(new VanillaRotationHandlers());

		// Integration
		addModule(new RedstoneFluxIntegration());
		addModule(new IndustrialCraftIntegration());
		addModule(new MultipartIntegration());

		addModule(new NetworkPlayerWatcher());
		addModule(new RenderNetworkOverlay());

		addModule(new ModelLoader());

		addModule(new RenderSquidOverlay());
	}

	public static void addModule(IModule module) {
		if (module instanceof IClientModule) {
			module = new RegisterWrapperClient((IClientModule) module);
		}

		modules.add(module);

		if (preInit && module.canLoad()) {
			module.preInit();
			if (init) {
				module.init();
				if (postInit) module.postInit();
			}
		}
	}


	public static void preInit() {
		if (preInit) throw new IllegalStateException("Attempting to preInit twice");
		preInit = true;
		for (IModule module : modules) {
			if (module.canLoad()) module.preInit();
		}
	}

	public static void init() {
		if (!preInit) throw new IllegalStateException("Cannot init before preInit");
		if (init) throw new IllegalStateException("Attempting to init twice");

		init = true;
		for (IModule module : modules) {
			if (module.canLoad()) module.init();
		}
	}

	public static void postInit() {
		if (!preInit) throw new IllegalStateException("Cannot init before preInit");
		if (!init) throw new IllegalStateException("Cannot postInit before init");
		if (postInit) throw new IllegalStateException("Attempting to postInit twice");

		postInit = true;
		for (IModule module : modules) {
			if (module.canLoad()) module.postInit();
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

		@Override
		public void postInit() {
			base.postInit();
		}

		@Override
		public String toString() {
			return base.toString();
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

		@Override
		public String toString() {
			return base.toString();
		}
	}
}

