package org.squiddev.cctweaks.api.network;

import dan200.computercraft.api.peripheral.IPeripheral;

import java.util.Map;
import java.util.Set;

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
	void receivePacket(Packet packet, int distanceTravelled);

	/**
	 * Called when the peripheral map on the network changes.
	 *
	 * @param oldPeripherals A map representing the peripheral
	 *                          map before the network changed
	 */
	void networkInvalidated(Map<String, IPeripheral> oldPeripherals);

	/**
	 * Object to synchronise on whilst calling {@link #networkInvalidated}
	 *
	 * @return The object to synchronise on
	 */
	Object lock();

	/**
	 * Get network nodes that this node attaches to the network.
	 *
	 * You must manually return in-world nodes that are adjacent.
	 * These will not be found for you.
	 *
	 * While a node is being removed, it still needs to provide
	 * a set of nodes with this method, in order to determine
	 * what it is being detached from.
	 *
	 * @return All nodes this node connects to.
	 */
	Set<INetworkNode> getConnectedNodes();

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
}
