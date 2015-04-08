package org.squiddev.cctweaks.core.items;

import dan200.computercraft.shared.computer.blocks.TileComputerBase;
import dan200.computercraft.shared.turtle.blocks.TileTurtle;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * Handles actions on computers
 */
public abstract class ItemComputerAction extends ItemBase {
	public ItemComputerAction(String itemName) {
		super(itemName);
	}

	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		if (!player.isSneaking() || world.isRemote) {
			return false;
		}

		TileEntity tile = world.getTileEntity(x, y, z);

		// Ensure tile is Computer Tile
		if (tile == null || !(tile instanceof TileComputerBase)) {
			return false;
		}

		// Allow custom Turtle Actions
		boolean result = false;
		if (tile instanceof TileTurtle) {
			result = upgradeTurtle(stack, player, world, x, y, z, (TileTurtle) tile);
		} else {
			result = upgradeComputer(stack, player, world, x, y, z, (TileComputerBase) tile);
		}

		if (result) {
			if (!player.capabilities.isCreativeMode) {
				stack.stackSize -= 1;
			}
		}

		return result;
	}

	protected abstract boolean upgradeComputer(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, TileComputerBase computerTile);

	protected boolean upgradeTurtle(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, TileTurtle computerTile) {
		return upgradeComputer(stack, player, world, x, y, z, computerTile);
	}
}
