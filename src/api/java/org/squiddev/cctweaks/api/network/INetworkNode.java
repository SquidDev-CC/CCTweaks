package org.squiddev.cctweaks.api.network;

import dan200.computercraft.api.peripheral.IPeripheral;

import java.util.Map;

/**
 * Defines a node on the network
 */
public interface INetworkNode {
	/**
	 * Get connected peripherals this node provides
	 *
	 * @return Map of name to peripheral or {@code null} if there are no peripherals
	 */
	Map<String, IPeripheral> getConnectedPeripherals();

	/**
	 * Receive a packet on the network
	 * This will be a packet sent with rednet or modem.transmit
	 *
	 * @param packet            The packet to send
	 * @param distanceTravelled Distance traveled by the packet
	 */
	void receivePacket(Packet packet, double distanceTravelled);

	/**
	 * Called when the peripheral map on the network changes.
	 *
	 * @param oldPeripherals A map representing the peripheral
	 *                          map before the network changed
	 */
	void networkInvalidated(Map<String, IPeripheral> oldPeripherals);

	/**
	 * Called when the network is detached from this node.
	 */
	void detachFromNetwork();

	/**
	 * Called when the network controller assimilates this node.
	 *
	 * @param networkController The network this node is being added to.
	 */
	void attachToNetwork(INetworkController networkController);

	/**
	 * Should return the network this node was last attached to.
	 *
	 * @return The network this node is attached to.
	 */
	INetworkController getAttachedNetwork();
}
