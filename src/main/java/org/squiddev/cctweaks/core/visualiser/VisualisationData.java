package org.squiddev.cctweaks.core.visualiser;

import net.minecraft.util.BlockPos;
import org.squiddev.cctweaks.api.UnorderedPair;

import java.util.Collection;
import java.util.List;

/**
 * Stores data for drawing network graphs
 */
public final class VisualisationData {
	public final List<Node> nodes;
	public final Collection<UnorderedPair<Node>> connections;

	public VisualisationData(List<Node> nodes, List<UnorderedPair<Node>> connections) {
		this.nodes = nodes;
		this.connections = connections;
	}

	public static class Node {
		public final int id;
		public String name;
		public BlockPos position;

		public Node(int id) {
			this.id = id;
		}

		public Node(int id, String name, BlockPos position) {
			this.id = id;
			this.name = name;
			this.position = position;
		}

		@Override
		public String toString() {
			return "Node{#" + id + " name=" + name + " position=" + position + "}";
		}
	}
}
