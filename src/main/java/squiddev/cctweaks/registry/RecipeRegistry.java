package squiddev.cctweaks.registry;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import squiddev.cctweaks.crafting.ComputerUpgradeCrafting;
import squiddev.cctweaks.reference.Config;
import cpw.mods.fml.common.registry.GameRegistry;

public final class RecipeRegistry {
	public static void init(){
		if(Config.enableItemComputerUpgrades){
			GameRegistry.addRecipe(new ItemStack(ItemRegistry.itemComputerUpgrade), "GGG", "G G", "GSG", 'G', Items.gold_ingot, 'S', Blocks.stone);
			GameRegistry.addRecipe(new ComputerUpgradeCrafting());
		}
	}
}