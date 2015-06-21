package org.squiddev.cctweaks;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import dan200.computercraft.ComputerCraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.common.MinecraftForge;
import org.squiddev.cctweaks.core.FmlEvents;
import org.squiddev.cctweaks.core.McEvents;
import org.squiddev.cctweaks.core.registry.Registry;

@Mod(modid = CCTweaks.ID, name = CCTweaks.NAME, version = CCTweaks.VERSION, dependencies = CCTweaks.DEPENDENCIES, guiFactory = CCTweaks.GUI_FACTORY)
public class CCTweaks {
	public static final String ID = "CCTweaks";
	public static final String NAME = ID;
	public static final String VERSION = "${mod_version}";
	public static final String RESOURCE_DOMAIN = ID.toLowerCase();
	public static final String DEPENDENCIES = "required-after:ComputerCraft;after:CCTurtle;after:ForgeMultipart;after:OpenPeripheralCore;";

	public static final String ROOT_NAME = "org.squiddev.cctweaks.";
	public static final String GUI_FACTORY = ROOT_NAME + "client.gui.GuiConfigFactory";

	public static CreativeTabs getCreativeTab() {
		return ComputerCraft.mainCreativeTab;
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		FMLCommonHandler.instance().bus().register(new FmlEvents());
		MinecraftForge.EVENT_BUS.register(new McEvents());

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
}
