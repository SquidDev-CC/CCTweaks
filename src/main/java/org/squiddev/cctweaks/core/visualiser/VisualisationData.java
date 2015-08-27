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
		public final Position position;

		public Node(String name, Position position) {
			this.name = name;
			this.position = position;
		}

		@Override
		public String toString() {
			return "Node{" + name + " " + position + "}";
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof Node)) return false;

			Node node = (Node) o;

			return name.equals(node.name) && !(position != null ? !position.equals(node.position) : node.position != null);

		}

		@Override
		public int hashCode() {
			return 31 * name.hashCode() + (position != null ? position.hashCode() : 0);
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

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof Position)) return false;

			Position position = (Position) o;
			return x == position.x && y == position.y && z == position.z;
		}

		@Override
		public int hashCode() {
			int result = x;
			result = 31 * result + y;
			result = 31 * result + z;
			return result;
		}
	}

	public static class Connection {
		public final Node x;
		public final Node y;

		public Connection(Node x, Node y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof Connection)) return false;

			Connection that = (Connection) o;
			return x.equals(that.x) && y.equals(that.y);
		}

		@Override
		public int hashCode() {
			return x.hashCode() ^ y.hashCode();
		}
	}
}
