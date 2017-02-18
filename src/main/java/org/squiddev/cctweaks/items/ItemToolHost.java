package org.squiddev.cctweaks.items;

import dan200.computercraft.ComputerCraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.core.utils.Helpers;
import org.squiddev.cctweaks.turtle.TurtleUpgradeToolHost;
import org.squiddev.cctweaks.turtle.TurtleUpgradeToolManipulator;

import java.util.List;

/**
 * Simply a holder item for the turtle tool host
 */
public class ItemToolHost extends ItemBase {
	public ItemToolHost() {
		super("toolHost");
		setHasSubtypes(true);
	}

	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void getSubItems(Item item, CreativeTabs tab, List list) {
		list.add(new ItemStack(this, 1, 0));
		list.add(new ItemStack(this, 1, 1));
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		switch (stack.getItemDamage()) {
			case 0:
			default:
				return getUnlocalizedName();
			case 1:
				return getUnlocalizedName() + ".advanced";
		}
	}

	@Override
	public int getMetadata(int metadata) {
		return metadata;
	}

	@Override
	public void init() {
		super.init();

		if (Config.Turtle.ToolHost.enabled) {
			if (Config.Turtle.ToolHost.crafting) {
				GameRegistry.addRecipe(new ItemStack(this, 1, 0),
					"GDG",
					"DOD",
					"GDG",

					'G', Items.GOLD_INGOT,
					'D', Items.DIAMOND,
					'O', Blocks.OBSIDIAN
				);
			}

			ComputerCraft.registerTurtleUpgrade(new TurtleUpgradeToolHost());

			if (Config.Turtle.ToolHost.advanced) {
				GameRegistry.addRecipe(new ItemStack(this, 1, 1),
					"GDG",
					"DOD",
					"GDG",

					'G', Items.GOLD_INGOT,
					'D', Items.DIAMOND,
					'O', new ItemStack(this, 1, 0)
				);

				ComputerCraft.registerTurtleUpgrade(new TurtleUpgradeToolManipulator());
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void clientPreInit() {
		Helpers.setupModel(this, 0, name);
		Helpers.setupModel(this, 1, "toolHostAdvanced");
	}
}
