package org.squiddev.cctweaks.core.network.mock;

/**
 * Nodes
 */
public class NamedNetworkNode extends CountingNetworkNode {
	public final String name;

	public NamedNetworkNode(String name) {
		super();
		this.name = name;
	}

	public NamedNetworkNode(String name, boolean[] canVisit) {
		super(canVisit);
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
