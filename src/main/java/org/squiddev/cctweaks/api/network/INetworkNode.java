package org.squiddev.cctweaks.api.network;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.peripheral.modem.TileCable;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Map;

/**
 * Defines a node on the network
 */
public interface INetworkNode {
	/**
	 * If this node can be visited whilst scanning.
	 * If you return false, nodes after this one will not be scanned
	 *
	 * @param from	The direction the node is being visited from. Might be UNKNOWN
	 *
	 * @return If this node can be visited
	 */
	boolean canBeVisited(ForgeDirection from);

	/**
	 * If the network can visit nodes that may be found one block away in a particular direction.
	 *
	 * @param to	The direction of the block that may be visited
	 * @return if this node doesn't block connection in the direction.
	 */
	boolean canVisitTo(ForgeDirection to);

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
	 * Called when a node is destroyed on the network
	 *
	 * @see #networkChanged()
	 * @see TileCable#networkChanged()
	 */
	void invalidateNetwork();

	/**
	 * Called when an adjacent network node is destroyed
	 *
	 * @see #invalidateNetwork()
	 * @see TileCable#networkChanged()
	 */
	void networkChanged();

	/**
	 * Get a list of extra node search locations.
	 *
	 * This is used by {@link NetworkVisitor} to find nodes in non-adjacent blocks
	 *
	 * @return Array of custom search locations, or {@code null} if none provided
	 */
	NetworkVisitor.SearchLoc[] getExtraNodes();

	/**
	 * Object to synchronise on whilst calling {@link #invalidateNetwork}
	 *
	 * @return The object to synchronise on
	 */
	Object lock();
}
