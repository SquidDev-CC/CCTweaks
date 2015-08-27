package org.squiddev.cctweaks.core.utils;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import org.squiddev.cctweaks.api.IWorldPosition;

/**
 * Base implementation of {@link IWorldPosition}
 */
public class WorldPosition implements IWorldPosition {
	protected final IBlockAccess world;
	protected final int x;
	protected final int y;
	protected final int z;

	public WorldPosition(TileEntity tile) {
		this(tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord);
	}

	public WorldPosition(IBlockAccess world, int x, int y, int z) {
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public IBlockAccess getWorld() {
		return world;
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	@Override
	public int getZ() {
		return z;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof IWorldPosition)) return false;

		IWorldPosition that = (IWorldPosition) o;

		if (x != that.getX()) return false;
		if (y != that.getY()) return false;
		if (z != that.getZ()) return false;
		return world.equals(that.getWorld());
	}

	@Override
	public int hashCode() {
		int result = world.hashCode();
		result = 31 * result + x;
		result = 31 * result + y;
		result = 31 * result + z;
		return result;
	}
}
