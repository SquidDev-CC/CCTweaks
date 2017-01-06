package org.squiddev.cctweaks.core.visualiser;

import net.minecraft.util.BlockPos;
import org.squiddev.cctweaks.api.network.INetworkNode;

/**
 * A node in a visualised network
 */
public class NetworkNode {
	public final int id;
	public final INetworkNode node;
	public String name;
	public BlockPos position;

	public NetworkNode(int id, String name, BlockPos position) {
		this(id, name, position, null);
	}

	public NetworkNode(int id, String name, BlockPos position, INetworkNode node) {
		this.id = id;
		this.name = name;
		this.position = position;
		this.node = node;
	}

	@Override
	public String toString() {
		return "Node{#" + id + " name=" + name + " position=" + position + "}";
	}
}
