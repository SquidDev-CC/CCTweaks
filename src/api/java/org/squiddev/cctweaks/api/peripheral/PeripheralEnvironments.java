package org.squiddev.cctweaks.api.peripheral;

/**
 * Various environments that can be used with openperipheral.api.adapter.method.Env
 *
 * Requires OpenPeripheral to function.
 */
public final class PeripheralEnvironments {
	private PeripheralEnvironments() {
	}

	/**
	 * Environment variable (see openperipheral.api.adapter.method.Env)
	 * for the network this is connected to.
	 *
	 * This is an instance of {@link org.squiddev.cctweaks.api.network.INetworkAccess} that
	 * delegates to all connected networks.
	 */
	public static final String ARG_NETWORK = "cctweaks.networkAccess";
}
