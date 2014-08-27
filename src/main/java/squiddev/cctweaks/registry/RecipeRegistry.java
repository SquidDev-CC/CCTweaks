package squiddev.cctweaks.registry;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import squiddev.cctweaks.crafting.ComputerUpgradeCrafting;
import squiddev.cctweaks.reference.Config;
import cpw.mods.fml.common.registry.GameRegistry;

public final class RecipeRegistry {
	public static void init(){
		if(Config.enableItemComputerUpgrades){
			GameRegistry.addRecipe(new ItemStack(ItemRegistry.itemComputerUpgrade), "GGG", "G G", "GSG", 'G', Item.ingotGold, 'S', Block.stone);
			GameRegistry.addRecipe(new ComputerUpgradeCrafting());
		}
	}
}