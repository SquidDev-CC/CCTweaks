package org.squiddev.cctweaks.core.network.visitor;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.api.network.INetworkVisitor;
import org.squiddev.cctweaks.api.network.ISearchLoc;
import org.squiddev.cctweaks.core.utils.WorldPosition;

import java.util.Set;

/**
 * A utility class for node traversal
 */
public final class NetworkVisitor implements INetworkVisitor {
	@Override
	public Iterable<ISearchLoc> visitNetwork(IBlockAccess world, int x, int y, int z) {
		return visitNetwork(new WorldPosition(world, x, y, z));
	}

	@Override
	public Iterable<ISearchLoc> visitNetwork(IBlockAccess world, int x, int y, int z, Set<ISearchLoc> visited) {
		return visitNetwork(new WorldPosition(world, x, y, z), visited);
	}

	@Override
	public Iterable<ISearchLoc> visitNetwork(TileEntity tile) {
		return visitNetwork(tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord);
	}

	@Override
	public Iterable<ISearchLoc> visitNetwork(TileEntity tile, Set<ISearchLoc> visited) {
		return visitNetwork(tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord, visited);
	}

	@Override
	public Iterable<ISearchLoc> visitNetwork(IWorldPosition position) {
		return new NetworkVisitorIterable(position);
	}

	@Override
	public Iterable<ISearchLoc> visitNetwork(IWorldPosition position, Set<ISearchLoc> visited) {
		return visitNetwork(position.getWorld(), position.getX(), position.getY(), position.getZ(), visited);
	}

	@Override
	public Iterable<ISearchLoc> visitNetwork(Iterable<IWorldPosition> positions) {
		return new NetworkVisitorIterable(positions);
	}
}
