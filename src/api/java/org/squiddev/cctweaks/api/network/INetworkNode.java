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
	 * Called when the peripheral map on the network changes
	 *
	 * This is also called when attaching or detaching from a network with peripherals
	 *
	 * If required, this will be called just after attaching to a network and
	 * just before detaching. The node will be *always* be attached to a network.
	 *
	 * @param oldPeripherals Peripherals removed from the network
	 * @param newPeripherals Peripherals added to the network
	 */
	void networkInvalidated(Map<String, IPeripheral> oldPeripherals, Map<String, IPeripheral> newPeripherals);

	/**
	 * Called when the network is detached from this node.
	 *
	 * You should NEVER call this method yourself: use {@link INetworkController#removeNode(INetworkNode)}
	 * instead.
	 *
	 * The network may be in an indeterminate state when this is called: do not
	 * perform any processing: this should just be a getter or setter.
	 */
	void detachFromNetwork();

	/**
	 * Called when the network controller assimilates this node.
	 *
	 * You should NEVER call this method yourself: use
	 * {@link INetworkController#formConnection(INetworkNode, INetworkNode)} instead.
	 *
	 * The network may be in an indeterminate state when this is called: do not
	 * perform any processing: this should just be a getter or setter.
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
