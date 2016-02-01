package org.squiddev.cctweaks.items;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dan200.computercraft.ComputerCraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.turtle.TurtleUpgradeToolHost;
import org.squiddev.cctweaks.turtle.TurtleUpgradeToolManipulator;

import java.util.List;

/**
 * Simply a holder item for the turtle tool host
 */
public class ItemToolHost extends ItemBase {
	@SideOnly(Side.CLIENT)
	public IIcon advanced;

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
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister register) {
		super.registerIcons(register);
		advanced = register.registerIcon(getIconString() + "Advanced");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int metadata) {
		switch (metadata) {
			case 0:
			default:
				return itemIcon;
			case 1:
				return advanced;

		}
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

					'G', Items.gold_ingot,
					'D', Items.diamond,
					'O', Blocks.obsidian
				);
			}

			ComputerCraft.registerTurtleUpgrade(new TurtleUpgradeToolHost());

			if (Config.Turtle.ToolHost.advanced) {
				GameRegistry.addRecipe(new ItemStack(this, 1, 1),
					"GDG",
					"DOD",
					"GDG",

					'G', Items.gold_ingot,
					'D', Items.diamond,
					'O', new ItemStack(this, 1, 0)
				);

				ComputerCraft.registerTurtleUpgrade(new TurtleUpgradeToolManipulator());
			}
		}
	}
}
