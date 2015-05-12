package org.squiddev.cctweaks.core.network.visitor;

import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.api.network.ISearchLoc;

import java.util.Set;

/**
 * A network visitor that doesn't load chunks if they don't exist
 */
public class SafeNetworkVisitorIterable extends NetworkVisitorIterable {
	public SafeNetworkVisitorIterable(Iterable<IWorldPosition> starts) {
		super(starts);
	}

	public SafeNetworkVisitorIterable(Iterable<IWorldPosition> starts, Set<ISearchLoc> visited) {
		super(starts, visited);
	}

	public SafeNetworkVisitorIterable(IWorldPosition starts) {
		super(starts);
	}

	public SafeNetworkVisitorIterable(IWorldPosition starts, Set<ISearchLoc> visited) {
		super(starts, visited);
	}

	public SafeNetworkVisitorIterable(IBlockAccess world, int x, int y, int z) {
		super(world, x, y, z);
	}

	public SafeNetworkVisitorIterable(IBlockAccess world, int x, int y, int z, Set<ISearchLoc> visited) {
		super(world, x, y, z, visited);
	}

	@Override
	protected NetworkVisitorIterator getIterator() {
		return visited == null ? new SafeNetworkVisitorIterator() : new SafeNetworkVisitorIterator(visited);
	}

	public static class SafeNetworkVisitorIterator extends NetworkVisitorIterator {
		public SafeNetworkVisitorIterator() {
		}

		public SafeNetworkVisitorIterator(Set<ISearchLoc> visited) {
			super(visited);
		}

		@Override
		public void enqueue(ISearchLoc location, ForgeDirection direction) {
			IBlockAccess world = location.getWorld();
			if (!(world instanceof World) || ((World) world).blockExists(location.getX(), location.getY(), location.getZ())) {
				super.enqueue(location, direction);
			}
		}
	}
}
