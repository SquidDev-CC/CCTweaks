package org.squiddev.cctweaks.core.visualiser;

import java.util.Arrays;

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
		public final Position position;

		public Node(String name, String[] peripherals, Position position) {
			this.name = name;
			this.peripherals = peripherals;
			this.position = position;
		}

		@Override
		public String toString() {
			return "Node{" + name + " " + Arrays.toString(peripherals) + " " + position + "}";
		}
	}

	public static class Position {
		public final int x;
		public final int y;
		public final int z;

		public Position(int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}

		@Override
		public String toString() {
			return String.format("(%s, %s, %s)", x, y, z);
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
