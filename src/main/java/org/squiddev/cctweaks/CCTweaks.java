package org.squiddev.cctweaks;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import dan200.computercraft.ComputerCraft;
import net.minecraft.creativetab.CreativeTabs;
import org.squiddev.cctweaks.core.integration.IntegrationRegistry;
import org.squiddev.cctweaks.core.reference.Config;
import org.squiddev.cctweaks.core.registry.ItemRegistry;
import org.squiddev.cctweaks.core.registry.RefuelRegistry;
import org.squiddev.cctweaks.core.utils.DebugLogger;

@Mod(modid = CCTweaks.ID, name = CCTweaks.NAME, version = CCTweaks.VERSION, dependencies = CCTweaks.DEPENDENCIES, guiFactory = CCTweaks.GUI_FACTORY)
public class CCTweaks {
	public static final String ID = "CCTweaks";
	public static final String NAME = ID;
	public static final String VERSION = "${mod_version}";
	public static final String RESOURCE_DOMAIN = ID.toLowerCase();
	public static final String DEPENDENCIES = "required-after:ComputerCraft;after:CCTurtle;after:ForgeMultipart;";

	public static final String GUI_FACTORY = "org.squiddev.cctweaks.client.gui.GuiConfigFactory";

	public static CreativeTabs getCreativeTab() {
		return ComputerCraft.mainCreativeTab;
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		Config.init(event.getSuggestedConfigurationFile());
		DebugLogger.init(event.getModLog());

		FMLCommonHandler.instance().bus().register(this);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		ItemRegistry.init();
		RefuelRegistry.init();
		IntegrationRegistry.init();
	}

	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
		if (eventArgs.modID.equals(CCTweaks.ID)) {
			Config.sync();
		}
	}
}
