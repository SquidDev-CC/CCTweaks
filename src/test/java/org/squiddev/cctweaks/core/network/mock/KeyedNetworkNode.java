package org.squiddev.cctweaks.core.network.mock;

import org.squiddev.cctweaks.api.IWorldPosition;

public class KeyedNetworkNode extends CountingNetworkNode {
	public final String key;

	public KeyedNetworkNode(IWorldPosition position, String key) {
		super(position);
		this.key = key;
	}

	public KeyedNetworkNode(IWorldPosition position, String key, boolean[] canVisit) {
		super(position, canVisit);
		this.key = key;
	}

	@Override
	public String toString() {
		return key + ": " + super.toString();
	}
}
