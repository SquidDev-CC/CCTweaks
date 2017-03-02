package org.squiddev.cctweaks.core.patch;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.items.ComputerItemFactory;
import dan200.computercraft.shared.computer.items.ItemComputerBase;
import dan200.computercraft.shared.turtle.items.ItemTurtleBase;
import dan200.computercraft.shared.turtle.items.TurtleItemFactory;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.squiddev.cctweaks.api.IComputerItemFactory;
import org.squiddev.cctweaks.api.computer.ICustomRomItem;
import org.squiddev.patcher.visitors.MergeVisitor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Implements {@link IComputerItemFactory} and {@link ICustomRomItem}.
 */
@MergeVisitor.Rename(
	from = "org/squiddev/cctweaks/core/patch/TileComputerBase_Patch",
	to = "dan200/computercraft/shared/computer/blocks/TileComputerBase"
)
public abstract class ItemComputerBase_Patch extends ItemComputerBase implements IComputerItemFactory, ICustomRomItem {
	@MergeVisitor.Stub
	protected ItemComputerBase_Patch() {
		super(null);
	}

	@Nonnull
	@Override
	public ItemStack createComputer(int id, @Nullable String label, @Nonnull ComputerFamily family) {
		if (ItemTurtleBase.class.isInstance(this)) {
			return TurtleItemFactory.create(id, label, null, family, null, null, 0, null);
		} else {
			return ComputerItemFactory.create(id, label, family);
		}
	}

	@Nonnull
	@Override
	public Set<ComputerFamily> getSupportedFamilies() {
		Block block = getBlock();
		if (block == ComputerCraft.Blocks.commandComputer) {
			return EnumSet.of(ComputerFamily.Command);
		} else if (block == ComputerCraft.Blocks.computer) {
			return EnumSet.of(ComputerFamily.Normal, ComputerFamily.Advanced);
		} else if (block == ComputerCraft.Blocks.turtleExpanded || block == ComputerCraft.Blocks.turtle) {
			return EnumSet.of(ComputerFamily.Normal);
		} else if (block == ComputerCraft.Blocks.turtleAdvanced) {
			return EnumSet.of(ComputerFamily.Advanced);
		} else {
			return Collections.emptySet();
		}
	}

	@Nonnull
	@Override
	public ComputerFamily getDefaultFamily() {
		Block block = getBlock();
		if (block == ComputerCraft.Blocks.commandComputer) {
			return ComputerFamily.Command;
		} else if (block == ComputerCraft.Blocks.computer) {
			return ComputerFamily.Normal;
		} else if (block == ComputerCraft.Blocks.turtleExpanded || block == ComputerCraft.Blocks.turtle) {
			return ComputerFamily.Normal;
		} else if (block == ComputerCraft.Blocks.turtleAdvanced) {
			return ComputerFamily.Advanced;
		} else {
			return ComputerFamily.Normal;
		}
	}

	@Override
	public boolean hasCustomRom(@Nonnull ItemStack stack) {
		return stack.hasTagCompound() && stack.getTagCompound().hasKey("rom_id", 99);
	}

	@Override
	public void clearCustomRom(@Nonnull ItemStack stack) {
		if (stack.hasTagCompound()) {
			stack.getTagCompound().removeTag("rom_id");
		}
	}

	@Override
	public void setCustomRom(@Nonnull ItemStack stack, int id) {
		NBTTagCompound tag = stack.getTagCompound();
		if (tag == null) stack.setTagCompound(tag = new NBTTagCompound());
		tag.setInteger("rom_id", id);
	}

	@Override
	public int getCustomRom(@Nonnull ItemStack stack) {
		return stack.getTagCompound().getInteger("rom_id");
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
		if (!super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState)) return false;

		NBTTagCompound tag = stack.getTagCompound();
		if (tag != null && tag.hasKey("rom_id", 99)) {
			TileEntity tile = world.getTileEntity(pos);
			if (tile != null && tile instanceof TileComputerBase_Patch) {
				TileComputerBase_Patch computer = (TileComputerBase_Patch) tile;
				computer.hasDisk = true;
				computer.diskId = tag.getInteger("rom_id");
			}
		}

		return true;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		if (advanced) {
			int id = getComputerID(stack);
			if (id >= 0) tooltip.add("(Computer ID: " + id + ")");
		}

		if (hasCustomRom(stack)) {
			int id = getCustomRom(stack);
			if (advanced && id >= 0) {
				tooltip.add("Has custom ROM (disk ID: " + id + ")");
			} else {
				tooltip.add("Has custom ROM");
			}
		}
	}
}
