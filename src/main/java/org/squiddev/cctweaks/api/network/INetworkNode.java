package org.squiddev.cctweaks.api.network;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.peripheral.modem.TileCable;

/**
 * Defines a node on the network
 */
public interface INetworkNode {
	/**
	 * If this node can be visited whilst scanning.
	 * If you return false, nodes after this one will not be scanned
	 *
	 * @return If this node can be visited
	 */
	boolean canVisit();

	/**
	 * Get the peripheral this node provides if any
	 *
	 * @return The peripheral or {@code null}
	 */
	IPeripheral getConnectedPeripheral();

	/**
	 * Get the peripheral's name
	 *
	 * @return The name or {@code null}
	 */
	String getConnectedPeripheralName();

	/**
	 * Receive a packet on the network
	 *
	 * @param packet            The packet to send
	 * @param distanceTravelled Distance traveled by the packet
	 */
	void receivePacket(Packet packet, int distanceTravelled);

	/**
	 * Called when a node is destroyed on the network
	 *
	 * @see #networkChanged()
	 * @see TileCable#networkChanged()
	 */
	void invalidateNetwork();

	/**
	 * Called when the network is changed.
	 *
	 * @see #invalidateNetwork()
	 * @see TileCable#networkChanged()
	 */
	void networkChanged();

	/**
	 * Object to synchronise on whilst calling {@link #invalidateNetwork}
	 *
	 * @return The object to synchronise on
	 */
	Object lock();
}
