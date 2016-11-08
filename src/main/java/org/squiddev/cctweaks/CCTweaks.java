package org.squiddev.cctweaks;

import dan200.computercraft.ComputerCraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.squiddev.cctweaks.core.McEvents;
import org.squiddev.cctweaks.core.network.bridge.NetworkBindings;
import org.squiddev.cctweaks.core.registry.Registry;
import org.squiddev.cctweaks.core.visualiser.NetworkPlayerWatcher;
import org.squiddev.cctweaks.lua.lib.ComputerMonitor;
import org.squiddev.cctweaks.lua.lib.DelayedTasks;

@Mod(modid = CCTweaks.ID, name = CCTweaks.NAME, version = CCTweaks.VERSION, dependencies = CCTweaks.DEPENDENCIES, guiFactory = CCTweaks.GUI_FACTORY)
public class CCTweaks {
	public static final String ID = "cctweaks";
	public static final String NAME = "CCTweaks";
	public static final String VERSION = "${mod_version}";
	public static final String DEPENDENCIES = "required-after:ComputerCraft@[${cc_version},);after:CCTurtle;";

	public static final String ROOT_NAME = "org.squiddev.cctweaks.";
	public static final String GUI_FACTORY = ROOT_NAME + "client.gui.GuiConfigFactory";

	@Mod.Instance
	public static CCTweaks instance;

	public static SimpleNetworkWrapper network;

	public static CreativeTabs getCreativeTab() {
		return ComputerCraft.mainCreativeTab;
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(new McEvents());

		network = NetworkRegistry.INSTANCE.newSimpleChannel(ID);

		Registry.preInit();
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		Registry.init();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		Registry.postInit();
	}

	@EventHandler
	public void onServerStarting(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandCCTweaks(event.getServer().isDedicatedServer()));
	}

	@EventHandler
	public void onServerStart(FMLServerStartedEvent event) {
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
			DelayedTasks.reset();
			NetworkBindings.reset();
			NetworkPlayerWatcher.reset();
		}
	}

	@EventHandler
	public void onServerStopped(FMLServerStoppedEvent event) {
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
			DelayedTasks.reset();
			NetworkBindings.reset();
			NetworkPlayerWatcher.reset();

			if (ComputerMonitor.get() != null) ComputerMonitor.stop();
		}
	}
}
