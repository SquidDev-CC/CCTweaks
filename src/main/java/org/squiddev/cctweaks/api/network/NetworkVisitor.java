package org.squiddev.cctweaks.api.network;

import dan200.computercraft.shared.peripheral.common.BlockCable;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

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
	 * @param world The world the network is in
	 * @param x     The starting X position of the traversal
	 * @param y     The starting Y position of the traversal
	 * @param z     The starting Z position of the traversal
	 */
	public void visitNetwork(IBlockAccess world, int x, int y, int z) {
		Queue<SearchLoc> queue = new LinkedList<SearchLoc>();
		Set<SearchLoc> visited = new HashSet<SearchLoc>();
		enqueue(queue, new SearchLoc(world, x, y, z, 1, ForgeDirection.UNKNOWN));

		while (queue.peek() != null) {
			visitBlock(queue, visited, queue.remove());
		}
	}

	/**
	 * Visit a network with this node visitor
	 *
	 * @param tile The node to start visiting with.
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
		if (location.distanceTravelled >= maxDistance || !visited.add(location)) {
			return;
		}

		TileEntity tile = location.world.getTileEntity(location.x, location.y, location.z);
		INetworkNode node;
		if (tile != null && (node = NetworkRegistry.getNode(tile)) != null) {
			visitNode(node, location.distanceTravelled + 1);

			SearchLoc downLocation = location.locationInDirection(ForgeDirection.DOWN);
			SearchLoc upLocation = location.locationInDirection(ForgeDirection.UP);
			SearchLoc northLocation = location.locationInDirection(ForgeDirection.NORTH);
			SearchLoc southLocation = location.locationInDirection(ForgeDirection.SOUTH);
			SearchLoc westLocation = location.locationInDirection(ForgeDirection.WEST);
			SearchLoc eastLocation = location.locationInDirection(ForgeDirection.EAST);

			if (node.canVisitTo(ForgeDirection.DOWN)) {
				enqueue(queue, downLocation);
			}
			if (node.canVisitTo(ForgeDirection.UP)) {
				enqueue(queue, upLocation);
			}
			if (node.canVisitTo(ForgeDirection.NORTH)) {
				enqueue(queue, northLocation);
			}
			if (node.canVisitTo(ForgeDirection.SOUTH)) {
				enqueue(queue, southLocation);
			}
			if (node.canVisitTo(ForgeDirection.WEST)) {
				enqueue(queue, westLocation);
			}
			if (node.canVisitTo(ForgeDirection.EAST)) {
				enqueue(queue, eastLocation);
			}

			SearchLoc[] searches = node.getExtraNodes();
			if (searches != null) {
				for (SearchLoc search : searches) {
					enqueue(queue, search);
				}
			}
		}
	}

	/**
	 * Add a node to the queue
	 *
	 * @param queue    The queue to add to
	 * @param location The location of the node to add
	 */
	protected void enqueue(Queue<SearchLoc> queue, SearchLoc location) {
		if (location.isValid()) {
			queue.offer(location);
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

		public boolean isValid() {
			return y >= 0 && y < world.getHeight()
				&& BlockCable.isCable(world, x, y, z)
				&& NetworkRegistry.getNode(world, x, y, z).canBeVisited(side.getOpposite());
		}

		public SearchLoc locationInDirection(ForgeDirection direction) {
			return new SearchLoc(world, x + direction.offsetX, y + direction.offsetY, z + direction.offsetZ, distanceTravelled + 1, direction.getOpposite());
		}
	}
}
