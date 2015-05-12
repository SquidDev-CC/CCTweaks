package org.squiddev.cctweaks.api.peripheral;

import org.squiddev.cctweaks.api.network.INetworkedPeripheral;

/**
 * An extension class to {@code openperipheral.api.adapter.IAdapter}
 *
 * Requires OpenPeripheral to function.
 * This will provide the same methods as {@link INetworkedPeripheral}
 */
public interface INetworkedAdapter extends INetworkedPeripheral {
	/**
	 * Environment variable (see {@code openperipheral.api.adapter.method.Env})
	 * for the network this is connected to.
	 *
	 * This is an instance of {@link org.squiddev.cctweaks.api.network.INetworkAccess} that
	 * delegates to all connected networks.
	 */
	String ARG_NETWORK = "cctweaks.networkAccess";
}
