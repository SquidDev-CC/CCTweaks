package org.squiddev.cctweaks.core.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import org.squiddev.cctweaks.core.blocks.BaseBlock;
import org.squiddev.cctweaks.core.blocks.IMultiBlock;

/**
 * An item to place instances of {@link IMultiBlock}
 */
public class MultiBlockItem extends ItemBlock {
	public MultiBlockItem(Block block, int stackSize) {
		super(block);
		if (!(block instanceof IMultiBlock)) throw new RuntimeException(block + " must be instance of IMultiBlock");

		setMaxStackSize(stackSize);
		setMaxDamage(0);
		setHasSubtypes(true);
	}

	public MultiBlockItem(BaseBlock block) {
		this(block, 64);
	}

	public int getMetadata(int meta) {
		return meta;
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int meta) {
		return field_150939_a.getIcon(0, meta);
	}

	public String getUnlocalizedName(ItemStack stack) {
		return ((IMultiBlock) field_150939_a).getUnlocalizedName(stack.getItemDamage());
	}
}
