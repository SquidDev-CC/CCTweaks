package org.squiddev.cctweaks.api.block;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

/**
 * A rotation handler which just changes the block state (after checking it can be placed on a side).
 */
public abstract class BasicRotationHandler implements IRotationHandler {
	private final boolean checkPlace;

	public BasicRotationHandler(boolean checkPlace) {
		this.checkPlace = checkPlace;
	}

	public BasicRotationHandler() {
		this.checkPlace = false;
	}

	@Nonnull
	protected abstract IBlockState setDirection(@Nonnull IBlockState state, @Nonnull EnumFacing facing, @Nonnull EnumFacing rotatorFacing);

	@Nonnull
	@Override
	public EnumActionResult rotate(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EnumFacing facing, @Nonnull EnumFacing rotatorFacing) {
		Block block = state.getBlock();
		if (checkPlace && !block.canPlaceBlockOnSide(world, pos, facing)) {
			return EnumActionResult.FAIL;
		}

		world.setBlockState(pos, setDirection(state, facing, rotatorFacing));
		return EnumActionResult.SUCCESS;
	}
}
