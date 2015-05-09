package org.squiddev.cctweaks.core.network.mock;

import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.api.network.INetworkNode;
import org.squiddev.cctweaks.api.network.Packet;

import java.util.Map;

/**
 * A TileEntity that delegates to other nodes
 */
public class NodeTile extends TileEntity implements INetworkNode {
	public final INetworkNode delegate;

	public NodeTile(INetworkNode delegate, int x, int z) {
		this.delegate = delegate;
		xCoord = x;
		zCoord = z;
	}

	@Override
	public boolean canBeVisited(ForgeDirection from) {
		return delegate.canBeVisited(from);
	}

	@Override
	public boolean canVisitTo(ForgeDirection to) {
		return delegate.canVisitTo(to);
	}

	@Override
	public Map<String, IPeripheral> getConnectedPeripherals() {
		return delegate.getConnectedPeripherals();
	}

	@Override
	public void receivePacket(Packet packet, int distanceTravelled) {
		delegate.receivePacket(packet, distanceTravelled);
	}

	@Override
	public void networkInvalidated() {
		delegate.networkInvalidated();
	}

	@Override
	public Iterable<IWorldPosition> getExtraNodes() {
		return delegate.getExtraNodes();
	}

	@Override
	public Object lock() {
		return delegate.lock();
	}

	@Override
	public String toString() {
		return String.format("Node<x: %s, z: %s>(%s)", xCoord, zCoord, delegate);
	}
}
