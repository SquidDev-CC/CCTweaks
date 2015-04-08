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
import org.squiddev.cctweaks.core.reference.Config;
import org.squiddev.cctweaks.core.reference.ModInfo;
import org.squiddev.cctweaks.core.registry.ItemRegistry;
import org.squiddev.cctweaks.core.registry.RefuelRegistry;
import org.squiddev.cctweaks.core.utils.DebugLogger;

@Mod(modid = ModInfo.ID, name = ModInfo.NAME, version = ModInfo.VERSION, dependencies = ModInfo.DEPENDENCIES, guiFactory = ModInfo.GUI_FACTORY)
public class CCTweaks {
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
	}

	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
		if (eventArgs.modID.equals(ModInfo.ID)) {
			Config.sync();
		}
	}
}
