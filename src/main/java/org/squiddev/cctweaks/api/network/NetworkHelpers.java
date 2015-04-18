package org.squiddev.cctweaks.api.network;

import net.minecraft.util.Facing;
import net.minecraft.world.IBlockAccess;

/**
 * Helper methods on networks
 */
public class NetworkHelpers {
	private static final NetworkVisitor visitor = new NetworkVisitor() {
		public void visitNode(INetworkNode node, int distance) {
			synchronized (node.lock()) {
				node.invalidateNetwork();
			}
		}
	};

	/**
	 * Fire {@link INetworkNode#networkChanged()} on adjacent nodes
	 *
	 * @param world The world the block lies in
	 * @param x     The X position of the node
	 * @param y     The Y position of the node
	 * @param z     The Z position of the node
	 */
	public static void fireNetworkChanged(IBlockAccess world, int x, int y, int z) {
		for (int dir = 0; dir < 6; dir++) {
			INetworkNode node = NetworkRegistry.getNode(
				world,
				Facing.offsetsXForSide[dir] + x,
				Facing.offsetsYForSide[dir] + y,
				Facing.offsetsZForSide[dir] + z
			);
			if (node != null) node.networkChanged();
		}
	}

	/**
	 * Fire {@link INetworkNode#invalidateNetwork()} on the current network
	 *
	 * @param world The world the block lies in
	 * @param x     The X position of the node
	 * @param y     The Y position of the node
	 * @param z     The Z position of the node
	 */
	public static void fireNetworkInvalidate(IBlockAccess world, int x, int y, int z) {
		visitor.visitNetwork(world, x, y, z);
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
