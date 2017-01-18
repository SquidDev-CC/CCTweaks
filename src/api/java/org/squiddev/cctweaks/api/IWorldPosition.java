package org.squiddev.cctweaks.api;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;

/**
 * Helper interface for blocks that provide a position
 */
public interface IWorldPosition {
	/**
	 * Get the world the block lies in
	 *
	 * @return The block's world
	 */
	IBlockAccess getBlockAccess();

	/**
	 * Get the position of the block
	 *
	 * @return The position of the block
	 */
	@Nonnull
	BlockPos getPosition();
}
