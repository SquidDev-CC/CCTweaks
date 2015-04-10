package org.squiddev.cctweaks.api.network;

import dan200.computercraft.shared.peripheral.common.BlockCable;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

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
	public void visitNetwork(World world, int x, int y, int z) {
		Queue<SearchLoc> queue = new LinkedList<SearchLoc>();
		Set<TileEntity> visited = new HashSet<TileEntity>();
		enqueue(queue, world, x, y, z, 1);

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
	protected void visitBlock(Queue<SearchLoc> queue, Set<TileEntity> visited, SearchLoc location) {
		if (location.distanceTravelled >= maxDistance) {
			return;
		}

		TileEntity tile = location.world.getTileEntity(location.x, location.y, location.z);
		INetworkNode node;
		if (tile != null && (node = NetworkRegistry.getNode(tile)) != null) {
			if (node.canVisit() && visited.add(tile)) {
				visitNode(node, location.distanceTravelled + 1);

				enqueue(queue, location.world, location.x, location.y + 1, location.z, location.distanceTravelled + 1);
				enqueue(queue, location.world, location.x, location.y - 1, location.z, location.distanceTravelled + 1);
				enqueue(queue, location.world, location.x, location.y, location.z + 1, location.distanceTravelled + 1);
				enqueue(queue, location.world, location.x, location.y, location.z - 1, location.distanceTravelled + 1);
				enqueue(queue, location.world, location.x + 1, location.y, location.z, location.distanceTravelled + 1);
				enqueue(queue, location.world, location.x - 1, location.y, location.z, location.distanceTravelled + 1);
			}
		}
	}

	/**
	 * Add a node to the queue
	 *
	 * @param queue             The queue to add to
	 * @param world             The world the node is in
	 * @param x                 X position of the node
	 * @param y                 Y position of the node
	 * @param z                 Z position of the node
	 * @param distanceTravelled Distance the node has traversal has travelled
	 */
	protected void enqueue(Queue<SearchLoc> queue, World world, int x, int y, int z, int distanceTravelled) {
		if (y >= 0 && y < world.getHeight() && BlockCable.isCable(world, x, y, z)) {
			queue.offer(new SearchLoc(world, x, y, z, distanceTravelled));
		}
	}

	/**
	 * The location we should search for nodes in
	 */
	protected static class SearchLoc {
		public final World world;
		public final int x;
		public final int y;
		public final int z;
		public final int distanceTravelled;

		public SearchLoc(World world, int x, int y, int z, int distanceTravelled) {
			this.world = world;
			this.x = x;
			this.y = y;
			this.z = z;
			this.distanceTravelled = distanceTravelled;
		}
	}
}
