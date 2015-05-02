package org.squiddev.cctweaks.api;

import net.minecraft.world.IBlockAccess;

/**
 * Helper interface for blocks that provide a position
 */
public interface IWorldPosition {
	/**
	 * Get the world the block lies in
	 *
	 * @return The block's world
	 */
	IBlockAccess getWorld();

	/**
	 * Get X position of the block
	 *
	 * @return X position of the block
	 */
	int getX();

	/**
	 * Get Y position of the block
	 *
	 * @return Y position of the block
	 */

	int getY();

	/**
	 * Get Z position of the block
	 *
	 * @return Z position of the block
	 */
	int getZ();

}
