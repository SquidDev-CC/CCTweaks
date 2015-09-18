package org.squiddev.cctweaks.items;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.squiddev.cctweaks.core.utils.Helpers;

/**
 * Item to bind networks together
 */
public class ItemTurtleWand extends ItemBase {
	public ItemTurtleWand() {
		super("turtleWand", 1);
	}

	@Override
	public void init() {
		super.init();
		Helpers.alternateCrafting(
			new ItemStack(this), 'p', 's',
			"PEP",
			"EDE",
			"PEP",

			'P', Items.ender_pearl,
			'E', Items.ender_eye,
			'p', Items.diamond_pickaxe,
			's', Items.diamond_sword
		);
	}
}
