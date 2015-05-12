package org.squiddev.cctweaks.core.network.mock;

/**
 * Nodes
 */
public class KeyedNetworkNode extends CountingNetworkNode {
	public final String key;

	public KeyedNetworkNode(String key) {
		super();
		this.key = key;
	}

	public KeyedNetworkNode(String key, boolean[] canVisit) {
		super(canVisit);
		this.key = key;
	}

	@Override
	public String toString() {
		return key;
	}
}
