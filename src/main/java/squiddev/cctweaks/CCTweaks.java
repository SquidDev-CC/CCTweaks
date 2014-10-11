package squiddev.cctweaks;

import net.minecraft.creativetab.CreativeTabs;
import squiddev.cctweaks.interfaces.IProxy;
import squiddev.cctweaks.reference.Config;
import squiddev.cctweaks.reference.ModInfo;
import squiddev.cctweaks.registry.ItemRegistry;
import squiddev.cctweaks.registry.RecipeRegistry;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import squiddev.cctweaks.utils.DebugLogger;

@Mod(modid = ModInfo.ID, name = ModInfo.NAME, version = ModInfo.VERSION, dependencies = ModInfo.DEPENDENCIES)
public class CCTweaks {

	// The instance of your mod that Forge uses.
	@Instance(value = ModInfo.ID)
	public static CCTweaks instance;

	// Says where the client and server 'proxy' code is loaded.
	@SidedProxy(clientSide = ModInfo.PROXY_CLIENT, serverSide = ModInfo.PROXY_SERVER)
	public static IProxy proxy;

	public static CreativeTabs creativeTab = new CCTweaksCreativeTab();

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		Config.ConfigHandler.init(event.getSuggestedConfigurationFile());
		DebugLogger.init(event.getModLog());
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		ItemRegistry.init();
		RecipeRegistry.init();

		proxy.registerRenderInfo();
	}

	@EventHandler // used in 1.6.2
	public void postInit(FMLPostInitializationEvent event) {
		// Stub Method
	}
}