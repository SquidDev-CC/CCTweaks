package org.squiddev.cctweaks.api.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

/**
 * A handler which attempts to rotate blocks
 */
public interface IRotationHandler {
	/**
	 * Attempt to rotate the block in a set direction
	 *
	 * @param world         The world the block is in
	 * @param pos           The position the block is in
	 * @param state         The current block state
	 * @param facing        The new direction to face
	 * @param rotatorFacing The direction the rotator is facing. Will be {@link EnumFacing#NORTH} if unknown.
	 * @return Whether rotating the block was successful
	 */
	@Nonnull
	EnumActionResult rotate(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EnumFacing facing, @Nonnull EnumFacing rotatorFacing);
}
