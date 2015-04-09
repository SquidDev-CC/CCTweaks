package org.squiddev.cctweaks.core.patch;

import dan200.computercraft.ComputerCraft;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.IBlockAccess;

/**
 * Patches {@link dan200.computercraft.shared.peripheral.common.BlockCable#isCable(IBlockAccess, int, int, int)}
 */
@SuppressWarnings("unused")
public final class BlockCable_Patch {
	public static boolean isCable(IBlockAccess world, int x, int y, int z) {
		Block block = world.getBlock(x, y, z);
		if (block == ComputerCraft.Blocks.cable) {
			switch (ComputerCraft.Blocks.cable.getPeripheralType(world, x, y, z)) {
				case Cable:
				case WiredModemWithCable:
					return true;
			}
		} else if (block == Blocks.redstone_block) {
			return true;
		}

		return false;
	}
}
