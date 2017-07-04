package org.squiddev.cctweaks.integration.jei;

import mezz.jei.api.IModRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import org.squiddev.cctweaks.blocks.BlockBase;
import org.squiddev.cctweaks.items.ItemBase;

import javax.annotation.Nonnull;

public final class JeiDescription {
	private JeiDescription() {
	}

	public static void registerDescription(IModRegistry registry, ItemBase item) {
		if (item.canLoad()) {
			NonNullList<ItemStack> stacks = NonNullList.create();
			item.getSubItems(CreativeTabs.SEARCH, stacks);

			if (stacks.size() == 0) {
				registerDescription(registry, new ItemStack(item));
			} else {
				for (ItemStack stack : stacks) {
					registerDescription(registry, stack);
				}
			}
		}
	}

	public static void registerDescription(IModRegistry registry, BlockBase<?> block) {
		if (block.canLoad()) {
			NonNullList<ItemStack> stacks = NonNullList.create();
			block.getSubBlocks(CreativeTabs.SEARCH, stacks);

			if (stacks.size() == 0) {
				registerDescription(registry, new ItemStack(block));
			} else {
				for (ItemStack stack : stacks) {
					registerDescription(registry, stack);
				}
			}
		}
	}

	public static void registerDescription(IModRegistry registry, @Nonnull ItemStack stack) {
		registry.addIngredientInfo(stack, ItemStack.class, stack.getUnlocalizedName() + ".information");
	}
}
