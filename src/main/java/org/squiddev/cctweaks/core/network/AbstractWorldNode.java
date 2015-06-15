package org.squiddev.cctweaks.core.network;

import net.minecraftforge.common.util.ForgeDirection;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.api.network.INetworkNode;
import org.squiddev.cctweaks.api.network.IWorldNetworkNode;
import org.squiddev.cctweaks.api.network.NetworkAPI;
import org.squiddev.cctweaks.core.utils.DebugLogger;

import java.util.HashSet;
import java.util.Set;

/**
 * Basic world network node class with additional methods
 */
public abstract class AbstractWorldNode extends AbstractNode implements IWorldNetworkNode {
	@Override
	public boolean canConnect(ForgeDirection direction) {
		return true;
	}

	/**
	 * Get the adjacent nodes.
	 *
	 * This is primarily used when choosing a network to connect to.
	 * Use {@link org.squiddev.cctweaks.core.network.cable.BasicCable} if you need
	 * more advanced handling.
	 */
	public Set<INetworkNode> getConnectedNodes() {
		Set<INetworkNode> nodes = new HashSet<INetworkNode>();
		IWorldPosition position = getPosition();

		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			if (canConnect(direction)) {
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
	 * Attempt to connect to {@link #getConnectedNodes()}
	 * This is a one way attempt: It will not assimilate nodes into the network
	 */
	public void connect() {
		if (getAttachedNetwork() != null) return;

		for (INetworkNode node : getConnectedNodes()) {
			if (node.getAttachedNetwork() != null) {
				node.getAttachedNetwork().formConnection(node, this);
				DebugLogger.debug(this + "Connecting to " + node.getAttachedNetwork() + " from " + node);
			} else {
				DebugLogger.debug(this + " Node has no network " + node);
			}
		}

		if (this.getAttachedNetwork() == null) {
			DebugLogger.debug(this + " Creating new network");
			NetworkHelpers.joinNewNetwork(this);
		}
	}

	/**
	 * Remove this node from the network
	 * TODO: Fix internal nodes?
	 */
	public void destroy() {
		if (networkController != null) networkController.removeNode(this);
	}
}
