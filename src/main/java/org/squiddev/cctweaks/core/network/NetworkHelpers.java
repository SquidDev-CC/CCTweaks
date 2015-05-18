package org.squiddev.cctweaks.core.network;

import net.minecraft.util.Facing;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import org.squiddev.cctweaks.api.network.INetworkNode;
import org.squiddev.cctweaks.api.network.ISearchLoc;
import org.squiddev.cctweaks.api.network.NetworkAPI;
import org.squiddev.cctweaks.api.network.Packet;
import org.squiddev.cctweaks.core.network.visitor.NetworkVisitorIterable;

import java.util.HashSet;
import java.util.Set;

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

		INetworkNode node = NetworkAPI.registry().getNode(world, x, y, z);
		return node != null && node.canBeVisited(direction.getOpposite());
	}
}
