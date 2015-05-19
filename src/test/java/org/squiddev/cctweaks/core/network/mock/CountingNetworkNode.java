package org.squiddev.cctweaks.core.network.mock;

import dan200.computercraft.api.peripheral.IPeripheral;
import org.squiddev.cctweaks.api.network.INetworkController;
import org.squiddev.cctweaks.api.network.INetworkNode;
import org.squiddev.cctweaks.api.network.Packet;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * A network node that counts how many times an event occured
 */
public class CountingNetworkNode implements INetworkNode {
	protected final boolean[] canVisit;
	protected int invalidated = 0;

	private INetworkController networkController;

	public CountingNetworkNode(boolean[] canVisit) {
		this.canVisit = canVisit;
	}

	protected CountingNetworkNode() {
		this(new boolean[]{true, true, true, true, true});
	}

	@Override
	public Map<String, IPeripheral> getConnectedPeripherals() {
		return null;
	}

	@Override
	public void receivePacket(INetworkController networkController, Packet packet, double distanceTravelled) {
	}

	@Override
	public void networkInvalidated(Map<String, IPeripheral> oldPeripherals) {
		invalidated++;
	}

	@Override
	public Set<INetworkNode> getConnectedNodes() {
		return Collections.emptySet();
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
