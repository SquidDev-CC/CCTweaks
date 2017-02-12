package org.squiddev.cctweaks.integration.jei;

import mezz.jei.api.IModRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import org.squiddev.cctweaks.blocks.BlockBase;
import org.squiddev.cctweaks.items.ItemBase;

import javax.annotation.Nonnull;

public final class JeiDescription {
	private JeiDescription() {
	}

	public static void registerDescription(IModRegistry registry, ItemStack stack) {
		registry.addDescription(stack, stack.getUnlocalizedName() + ".information");
	}

	public static void registerDescription(IModRegistry registry, ItemBase stack, int meta) {
		if (stack.canLoad()) registerDescription(registry, new ItemStack(stack, meta));
	}

	public static void registerDescription(IModRegistry registry, ItemBase item) {
		if (item.canLoad()) {
			NonNullList<ItemStack> stacks = NonNullList.create();
			item.getSubItems(item, null, stacks);

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
			block.getSubBlocks(Item.getItemFromBlock(block), null, stacks);

			if (stacks.size() == 0) {
				registerDescription(registry, new ItemStack(block));
			} else {
				for (ItemStack stack : stacks) {
					registerDescription(registry, stack);
				}
			}
		}
	}

	public static void registerGenericDescription(IModRegistry registry, ItemBase stack) {
		if (stack.canLoad()) registerGenericDescription(registry, new ItemStack(stack));
	}

	public static void registerGenericDescription(IModRegistry registry, BlockBase<?> block) {
		if (block.canLoad()) {
			NonNullList<ItemStack> stacks = NonNullList.create();
			block.getSubBlocks(Item.getItemFromBlock(block), null, stacks);

			if (stacks.size() == 0) {
				registerGenericDescription(registry, new ItemStack(block));
			} else {
				for (ItemStack stack : stacks) {
					registerGenericDescription(registry, stack);
				}
			}
		}
	}

	public static void registerGenericDescription(IModRegistry registry, @Nonnull ItemStack stack) {
		registry.addDescription(stack, stack.getItem().getUnlocalizedName() + ".information");
	}
}
