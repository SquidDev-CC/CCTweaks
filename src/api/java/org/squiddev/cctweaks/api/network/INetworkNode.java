package org.squiddev.cctweaks.api.network;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.peripheral.modem.TileCable;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import org.squiddev.cctweaks.api.IWorldPosition;

import java.util.Map;

/**
 * Defines a node on the network
 */
public interface INetworkNode {
	/**
	 * If this node can be visited whilst scanning.
	 * If you return false, nodes after this one will not be scanned
	 *
	 * You must fire a block update if this is changed, otherwise
	 * other blocks may not notice this
	 *
	 * @param from The direction the node is being visited from. Might be UNKNOWN
	 * @return If this node can be visited
	 */
	boolean canBeVisited(ForgeDirection from);

	/**
	 * If the network can visit nodes that may be found one block away in a particular direction.
	 *
	 * The visitor will determine if the adjacent block can be visited from this direction.
	 * No need for this method to try to determine if the adjacent block wants to be visited.
	 *
	 * @param to The direction of the block that may be visited
	 * @return If this node doesn't block connection in the direction.
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
	 * Called when the network is changed in some way
	 *
	 * This includes adding/removing nodes or changing peripherals
	 *
	 * @see NetworkHelpers#fireNetworkInvalidate(IBlockAccess, int, int, int)
	 * @see NetworkHelpers#fireNetworkInvalidateAdjacent(IBlockAccess, int, int, int)
	 * @see TileCable#networkChanged()
	 */
	void networkInvalidated();

	/**
	 * Get a list of extra node search locations.
	 *
	 * This is used by {@link NetworkVisitor} to find nodes in non-adjacent blocks
	 *
	 * @return Array of custom search locations, or {@code null} if none provided
	 */
	Iterable<IWorldPosition> getExtraNodes();

	/**
	 * Object to synchronise on whilst calling {@link #networkInvalidated}
	 *
	 * @return The object to synchronise on
	 */
	Object lock();
}
