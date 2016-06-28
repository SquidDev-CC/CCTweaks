package org.squiddev.cctweaks.core.network.mock;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.squiddev.cctweaks.api.network.IWorldNetworkNode;
import org.squiddev.cctweaks.api.network.IWorldNetworkNodeHost;
import org.squiddev.cctweaks.blocks.TileBase;

public class NodeTile extends TileBase implements IWorldNetworkNodeHost {
	public IWorldNetworkNode node;
	public final IBlockAccess world;

	public NodeTile(IBlockAccess world, int x, int z) {
		this.world = world;
		this.pos = new BlockPos(x, 0, z);
	}

	@Override
	public IWorldNetworkNode getNode() {
		return node;
	}

	@Override
	public IBlockAccess getBlockAccess() {
		return world;
	}

	@Override
	public String toString() {
		return String.format("Tile<%s>(%s)", pos, node);
	}
}
