package squiddev.cctweaks.core.registry;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import squiddev.cctweaks.core.crafting.ComputerUpgradeCrafting;
import squiddev.cctweaks.core.items.ItemBase;
import squiddev.cctweaks.core.items.ItemComputerUpgrade;
import squiddev.cctweaks.core.items.ItemDebugger;
import squiddev.cctweaks.core.reference.Config;

public class ItemRegistry {
	public static ItemBase itemComputerUpgrade;
	public static ItemBase itemDebugger;

	public static void init() {
		if (Config.config.enableComputerUpgrades) {
			itemComputerUpgrade = new ItemComputerUpgrade();
			itemComputerUpgrade.registerItem();

			GameRegistry.addRecipe(new ItemStack(ItemRegistry.itemComputerUpgrade), "GGG", "GSG", "GSG", 'G', Items.gold_ingot, 'S', Blocks.stone);
			GameRegistry.addRecipe(new ComputerUpgradeCrafting());
		}

		if(Config.config.enableDebugWand) {
			itemDebugger = new ItemDebugger();
			itemDebugger.registerItem();
		}
	}
}
