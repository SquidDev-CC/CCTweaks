package org.squiddev.cctweaks.api.block;

import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

/**
 * A rotation handler which just sets a property (after checking it can be placed on a side).
 */
public class PropertyRotationHandler extends BasicRotationHandler {
	private final PropertyEnum<EnumFacing> property;

	public PropertyRotationHandler(PropertyEnum<EnumFacing> property) {
		super();
		this.property = property;
	}

	public PropertyRotationHandler(PropertyEnum<EnumFacing> property, boolean checkPlace) {
		super(checkPlace);
		this.property = property;
	}

	@Nonnull
	@Override
	protected IBlockState setDirection(@Nonnull IBlockState state, @Nonnull EnumFacing facing, @Nonnull EnumFacing rotatorFacing) {
		return state.withProperty(property, facing);
	}

	@Nonnull
	@Override
	public EnumActionResult rotate(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EnumFacing facing, @Nonnull EnumFacing rotatorFacing) {
		if (!property.getAllowedValues().contains(facing)) return EnumActionResult.FAIL;

		return super.rotate(world, pos, state, facing, rotatorFacing);
	}
}
