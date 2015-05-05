package org.squiddev.cctweaks.api.network;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import org.squiddev.cctweaks.api.IWorldPosition;

import java.util.Set;

/**
 * Handles traversing networks
 *
 * Network state is not cached, so try to use this as minimally as possible.
 *
 * Ideally network traversal should happen on the main thread, so try to defer traversal
 * until the next tick - though this is not always possible.
 */
public interface INetworkVisitor {
	/**
	 * Visit every node in a network
	 *
	 * @param world The world to start traversal in
	 * @param x     The X position to start at
	 * @param y     The Y position to start at
	 * @param z     The Z position to start at
	 * @return An iterable for every node in the network
	 */
	Iterable<ISearchLoc> visitNetwork(IBlockAccess world, int x, int y, int z);

	/**
	 * Visit every node in a network
	 *
	 * @param world   The world to start traversal in
	 * @param x       The X position to start at
	 * @param y       The Y position to start at
	 * @param z       The Z position to start at
	 * @param visited Collection of previously visited nodes
	 * @return An iterable for every node in the network
	 */
	Iterable<ISearchLoc> visitNetwork(IBlockAccess world, int x, int y, int z, Set<ISearchLoc> visited);

	/**
	 * Visit every node in a network
	 *
	 * @param tile TileEntity to start traversal at
	 * @return An iterable for every node in the network
	 */
	Iterable<ISearchLoc> visitNetwork(TileEntity tile);

	/**
	 * Visit every node in a network
	 *
	 * @param tile    TileEntity to start traversal at
	 * @param visited Collection of previously visited nodes
	 * @return An iterable for every node in the network
	 */
	Iterable<ISearchLoc> visitNetwork(TileEntity tile, Set<ISearchLoc> visited);

	/**
	 * Visit every node in a network
	 *
	 * @param position Position to start traversal at
	 * @return An iterable for every node in the network
	 */
	Iterable<ISearchLoc> visitNetwork(IWorldPosition position);

	/**
	 * Visit every node in a network
	 *
	 * @param position Position to start traversal at
	 * @param visited  Collection of previously visited nodes
	 * @return An iterable for every node in the network
	 */
	Iterable<ISearchLoc> visitNetwork(IWorldPosition position, Set<ISearchLoc> visited);

	/**
	 * Visit every node in a network
	 *
	 * @param positions Collection of positions to start traversal at
	 * @return An iterable for every node in the network
	 * @see #visitNetwork(IWorldPosition, Set)
	 */
	Iterable<ISearchLoc> visitNetwork(Iterable<IWorldPosition> positions);
}
