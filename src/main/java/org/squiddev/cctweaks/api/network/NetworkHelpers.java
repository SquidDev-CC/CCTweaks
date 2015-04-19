package org.squiddev.cctweaks.api.network;

import net.minecraft.util.Facing;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.HashSet;
import java.util.Set;

/**
 * Helper methods on networks
 */
public class NetworkHelpers {
	private static final NetworkVisitor invalidateVisitor = new NetworkVisitor() {
		public void visitNode(INetworkNode node, int distance) {
			synchronized (node.lock()) {
				node.networkInvalidated();
			}
		}
	};

	/**
	 * Fire {@link INetworkNode#networkInvalidated()} on adjacent nodes
	 *
	 * @param world The world the block lies in
	 * @param x     The X position of the node
	 * @param y     The Y position of the node
	 * @param z     The Z position of the node
	 */
	public static void fireNetworkInvalidateAdjacent(IBlockAccess world, int x, int y, int z) {
		Set<NetworkVisitor.SearchLoc> visited = new HashSet<NetworkVisitor.SearchLoc>();

		for (int dir = 0; dir < 6; dir++) {
			invalidateVisitor.visitNetwork(
				world,
				Facing.offsetsXForSide[dir] + x,
				Facing.offsetsYForSide[dir] + y,
				Facing.offsetsZForSide[dir] + z,
				visited
			);
		}
	}

	/**
	 * Fire {@link INetworkNode#networkInvalidated()} on the current network
	 *
	 * @param world The world the block lies in
	 * @param x     The X position of the node
	 * @param y     The Y position of the node
	 * @param z     The Z position of the node
	 */
	public static void fireNetworkInvalidate(IBlockAccess world, int x, int y, int z) {
		invalidateVisitor.visitNetwork(world, x, y, z);
	}

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

		INetworkNode node = NetworkRegistry.getNode(world, x, y, z);
		return node != null && node.canBeVisited(direction.getOpposite());
	}

	/**
	 * Send a packet across the network
	 *
	 * @param world  The world the block lies in
	 * @param x      The X position of the node
	 * @param y      The Y position of the node
	 * @param z      The Z position of the node
	 * @param packet Packet to send
	 */
	public static void sendPacket(IBlockAccess world, int x, int y, int z, final Packet packet) {
		new NetworkVisitor() {
			@Override
			protected void visitNode(INetworkNode node, int distance) {
				node.receivePacket(packet, distance);
			}
		}.visitNetwork(world, x, y, z);
	}
}
