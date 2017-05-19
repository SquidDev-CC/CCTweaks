package org.squiddev.cctweaks.core.network;

import com.google.common.base.Preconditions;
import dan200.computercraft.api.network.Packet;
import dan200.computercraft.api.peripheral.IPeripheral;
import org.squiddev.cctweaks.api.network.INetworkController;
import org.squiddev.cctweaks.api.network.INetworkNode;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

/**
 * A base node class
 */
public abstract class AbstractNode implements INetworkNode {
	/**
	 * The network this modem is attached to.
	 */
	private INetworkController networkController;

	@Nonnull
	@Override
	public Map<String, IPeripheral> getConnectedPeripherals() {
		return Collections.emptyMap();
	}

	@Override
	public void receivePacket(@Nonnull Packet packet, double distanceTravelled) {
	}

	@Override
	public void networkInvalidated(@Nonnull Map<String, IPeripheral> oldPeripherals, @Nonnull Map<String, IPeripheral> newPeripherals) {
	}

	@Override
	public void detachFromNetwork() {
		if (networkController == null) {
			throw new IllegalStateException("Not connected to network");
		}

		networkController = null;
	}

	@Override
	public void attachToNetwork(@Nonnull INetworkController networkController) {
		Preconditions.checkNotNull(networkController, "networkController cannot be null");
		if (this.networkController != null) {
			throw new IllegalStateException("Already connected");
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
