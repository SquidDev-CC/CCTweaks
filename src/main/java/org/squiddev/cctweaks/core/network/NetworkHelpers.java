package org.squiddev.cctweaks.core.network;

import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.api.network.*;
import org.squiddev.cctweaks.core.FmlEvents;

import java.util.HashSet;
import java.util.Set;

/**
 * Helper methods on networks
 */
public final class NetworkHelpers implements INetworkHelpers {
	/**
	 * Check if a block is a cable and can be connected to
	 *
	 * @param world     World the node lies in
	 * @param x         X position of the node we are checking from
	 * @param y         Y position of the node we are checking from
	 * @param z         Z position of the node we are checking from
	 * @param direction Direction we are checking in
	 * @return If the target block is a node and can be connected to
	 */
	@Override
	public boolean canConnect(IBlockAccess world, int x, int y, int z, ForgeDirection direction) {
		x += direction.offsetX;
		y += direction.offsetY;
		z += direction.offsetZ;

		IWorldNetworkNode node = NetworkAPI.registry().getNode(world, x, y, z);
		return node != null && node.canConnect(direction.getOpposite());
	}

	@Override
	public boolean canConnect(IWorldPosition pos, ForgeDirection direction) {
		return canConnect(pos.getWorld(), pos.getX(), pos.getY(), pos.getZ(), direction);
	}

	/**
	 * Get adjacent nodes that can be connected to
	 *
	 * Checks the current node can connect, and adjacent node can be connected to
	 * in that direction
	 *
	 * @param node The current node
	 * @return The adjacent nodes
	 */
	@Override
	public Set<INetworkNode> getAdjacentNodes(IWorldNetworkNode node) {
		Set<INetworkNode> nodes = new HashSet<INetworkNode>();
		IWorldPosition position = node.getPosition();

		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			if (node.canConnect(direction)) {
				IWorldNetworkNode neighbour = NetworkAPI.registry().getNode(
					position.getWorld(),
					position.getX() + direction.offsetX,
					position.getY() + direction.offsetY,
					position.getZ() + direction.offsetZ
				);

				if (neighbour != null && neighbour.canConnect(direction.getOpposite())) {
					nodes.add(neighbour);
				}
			}
		}

		return nodes;
	}

	/**
	 * Connect to adjacent nodes, or create a network.
	 *
	 * Uses {@link #getAdjacentNodes(IWorldNetworkNode)} and {@link #joinOrCreateNetwork(INetworkNode, Set)}
	 *
	 * @param node The node to scan with
	 */
	@Override
	public void joinOrCreateNetwork(IWorldNetworkNode node) {
		joinOrCreateNetwork(node, getAdjacentNodes(node));
	}

	/**
	 * Attempt to connect to any node, or create a network if it cannot
	 *
	 * @param node        The node to scan with
	 * @param connections The nodes that can connect
	 */
	@Override
	public void joinOrCreateNetwork(INetworkNode node, Set<? extends INetworkNode> connections) {
		for (INetworkNode neighbour : connections) {
			if (neighbour.getAttachedNetwork() != null) {
				INetworkController network = neighbour.getAttachedNetwork();
				network.formConnection(neighbour, node);
			}
		}

		if (node.getAttachedNetwork() == null) {
			joinNewNetwork(node);
			for (INetworkNode neighbour : connections) {
				node.getAttachedNetwork().formConnection(node, neighbour);
			}
		}
	}

	/**
	 * Creates a new network for the node.
	 * It will be removed from the current network.
	 *
	 * @param node The node to create the network with
	 */
	@Override
	public void joinNewNetwork(INetworkNode node) {
		if (node.getAttachedNetwork() != null) {
			node.getAttachedNetwork().removeNode(node);
		}
		new NetworkController(node);
	}

	/**
	 * Schedule calling {@link #joinOrCreateNetwork(IWorldNetworkNode)} next tick
	 *
	 * @param node The node to schedule
	 */
	@Override
	public void scheduleJoin(final IWorldNetworkNode node) {
		FmlEvents.schedule(new Runnable() {
			@Override
			public void run() {
				joinOrCreateNetwork(node);
			}
		});
	}

	/**
	 * Schedule calling {@link AbstractWorldNode#connect()} next tick
	 *
	 * @param node The node to schedule
	 */
	public static void scheduleConnect(final AbstractWorldNode node) {
		FmlEvents.schedule(new Runnable() {
			@Override
			public void run() {
				node.connect();
			}
		});
	}
}
