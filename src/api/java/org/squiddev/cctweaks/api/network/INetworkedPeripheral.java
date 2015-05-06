package org.squiddev.cctweaks.api.network;

public interface INetworkedPeripheral {
	void attachToNetwork(INetworkAccess network, String name);
	void detachFromNetwork(INetworkAccess network, String name);
}
