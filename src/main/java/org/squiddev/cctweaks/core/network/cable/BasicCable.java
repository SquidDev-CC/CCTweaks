package org.squiddev.cctweaks.core.network.cable;

import com.google.common.collect.Sets;
import net.minecraft.util.EnumFacing;
import org.squiddev.cctweaks.api.SingleTypeUnorderedPair;
import org.squiddev.cctweaks.api.network.INetworkHelpers;
import org.squiddev.cctweaks.api.network.INetworkNode;
import org.squiddev.cctweaks.api.network.NetworkAPI;
import org.squiddev.cctweaks.core.network.AbstractWorldNode;

import java.util.Collections;
import java.util.Set;

/**
 * A world node that caches where it can connect to and
 * handles adding and removing connections
 */
public abstract class BasicCable extends AbstractWorldNode {
	private int connMap;
	private Set<INetworkNode> connections = Collections.emptySet();

	protected boolean updateConnectionMap() {
		int map = 0;

		INetworkHelpers helpers = NetworkAPI.helpers();
		for (EnumFacing dir : EnumFacing.VALUES) {
			if (canConnect(dir) && helpers.canConnect(getPosition(), dir)) {
				map |= 1 << dir.ordinal();
			}
		}

		if (connMap != map) {
			connMap = map;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Update the connection map.
	 *
	 * On a server, this will also connect/disconnect networks
	 *
	 * @return If the connections changed
	 */
	public boolean updateConnections() {
		if (getAttachedNetwork() != null) {
			Set<INetworkNode> attachedNodes = connections;
			Set<INetworkNode> newNodes = connections = getConnectedNodes();

			for (INetworkNode newNode : Sets.difference(newNodes, attachedNodes)) {
				getAttachedNetwork().formConnection(this, newNode);
			}

			for (INetworkNode removedNode : Sets.difference(attachedNodes, newNodes)) {
				SingleTypeUnorderedPair<INetworkNode> connection = new SingleTypeUnorderedPair<INetworkNode>(this, removedNode);

				// The network can/will change whilst the loop is iterating.
				if (getAttachedNetwork().getNodeConnections().contains(connection)) {
					getAttachedNetwork().breakConnection(connection);
				}
			}
		}

		return updateConnectionMap();
	}

	@Override
	public void connect() {
		super.connect();
		updateConnections();
	}

	public boolean doesConnect(EnumFacing dir) {
		int flag = 1 << dir.ordinal();
		return (connMap & flag) == flag;
	}

	@Override
	public String toString() {
		return "Cable: " + super.toString();
	}
}
