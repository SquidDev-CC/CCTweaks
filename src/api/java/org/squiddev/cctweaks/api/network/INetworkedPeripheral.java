package org.squiddev.cctweaks.api.network;

import dan200.computercraft.api.network.Packet;
import dan200.computercraft.api.peripheral.IPeripheral;

import javax.annotation.Nonnull;
import java.util.Map;

public interface INetworkedPeripheral extends INetworkCompatiblePeripheral {
	/**
	 * Called when this peripheral is attached to a network access.
	 *
	 * @param network Access to the network being attached to.
	 * @param name    The name of this peripheral on that network.
	 */
	void attachToNetwork(@Nonnull INetworkAccess network, @Nonnull String name);

	/**
	 * Called when this peripheral is detached from a network access.
	 *
	 * @param network Access to the network being detached from.
	 * @param name    The name of this peripheral on that network.
	 */
	void detachFromNetwork(@Nonnull INetworkAccess network, @Nonnull String name);

	/**
	 * Called when the peripheral map on the network changes
	 *
	 * This is also called when attaching or detaching from a network with peripherals
	 *
	 * @param network        The network that was invalidated.
	 * @param oldPeripherals Peripherals removed from the network
	 * @param newPeripherals Peripherals added to the network
	 */
	void networkInvalidated(@Nonnull INetworkAccess network, @Nonnull Map<String, IPeripheral> oldPeripherals, @Nonnull Map<String, IPeripheral> newPeripherals);

	/**
	 * Called when the network receives a packet.
	 *
	 * @param network           The network this packet was sent on.
	 * @param packet            The packet received.
	 * @param distanceTravelled The distance that packet travelled.
	 */
	void receivePacket(@Nonnull INetworkAccess network, @Nonnull Packet packet, double distanceTravelled);
}
