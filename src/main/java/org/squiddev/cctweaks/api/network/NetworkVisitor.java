package org.squiddev.cctweaks.api.network;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import org.squiddev.cctweaks.api.IWorldPosition;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * A utility class for node traversal
 */
public abstract class NetworkVisitor {
	protected int maxDistance = 256;

	/**
	 * Visit a network with this node visitor
	 *
	 * @param world   The world the network is in
	 * @param x       The starting X position of the traversal
	 * @param y       The starting Y position of the traversal
	 * @param z       The starting Z position of the traversal
	 * @param visited Already visited locations on the network
	 */
	public void visitNetwork(IBlockAccess world, int x, int y, int z, Set<SearchLoc> visited) {
		Queue<SearchLoc> queue = new LinkedList<SearchLoc>();
		enqueue(queue, visited, new SearchLoc(world, x, y, z, 1, ForgeDirection.UNKNOWN));

		while (queue.peek() != null) {
			visitBlock(queue, visited, queue.remove());
		}
	}

	/**
	 * Visit a network with this node visitor
	 *
	 * @param world The world the network is in
	 * @param x     The starting X position of the traversal
	 * @param y     The starting Y position of the traversal
	 * @param z     The starting Z position of the traversal
	 */
	public void visitNetwork(IBlockAccess world, int x, int y, int z) {
		visitNetwork(world, x, y, z, new HashSet<SearchLoc>());
	}

	/**
	 * Visit a network with this node visitor
	 *
	 * @param tile    The node to start visiting with
	 * @param visited Already visited locations on the network
	 */
	public void visitNetwork(TileEntity tile, Set<SearchLoc> visited) {
		if (tile == null) return;
		visitNetwork(tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord, visited);
	}

	/**
	 * Visit a network with this node visitor
	 *
	 * @param tile The node to start visiting with
	 */
	public void visitNetwork(TileEntity tile) {
		if (tile == null) return;
		visitNetwork(tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord);
	}

	/**
	 * Visit a node on the network. Override in child classes for functionality
	 *
	 * @param node     The node to visit
	 * @param distance Distance traveled since starting traversal
	 */
	protected abstract void visitNode(INetworkNode node, int distance);

	/**
	 * Visit a block and queue adjacent nodes
	 *
	 * @param queue    The queue to append to
	 * @param visited  The list of visited nodes
	 * @param location Location of the current node
	 */
	protected void visitBlock(Queue<SearchLoc> queue, Set<SearchLoc> visited, SearchLoc location) {
		INetworkNode node = location.getNode();
		visitNode(node, location.distanceTravelled + 1);

		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			enqueue(queue, visited, location, direction);
		}

		Iterable<IWorldPosition> searches = node.getExtraNodes();
		if (searches != null) {
			for (IWorldPosition search : searches) {
				enqueue(queue, visited, new SearchLoc(search, location.distanceTravelled + 1, ForgeDirection.UNKNOWN));
			}
		}
	}

	/**
	 * Add a node to the queue
	 *
	 * @param queue    The queue to add to
	 * @param visited  List of visited locations
	 * @param location The location of the node to add
	 */
	protected void enqueue(Queue<SearchLoc> queue, Set<SearchLoc> visited, SearchLoc location) {
		if (location.distanceTravelled < maxDistance && visited.add(location) && location.getNode() != null) {
			queue.offer(location);
		}
	}

	/**
	 * Add a node to the queue if it doesn't exist already
	 *
	 * @param queue   The queue
	 * @param visited List of visited locations
	 * @param current The current node
	 * @param to      Direction to visit in
	 */
	protected void enqueue(Queue<SearchLoc> queue, Set<SearchLoc> visited, SearchLoc current, ForgeDirection to) {
		if (current.getNode().canVisitTo(to)) {
			enqueue(queue, visited, current.locationInDirection(to));
		}
	}

	/**
	 * The location we should search for nodes in
	 */
	public static final class SearchLoc {
		public final IBlockAccess world;
		public final int x;
		public final int y;
		public final int z;
		public final int distanceTravelled;
		public final ForgeDirection side;

		protected final int hash;

		protected INetworkNode node = null;

		public SearchLoc(IBlockAccess world, int x, int y, int z, int distanceTravelled, ForgeDirection side) {
			this.world = world;
			this.x = x;
			this.y = y;
			this.z = z;
			this.distanceTravelled = distanceTravelled;
			this.side = side;

			// Cache the hash code as we store this in a map
			int hash = world.hashCode();
			hash = 31 * hash + x;
			hash = 31 * hash + y;
			hash = 31 * hash + z;
			this.hash = hash;
		}

		public SearchLoc(IWorldPosition position, int distanceTravelled, ForgeDirection side) {
			this(position.getWorld(), position.getX(), position.getY(), position.getZ(), distanceTravelled, side);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof SearchLoc)) return false;

			SearchLoc searchLoc = (SearchLoc) o;

			if (x != searchLoc.x) return false;
			if (y != searchLoc.y) return false;
			if (z != searchLoc.z) return false;
			if (side != searchLoc.side) return false;
			return world.equals(searchLoc.world);
		}

		@Override
		public int hashCode() {
			return hash;
		}

		public INetworkNode getNode() {
			INetworkNode node = this.node;
			if (node != null) return node;

			if (y >= 0 && y < world.getHeight()) {
				node = NetworkRegistry.getNode(world, x, y, z);
				if (node != null && node.canBeVisited(side)) {
					this.node = node;
				} else {
					node = null;
				}
			}

			return node;
		}

		public SearchLoc locationInDirection(ForgeDirection direction) {
			return new SearchLoc(world, x + direction.offsetX, y + direction.offsetY, z + direction.offsetZ, distanceTravelled + 1, direction.getOpposite());
		}
	}
}
