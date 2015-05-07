package org.squiddev.cctweaks.api.network;

public interface INetworkedPeripheral {
	/**
	 * Called when this peripheral is attached to a network.
	 *
	 * @param network Access to the network being attached to.
	 * @param name	  The name of this peripheral on that network.
	 */
	void attachToNetwork(INetworkAccess network, String name);

	/**
	 * Called when this peripheral is detached from a network.
	 *
	 * @param network Access to the network being detached from.
	 * @param name 	  The name of this peripheral on that network.
	 */
	void detachFromNetwork(INetworkAccess network, String name);

	/**
	 * Called when the network is invalidated.
	 *
	 * @param network The network that was invalidated.
	 */
	void networkInvalidated(INetworkAccess network);
}
