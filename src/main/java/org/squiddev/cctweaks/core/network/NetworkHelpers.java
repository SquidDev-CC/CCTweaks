package org.squiddev.cctweaks.core.network;

import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.api.network.INetworkController;
import org.squiddev.cctweaks.api.network.INetworkNode;
import org.squiddev.cctweaks.api.network.IWorldNetworkNode;
import org.squiddev.cctweaks.api.network.NetworkAPI;
import org.squiddev.cctweaks.core.FmlEvents;
import org.squiddev.cctweaks.core.utils.DebugLogger;

/**
 * Helper methods on networks
 */
public final class NetworkHelpers {
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
	public static boolean canConnect(IBlockAccess world, int x, int y, int z, ForgeDirection direction) {
		x += direction.offsetX;
		y += direction.offsetY;
		z += direction.offsetZ;

		IWorldNetworkNode node = NetworkAPI.registry().getNode(world, x, y, z);
		return node != null && node.canConnect(direction.getOpposite());
	}

	public static boolean canConnect(IWorldPosition pos, ForgeDirection direction) {
		return canConnect(pos.getWorld(), pos.getX(), pos.getY(), pos.getZ(), direction);
	}

	/**
	 * Scans surrounding nodes and attaches to them if possible
	 *
	 * @param node The node to scan with
	 */
	public static void joinOrCreateNetwork(IWorldNetworkNode node) {
		if (node.getAttachedNetwork() != null) return;

		IWorldPosition position = node.getPosition();
		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			if (node.canConnect(direction)) {
				IWorldNetworkNode neighbour = NetworkAPI.registry().getNode(
					position.getWorld(),
					position.getX() + direction.offsetX,
					position.getY() + direction.offsetY,
					position.getZ() + direction.offsetZ
				);

				if (neighbour != null && neighbour.canConnect(direction.getOpposite()) && neighbour.getAttachedNetwork() != null) {
					INetworkController network = neighbour.getAttachedNetwork();
					network.formConnection(neighbour, node);
					DebugLogger.debug(node + "Connecting to " + node.getAttachedNetwork() + " from " + neighbour);
				} else if (neighbour != null) {
					DebugLogger.debug(node + " Node has no network " + neighbour);
				}
			}
		}

		if (node.getAttachedNetwork() == null) {
			joinNewNetwork(node);
			DebugLogger.debug(node + " Creating new network");
		}
	}

	/**
	 * Creates a new network for the node
	 *
	 * @param node The node to create the network with
	 */
	public static void joinNewNetwork(INetworkNode node) {
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
	public static void scheduleJoin(final IWorldNetworkNode node) {
		FmlEvents.schedule(new Runnable() {
			@Override
			public void run() {
				joinOrCreateNetwork(node);
			}
		});
	}
}
