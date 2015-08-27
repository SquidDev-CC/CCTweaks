package org.squiddev.cctweaks.core.visualiser;

/**
 * Stores data for drawing network graphs
 */
public final class VisualisationData {
	public final Node[] nodes;
	public final Connection[] connections;

	public VisualisationData(Node[] nodes, Connection[] connections) {
		this.nodes = nodes;
		this.connections = connections;
	}

	public static class Node {
		public final String name;
		public final String[] peripherals;

		public Node(String name, String[] peripherals) {
			this.name = name;
			this.peripherals = peripherals;
		}
	}

	public static class PositionedNode extends Node {
		public final int x;
		public final int y;
		public final int z;

		public PositionedNode(String name, String[] peripherals, int x, int y, int z) {
			super(name, peripherals);
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}

	public static class Connection {
		public final Node x;
		public final Node y;

		public Connection(Node x, Node y) {
			this.x = x;
			this.y = y;
		}
	}
}
