package org.squiddev.cctweaks.core.network.visitor;

import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.api.network.ISearchLoc;
import org.squiddev.cctweaks.core.utils.WorldPosition;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

/**
 * Iterable for {@link NetworkVisitorIterator}.
 *
 * This stores the starting world and coordinates so it can be reused.
 */
public class NetworkVisitorIterable implements Iterable<ISearchLoc> {
	protected final Iterable<IWorldPosition> starts;
	protected final Set<ISearchLoc> visited;

	public NetworkVisitorIterable(Iterable<IWorldPosition> starts, Set<ISearchLoc> visited) {
		this.starts = starts;
		this.visited = visited;
	}

	public NetworkVisitorIterable(Iterable<IWorldPosition> starts) {
		this(starts, null);
	}

	public NetworkVisitorIterable(IWorldPosition starts, Set<ISearchLoc> visited) {
		this(Collections.singletonList(starts), visited);
	}

	public NetworkVisitorIterable(IWorldPosition starts) {
		this(starts, null);
	}

	public NetworkVisitorIterable(IBlockAccess world, int x, int y, int z, Set<ISearchLoc> visited) {
		this(new WorldPosition(world, x, y, z), visited);
	}

	public NetworkVisitorIterable(IBlockAccess world, int x, int y, int z) {
		this(world, x, y, z, null);
	}

	@Override
	public Iterator<ISearchLoc> iterator() {
		NetworkVisitorIterator visitor = getIterator();
		for (IWorldPosition start : starts) {
			visitor.enqueue(new SearchLoc(start, 1), ForgeDirection.UNKNOWN);
		}
		return visitor;
	}

	protected NetworkVisitorIterator getIterator() {
		return visited == null ? new NetworkVisitorIterator() : new NetworkVisitorIterator(visited);
	}
}
