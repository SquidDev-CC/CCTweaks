package org.squiddev.cctweaks.integration.jei;

import mezz.jei.api.IModRegistry;
import net.minecraft.item.ItemStack;
import org.squiddev.cctweaks.blocks.BlockBase;
import org.squiddev.cctweaks.items.ItemBase;

import java.util.ArrayList;
import java.util.List;

public final class JeiDescription {
	private JeiDescription() {
	}

	public static void registerDescription(IModRegistry registry, ItemStack stack) {
		registry.addDescription(stack, stack.getUnlocalizedName() + ".information");
	}

	public static void registerDescription(IModRegistry registry, ItemBase stack, int meta) {
		if (stack.canLoad()) registerDescription(registry, new ItemStack(stack, meta));
	}

	public static void registerDescription(IModRegistry registry, BlockBase<?> block) {
		if (block.canLoad()) {
			List<ItemStack> stacks = new ArrayList<ItemStack>();
			block.getSubBlocks(null, null, stacks);

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
			List<ItemStack> stacks = new ArrayList<ItemStack>();
			block.getSubBlocks(null, null, stacks);

			if (stacks.size() == 0) {
				registerGenericDescription(registry, new ItemStack(block));
			} else {
				for (ItemStack stack : stacks) {
					registerGenericDescription(registry, stack);
				}
			}
		}
	}

	public static void registerGenericDescription(IModRegistry registry, ItemStack stack) {
		registry.addDescription(stack, stack.getItem().getUnlocalizedName() + ".information");
	}
}
