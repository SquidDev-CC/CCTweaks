package org.squiddev.cctweaks.core.network;

import dan200.computercraft.api.peripheral.IPeripheral;
import org.squiddev.cctweaks.api.network.INetworkController;
import org.squiddev.cctweaks.api.network.INetworkNode;
import org.squiddev.cctweaks.api.network.Packet;

import java.util.Collections;
import java.util.Map;

/**
 * A base node class
 */
public abstract class AbstractNode implements INetworkNode {
	/**
	 * The network this modem is attached to.
	 */
	protected INetworkController networkController;

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
	public void detachFromNetwork() {
		if (networkController == null) {
			throw new IllegalStateException("Not connected to network");
		}

		networkController = null;
	}

	@Override
	public void attachToNetwork(INetworkController networkController) {
		if (this.networkController != null) {
			throw new IllegalStateException("Already connected");
		} else if (networkController == null) {
			throw new IllegalArgumentException("Cannot connect to <null>");
		}

		this.networkController = networkController;
	}

	@Override
	public INetworkController getAttachedNetwork() {
		return networkController;
	}

	@Override
	public String toString() {
		String name = getClass().getName();
		return name.substring(name.lastIndexOf('.') + 1) + "@" + Integer.toHexString(this.hashCode());
	}
}
