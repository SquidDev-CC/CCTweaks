package org.squiddev.cctweaks.core.network.mock;

import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.core.network.bridge.NetworkBinding;

import java.util.UUID;

public class BoundNetworkNode extends KeyedNetworkNode {
	public static final UUID id = UUID.randomUUID();

	public NetworkBinding binding = new NetworkBinding(position);

	public BoundNetworkNode(IWorldPosition position, String character) {
		super(position, character);
		binding.setId(id);
	}

	@Override
	public void connect() {
		super.connect();
		binding.connect();
		getAttachedNetwork().formConnection(this, binding);
	}

	static {
		Config.Network.WirelessBridge.enabled = true;
	}
}
