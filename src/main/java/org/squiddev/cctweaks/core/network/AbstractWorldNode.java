package org.squiddev.cctweaks.core.network;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.api.network.INetworkController;
import org.squiddev.cctweaks.api.network.INetworkNode;
import org.squiddev.cctweaks.api.network.IWorldNetworkNode;
import org.squiddev.cctweaks.api.network.NetworkAPI;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * Basic world network node class with additional methods
 */
public abstract class AbstractWorldNode extends AbstractNode implements IWorldNetworkNode {
	@Override
	public boolean canConnect(@Nonnull EnumFacing direction) {
		return true;
	}

	/**
	 * Get the adjacent nodes.
	 *
	 * This is primarily used when choosing a network to connect to.
	 * Use {@link org.squiddev.cctweaks.core.network.cable.BasicCable} if you need
	 * more advanced handling.
	 *
	 * This set can be modified in place.
	 */
	public Set<INetworkNode> getConnectedNodes() {
		return NetworkAPI.helpers().getAdjacentNodes(this);
	}

	/**
	 * Attempt to connect to {@link #getConnectedNodes()} using {@link NetworkHelpers#joinOrCreateNetwork(INetworkNode, Set)}
	 */
	public void connect() {
		NetworkAPI.helpers().joinOrCreateNetwork(this, getConnectedNodes());
	}

	/**
	 * Remove this node from the network
	 */
	public void destroy() {
		INetworkController controller = getAttachedNetwork();
		if (controller != null) controller.removeNode(this);
	}

	@Override
	public String toString() {
		IWorldPosition position = getPosition();
		BlockPos pos = position.getPosition();
		return super.toString() + String.format(" (%s, %s, %s)", pos.getX(), pos.getY(), pos.getZ());
	}
}
