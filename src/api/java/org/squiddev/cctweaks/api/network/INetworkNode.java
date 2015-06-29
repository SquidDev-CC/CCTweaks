package org.squiddev.cctweaks.api.network;

import dan200.computercraft.api.peripheral.IPeripheral;

import java.util.Map;

/**
 * Defines a node on the network
 */
public interface INetworkNode {
	/**
	 * Get connected peripherals this node provides.
	 *
	 * If this changes, call {@link INetworkController#invalidateNode(INetworkNode)}
	 *
	 * @return Map of name to peripheral. This should *never* be {@code null}.
	 * @see INetworkController#getPeripheralsOnNetwork()
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
	 * @param oldPeripherals Peripherals removed from the network
	 * @param newPeripherals Peripherals added to the network
	 */
	void networkInvalidated(Map<String, IPeripheral> oldPeripherals, Map<String, IPeripheral> newPeripherals);

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
