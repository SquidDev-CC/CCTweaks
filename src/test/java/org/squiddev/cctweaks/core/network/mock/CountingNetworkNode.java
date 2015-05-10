package org.squiddev.cctweaks.core.network.mock;

import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraftforge.common.util.ForgeDirection;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.api.network.INetworkNode;
import org.squiddev.cctweaks.api.network.Packet;

import java.util.Map;

/**
 * A network node that counts how many times an event occured
 */
public class CountingNetworkNode implements INetworkNode {
	protected final boolean[] canVisit;
	protected int invalidated = 0;

	private final Object lock = new Object();

	public CountingNetworkNode(boolean[] canVisit) {
		this.canVisit = canVisit;
	}

	protected CountingNetworkNode() {
		this(new boolean[]{true, true, true, true, true});
	}

	@Override
	public boolean canBeVisited(ForgeDirection from) {
		return canVisitTo(from);
	}

	@Override
	public boolean canVisitTo(ForgeDirection to) {
		return to.offsetY == 0 && canVisit[to.ordinal() - 2];
	}

	@Override
	public Map<String, IPeripheral> getConnectedPeripherals() {
		return null;
	}

	@Override
	public void receivePacket(Packet packet, int distanceTravelled) {
	}

	@Override
	public void networkInvalidated() {
		invalidated++;
	}

	@Override
	public Iterable<IWorldPosition> getExtraNodes() {
		return null;
	}

	@Override
	public Object lock() {
		return lock;
	}

	/**
	 * Get the number of times invalidated
	 *
	 * @return Number of times invalidated
	 */
	public int invalidated() {
		return invalidated;
	}

	/**
	 * Reset the invalidated count
	 */
	public void reset() {
		invalidated = 0;
	}
}
