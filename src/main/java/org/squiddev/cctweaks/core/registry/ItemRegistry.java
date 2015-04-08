package org.squiddev.cctweaks.core.registry;

import cpw.mods.fml.common.registry.GameRegistry;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.items.ComputerItemFactory;
import dan200.computercraft.shared.pocket.items.PocketComputerItemFactory;
import dan200.computercraft.shared.turtle.items.TurtleItemFactory;
import dan200.computercraft.shared.util.ImpostorShapelessRecipe;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.RecipeSorter;
import org.squiddev.cctweaks.core.crafting.ComputerUpgradeCrafting;
import org.squiddev.cctweaks.core.items.ItemBase;
import org.squiddev.cctweaks.core.items.ItemComputerUpgrade;
import org.squiddev.cctweaks.core.items.ItemDebugger;
import org.squiddev.cctweaks.core.reference.Config;
import org.squiddev.cctweaks.core.reference.ModInfo;

public class ItemRegistry {
	public static ItemBase itemComputerUpgrade;
	public static ItemBase itemDebugger;

	public static void init() {
		if (Config.config.enableComputerUpgrades) {
			ItemBase item = itemComputerUpgrade = new ItemComputerUpgrade();
			item.registerItem();

			RecipeSorter.register(ModInfo.RESOURCE_DOMAIN + ":computer_upgrade_crafting", ComputerUpgradeCrafting.class, RecipeSorter.Category.SHAPELESS, "after:minecraft:shapeless");

			ItemStack stack = new ItemStack(item);
			GameRegistry.addRecipe(stack, "GGG", "GSG", "GSG", 'G', Items.gold_ingot, 'S', Blocks.stone);
			GameRegistry.addRecipe(new ComputerUpgradeCrafting());

			// Add some impostor recipes for NEI. We just use CC's default ones
			{
				// Computer
				GameRegistry.addRecipe(new ImpostorShapelessRecipe(
					ComputerItemFactory.create(-1, null, ComputerFamily.Advanced),
					new Object[]{
						ComputerItemFactory.create(-1, null, ComputerFamily.Normal),
						stack
					}
				));

				// Turtle (Is is silly to include every possible upgrade so we just do the normal one)
				GameRegistry.addRecipe(new ImpostorShapelessRecipe(
					TurtleItemFactory.create(-1, null, null, ComputerFamily.Advanced, null, null, 0),
					new Object[]{
						TurtleItemFactory.create(-1, null, null, ComputerFamily.Normal, null, null, 0),
						stack
					}
				));

				// Non-wireless pocket computer
				GameRegistry.addRecipe(new ImpostorShapelessRecipe(
					PocketComputerItemFactory.create(-1, null, ComputerFamily.Advanced, false),
					new Object[]{
						PocketComputerItemFactory.create(-1, null, ComputerFamily.Normal, false),
						stack
					}
				));

				// Wireless pocket computer
				GameRegistry.addRecipe(new ImpostorShapelessRecipe(
					PocketComputerItemFactory.create(-1, null, ComputerFamily.Advanced, true),
					new Object[]{
						PocketComputerItemFactory.create(-1, null, ComputerFamily.Normal, true),
						stack
					}
				));
			}
		}

		if (Config.config.enableDebugWand) {
			itemDebugger = new ItemDebugger();
			itemDebugger.registerItem();
		}
	}
}
