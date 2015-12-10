package org.squiddev.cctweaks.items;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import org.squiddev.cctweaks.blocks.IMultiBlock;

/**
 * An item to place instances of {@link IMultiBlock}
 */
public class ItemMultiBlock extends ItemBlock {
	public ItemMultiBlock(Block block) {
		super(block);
		if (!(block instanceof IMultiBlock)) throw new RuntimeException(block + " must be instance of IMultiBlock");

		setMaxStackSize(64);
		setMaxDamage(0);
		setHasSubtypes(true);
	}

	@Override
	public int getMetadata(int meta) {
		return meta;
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return ((IMultiBlock) block).getUnlocalizedName(stack.getItemDamage());
	}
}
