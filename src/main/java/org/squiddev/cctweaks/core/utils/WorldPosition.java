package org.squiddev.cctweaks.core.utils;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.squiddev.cctweaks.api.IWorldPosition;

import javax.annotation.Nonnull;

/**
 * Base implementation of {@link IWorldPosition}
 */
public class WorldPosition implements IWorldPosition {
	private final IBlockAccess world;
	private final BlockPos pos;

	public WorldPosition(TileEntity tile) {
		this(tile.getWorld(), tile.getPos());
	}

	public WorldPosition(IBlockAccess world, BlockPos pos) {
		this.world = world;
		this.pos = pos;
	}

	@Nonnull
	@Override
	public IBlockAccess getBlockAccess() {
		return world;
	}

	@Nonnull
	@Override
	public BlockPos getPosition() {
		return pos;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof IWorldPosition)) return false;

		IWorldPosition that = (IWorldPosition) o;

		return pos.equals(that.getPosition()) && world.equals(that.getBlockAccess());
	}

	@Override
	public int hashCode() {
		return world.hashCode() * 31 + pos.hashCode();
	}
}
