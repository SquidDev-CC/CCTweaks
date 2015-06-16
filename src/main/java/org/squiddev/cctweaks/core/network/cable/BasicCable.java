package org.squiddev.cctweaks.core.network.cable;

import net.minecraftforge.common.util.ForgeDirection;
import org.squiddev.cctweaks.api.network.INetworkNode;
import org.squiddev.cctweaks.api.network.IWorldNetworkNode;
import org.squiddev.cctweaks.api.network.NetworkAPI;
import org.squiddev.cctweaks.core.network.AbstractWorldNode;

import java.util.HashSet;
import java.util.Set;

// No pun intended...
public abstract class BasicCable extends AbstractWorldNode {
	private int connMap;
	private Set<INetworkNode> attachedNodes = new HashSet<INetworkNode>();

	protected void updateConnectionMap() {
		//TODO: Could we inline this in updateConnections?
		connMap = 0;

		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			if (canConnect(dir)) {
				int x = getPosition().getX() + dir.offsetX;
				int y = getPosition().getY() + dir.offsetY;
				int z = getPosition().getZ() + dir.offsetZ;

				IWorldNetworkNode node = NetworkAPI.registry().getNode(getPosition().getWorld(), x, y, z);
				if (node != null && node.canConnect(dir.getOpposite())) {
					connMap |= 1 << dir.ordinal();
				}
			}
		}
	}

	/**
	 * Update connections and the connection map
	 *
	 * @return If the connections changed
	 */
	public boolean updateConnections() {
		Set<INetworkNode> newNodes = getConnectedNodes();

		// FIXME: Don't think these should be called.
		/*for (INetworkNode newNode : Sets.difference(newNodes, attachedNodes)) {
			networkController.formConnection(this, newNode);
		}

		for (INetworkNode removedNode : Sets.difference(attachedNodes, newNodes)) {
			networkController.breakConnection(new SingleTypeUnorderedPair<INetworkNode>(this, removedNode));
		}*/

		updateConnectionMap();
		return !attachedNodes.equals(attachedNodes = newNodes);
	}

	@Override
	public void connect() {
		super.connect();
		updateConnections();
	}

	public int getConnectionMap() {
		return connMap;
	}

	public boolean doesConnect(ForgeDirection dir) {
		int flag = 1 << dir.ordinal();
		return (connMap & flag) == flag;
	}
}
