package org.squiddev.cctweaks.core.patch;

import dan200.computercraft.shared.computer.blocks.IComputerTile;
import dan200.computercraft.shared.computer.items.ComputerItemFactory;
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
public class ComputerItemFactory_Patch extends ComputerItemFactory {
	@Nonnull
	public static ItemStack create(IComputerTile tile) {
		ItemStack stack = native_create(tile);
		if (!stack.isEmpty() && tile instanceof TileComputerBase_Patch) {
			TileComputerBase_Patch compTile = (TileComputerBase_Patch) tile;
			if (compTile.hasDisk) {
				NBTTagCompound tag = stack.getTagCompound();
				if (tag == null) stack.setTagCompound(tag = new NBTTagCompound());

				tag.setInteger("rom_id", compTile.diskId);
			}
		}

		return stack;
	}

	@Nonnull
	@MergeVisitor.Rename(from = "create")
	@MergeVisitor.Stub
	public static ItemStack native_create(IComputerTile tile) {
		return ComputerItemFactory.create(tile);
	}
}
