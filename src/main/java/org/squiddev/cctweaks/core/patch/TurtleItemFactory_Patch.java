package org.squiddev.cctweaks.core.patch;

import dan200.computercraft.shared.turtle.blocks.ITurtleTile;
import dan200.computercraft.shared.turtle.items.TurtleItemFactory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.squiddev.patcher.visitors.MergeVisitor;

import javax.annotation.Nonnull;

/**
 * Copy across rom_id from the computer tile to the item
 */
@MergeVisitor.Rename(
	from = "org/squiddev/cctweaks/core/patch/TileComputerBase_Patch",
	to = "dan200/computercraft/shared/computer/blocks/TileComputerBase"
)
public class TurtleItemFactory_Patch extends TurtleItemFactory {
	@Nonnull
	public static ItemStack create(ITurtleTile turtle) {
		ItemStack stack = native_create(turtle);
		if (!stack.isEmpty() && turtle instanceof TileComputerBase_Patch) {
			TileComputerBase_Patch compTile = (TileComputerBase_Patch) turtle;
			if (compTile.hasDisk()) {
				NBTTagCompound tag = stack.getTagCompound();
				if (tag == null) stack.setTagCompound(tag = new NBTTagCompound());

				tag.setInteger("rom_id", compTile.getDiskId());
			}
		}

		return stack;
	}

	@Nonnull
	@MergeVisitor.Rename(from = "create")
	@MergeVisitor.Stub
	public static ItemStack native_create(ITurtleTile turtle) {
		return TurtleItemFactory.create(turtle);
	}
}
