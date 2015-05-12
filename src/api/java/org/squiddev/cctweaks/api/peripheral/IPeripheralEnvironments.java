package org.squiddev.cctweaks.api.peripheral;

import org.squiddev.cctweaks.api.network.INetworkedPeripheral;

/**
 * Various environments that can be used with
 * {@link openperipheral.api.adapter.method.Env}
 *
 * Requires OpenPeripheral to function.
 */
public interface IPeripheralEnvironments extends INetworkedPeripheral {
	/**
	 * Environment variable (see {@code openperipheral.api.adapter.method.Env})
	 * for the network this is connected to.
	 *
	 * This is an instance of {@link org.squiddev.cctweaks.api.network.INetworkAccess} that
	 * delegates to all connected networks.
	 */
	String ARG_NETWORK = "cctweaks.networkAccess";
}
