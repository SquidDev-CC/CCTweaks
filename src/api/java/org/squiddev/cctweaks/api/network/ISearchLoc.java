package org.squiddev.cctweaks.api.network;

import org.squiddev.cctweaks.api.IWorldPosition;

/**
 * A point on network traversal {@link INetworkVisitor}
 */
public interface ISearchLoc extends IWorldPosition {
	/**
	 * Get the distance from the start of traversal
	 *
	 * @return The distance travelled
	 */
	int getDistance();

	/**
	 * Get the current node
	 *
	 * @return The node at this location
	 */
	INetworkNode getNode();
}
