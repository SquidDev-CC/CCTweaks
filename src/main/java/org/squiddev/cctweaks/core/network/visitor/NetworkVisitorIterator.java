package org.squiddev.cctweaks.core.network.visitor;

import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.api.network.INetworkNode;
import org.squiddev.cctweaks.api.network.ISearchLoc;
import org.squiddev.cctweaks.core.network.NetworkVisitor;

import java.util.*;

/**
 * Iterator for {@link NetworkVisitor}
 */
public class NetworkVisitorIterator implements Iterator<ISearchLoc> {
	protected final int maxDistance = 256;
	protected final Queue<ISearchLoc> queue = new LinkedList<ISearchLoc>();
	protected final Set<ISearchLoc> visited;

	public NetworkVisitorIterator(Set<ISearchLoc> visited) {
		this.visited = visited;
	}

	public NetworkVisitorIterator() {
		this(new HashSet<ISearchLoc>());
	}

	@Override
	public boolean hasNext() {
		return !queue.isEmpty();
	}

	@Override
	public ISearchLoc next() {
		ISearchLoc location = queue.remove();
		INetworkNode node = location.getNode();

		// Visit surrounding nodes
		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			if (node.canVisitTo(direction)) {
				enqueue(SearchLoc.locationInDirection(location, direction));
			}
		}

		// Visit extra nodes
		Iterable<IWorldPosition> searches = node.getExtraNodes();
		if (searches != null) {
			for (IWorldPosition search : searches) {
				enqueue(new SearchLoc(search, location.getDistance() + 1, ForgeDirection.UNKNOWN));
			}
		}

		return location;
	}

	/**
	 * Add a node to the queue
	 *
	 * This checks if it the distance isn't more than {@link #maxDistance} blocks,
	 * if it hasn't been visited before and if it is a node
	 *
	 * @param location The location of the node to add
	 */
	public void enqueue(ISearchLoc location) {
		if (location.getDistance() < maxDistance && visited.add(location) && location.getNode() != null) {
			queue.offer(location);
		}
	}
}