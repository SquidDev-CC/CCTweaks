package org.squiddev.cctweaks.core.utils;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.squiddev.cctweaks.api.IWorldPosition;

/**
 * Base implementation of {@link IWorldPosition}
 */
public class WorldPosition implements IWorldPosition {
	protected final IBlockAccess world;
	protected final BlockPos pos;

	public WorldPosition(TileEntity tile) {
		this(tile.getWorld(), tile.getPos());
	}

	public WorldPosition(IBlockAccess world, BlockPos pos) {
		this.world = world;
		this.pos = pos;
	}

	@Override
	public IBlockAccess getBlockAccess() {
		return world;
	}

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
