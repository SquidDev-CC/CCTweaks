package org.squiddev.cctweaks.core.visualiser;

import net.minecraft.util.BlockPos;

import java.util.Collection;
import java.util.List;

/**
 * Stores data for drawing network graphs
 */
public final class VisualisationData {
	public final List<Node> nodes;
	public final Collection<Connection> connections;

	public VisualisationData(List<Node> nodes, List<Connection> connections) {
		this.nodes = nodes;
		this.connections = connections;
	}

	public static class Node {
		public final String name;
		public final BlockPos position;

		public Node(String name, BlockPos position) {
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
