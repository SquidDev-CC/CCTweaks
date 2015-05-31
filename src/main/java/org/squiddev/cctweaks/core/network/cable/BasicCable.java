package org.squiddev.cctweaks.core.network.cable;

import com.google.common.collect.Sets;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraftforge.common.util.ForgeDirection;
import org.squiddev.cctweaks.api.SingleTypeUnorderedPair;
import org.squiddev.cctweaks.api.network.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// No pun intended...
public abstract class BasicCable implements IWorldNetworkNode {
	private int connMap;
	private INetworkController networkController;
	private Set<INetworkNode> attachedNodes = new HashSet<INetworkNode>();

	// Util

	private Set<IWorldNetworkNode> getConnectedWorldNodes() {
		Set<IWorldNetworkNode> nodes = new HashSet<IWorldNetworkNode>(6);

		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			if (canConnect(dir)) {
				int x = getPosition().getX() + dir.offsetX;
				int y = getPosition().getY() + dir.offsetY;
				int z = getPosition().getZ() + dir.offsetZ;

				IWorldNetworkNode node = NetworkAPI.registry().getNode(getPosition().getWorld(), x, y, z);
				if (node != null && node.canConnect(dir.getOpposite())) {
					nodes.add(node);
				}
			}
		}

		return nodes;
	}

	private void updateConnectionMap() {
		connMap = 0;

		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			if (canConnect(dir)) {
				int x = getPosition().getX() + dir.offsetX;
				int y = getPosition().getY() + dir.offsetY;
				int z = getPosition().getZ() + dir.offsetZ;

				IWorldNetworkNode node = NetworkAPI.registry().getNode(getPosition().getWorld(), x, y, z);
				if (node != null && node.canConnect(dir.getOpposite())) {
					connMap |=  1 << dir.ordinal();
				}
			}
		}
	}

	// Public ops

	public boolean updateConnections() {
		Set<INetworkNode> newNodes = getConnectedNodes();

		for (INetworkNode newNode : Sets.difference(newNodes, attachedNodes)) {
			networkController.formConnection(this, newNode);
		}

		for (INetworkNode removedNode : Sets.difference(attachedNodes, newNodes)) {
			networkController.breakConnection(new SingleTypeUnorderedPair<INetworkNode>(this, removedNode));
		}

		updateConnectionMap();
		return !attachedNodes.equals(attachedNodes = newNodes);
	}

	public int getConnectionMap() {
		return connMap;
	}

	public void removeFromWorld() {
		networkController.removeNode(this);
	}

	public boolean doesConnect(ForgeDirection dir) {
		int flag = 1 << dir.ordinal();
		return (connMap & flag) == flag;
	}

	// IWorldNetworkNode

	@Override
	public Map<String, IPeripheral> getConnectedPeripherals() {
		return Collections.emptyMap();
	}

	@Override
	public void receivePacket(Packet packet, double distanceTravelled) {
	}

	@Override
	public void networkInvalidated(Map<String, IPeripheral> oldPeripherals) {
	}

	@Override
	public Set<INetworkNode> getConnectedNodes() {
		return Collections.<INetworkNode>unmodifiableSet(getConnectedWorldNodes());
	}

	@Override
	public void detachFromNetwork() {
		networkController = null;
	}

	@Override
	public void attachToNetwork(INetworkController networkController) {
		this.networkController = networkController;
	}

	@Override
	public INetworkController getAttachedNetwork() {
		return networkController;
	}
}
