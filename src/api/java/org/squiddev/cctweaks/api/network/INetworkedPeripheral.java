package org.squiddev.cctweaks.api.network;

import dan200.computercraft.api.peripheral.IPeripheral;

import java.util.Map;

public interface INetworkedPeripheral extends INetworkCompatiblePeripheral {
	/**
	 * Called when this peripheral is attached to a network.
	 *
	 * @param network Access to the network being attached to.
	 * @param name    The name of this peripheral on that network.
	 */
	void attachToNetwork(INetworkAccess network, String name);

	/**
	 * Called when this peripheral is detached from a network.
	 *
	 * @param network Access to the network being detached from.
	 * @param name    The name of this peripheral on that network.
	 */
	void detachFromNetwork(INetworkAccess network, String name);

	/**
	 * Called when the network is invalidated.
	 *
	 * @param network        The network that was invalidated.
	 * @param oldPeripherals Peripherals removed from the network
	 * @param newPeripherals Peripherals added to the network
	 */
	void networkInvalidated(INetworkAccess network, Map<String, IPeripheral> oldPeripherals, Map<String, IPeripheral> newPeripherals);

	/**
	 * Called when the network receives a packet.
	 *
	 * @param network           The network this packet was sent on.
	 * @param packet            The packet received.
	 * @param distanceTravelled The distance that packet travelled.
	 */
	void receivePacket(INetworkAccess network, Packet packet, double distanceTravelled);
}
