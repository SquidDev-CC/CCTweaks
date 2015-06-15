package org.squiddev.cctweaks.items;

import cpw.mods.fml.common.registry.GameRegistry;
import dan200.computercraft.ComputerCraft;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.turtle.TurtleUpgradeToolHost;

/**
 * Simply a holder item for the turtle tool host
 */
public class ItemToolHost extends ItemBase {
	public ItemToolHost() {
		super("toolHost");
	}

	@Override
	public void init() {
		super.init();

		if (Config.Turtle.ToolHost.enabled) {
			if (Config.Turtle.ToolHost.crafting) {
				GameRegistry.addRecipe(new ItemStack(this),
					"GDG",
					"DOD",
					"GDG",

					'G', Items.gold_ingot,
					'D', Items.diamond,
					'O', Blocks.obsidian
				);
			}

			ComputerCraft.registerTurtleUpgrade(new TurtleUpgradeToolHost());
		}
	}
}
