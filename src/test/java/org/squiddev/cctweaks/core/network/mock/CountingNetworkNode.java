package org.squiddev.cctweaks.core.network.mock;

import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.util.EnumFacing;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.api.network.Packet;
import org.squiddev.cctweaks.core.network.AbstractWorldNode;

import java.util.Map;

/**
 * A network node that counts how many times an event occurred
 */
public class CountingNetworkNode extends AbstractWorldNode {
	protected final boolean[] canVisit;
	protected final IWorldPosition position;
	protected int invalidated = 0;
	protected double distance = -1;

	public CountingNetworkNode(IWorldPosition position, boolean[] canVisit) {
		this.canVisit = canVisit;
		this.position = position;
	}

	protected CountingNetworkNode(IWorldPosition position) {
		this(position, new boolean[]{true, true, true, true, true});
	}

	@Override
	public void networkInvalidated(Map<String, IPeripheral> oldPeripherals, Map<String, IPeripheral> newPeripherals) {
		invalidated++;
	}

	@Override
	public void receivePacket(Packet packet, double distanceTravelled) {
		super.receivePacket(packet, distanceTravelled);
		distance = distanceTravelled;
	}

	@Override
	public boolean canConnect(EnumFacing direction) {
		return direction.getAxis() != EnumFacing.Axis.Y && canVisit[direction.ordinal() - 2];
	}

	@Override
	public IWorldPosition getPosition() {
		return position;
	}

	public int invalidated() {
		return invalidated;
	}

	public double distance() {
		return distance;
	}

	public void reset() {
		invalidated = 0;
	}

}
