package org.squiddev.cctweaks.items;

import dan200.computercraft.shared.computer.blocks.TileComputerBase;
import dan200.computercraft.shared.turtle.blocks.TileTurtle;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

/**
 * Handles actions on computers
 */
public abstract class ItemComputerAction extends ItemBase {
	public ItemComputerAction(String itemName) {
		super(itemName);
	}

	@Nonnull
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos position, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (!player.isSneaking()) {
			return EnumActionResult.PASS;
		}

		ItemStack stack = player.getHeldItem(hand);
		TileEntity tile = world.getTileEntity(position);
		if (tile == null) return EnumActionResult.PASS;

		if (world.isRemote) return EnumActionResult.SUCCESS;

		boolean result;
		if (tile instanceof TileComputerBase) {
			// Allow custom Turtle Actions
			if (tile instanceof TileTurtle) {
				result = useTurtle(stack, player, (TileTurtle) tile, side);
			} else {
				result = useComputer(stack, player, (TileComputerBase) tile, side);
			}
		} else {
			result = useGeneric(stack, player, tile, side);
		}

		if (result) {
			if (!player.capabilities.isCreativeMode) {
				stack.grow(-1);
			}
		}

		return result ? EnumActionResult.SUCCESS : EnumActionResult.FAIL;
	}

	protected abstract boolean useComputer(ItemStack stack, EntityPlayer player, TileComputerBase computerTile, EnumFacing side);

	protected boolean useTurtle(ItemStack stack, EntityPlayer player, TileTurtle computerTile, EnumFacing side) {
		return useComputer(stack, player, computerTile, side);
	}

	/**
	 * Custom action on other tile types
	 */
	protected boolean useGeneric(ItemStack stack, EntityPlayer player, TileEntity tile, EnumFacing side) {
		return false;
	}
}
