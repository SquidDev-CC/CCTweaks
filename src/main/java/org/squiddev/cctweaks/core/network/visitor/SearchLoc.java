package org.squiddev.cctweaks.core.network.visitor;

import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.api.network.INetworkNode;
import org.squiddev.cctweaks.api.network.ISearchLoc;
import org.squiddev.cctweaks.api.network.NetworkAPI;
import org.squiddev.cctweaks.core.utils.WorldPosition;

/*
 * The location we should search for nodes in
 */
public final class SearchLoc extends WorldPosition implements ISearchLoc {
	public final int distanceTravelled;

	protected final int hash;

	protected INetworkNode node = null;

	public SearchLoc(IBlockAccess world, int x, int y, int z, int distanceTravelled) {
		super(world, x, y, z);
		this.distanceTravelled = distanceTravelled;

		// Cache the hash code as we store this in a map
		this.hash = super.hashCode();
	}

	public SearchLoc(IWorldPosition position, int distanceTravelled) {
		this(position.getWorld(), position.getX(), position.getY(), position.getZ(), distanceTravelled);
	}

	@Override
	public boolean equals(Object o) {
		return this == o || (o instanceof ISearchLoc && super.equals(o));
	}

	@Override
	public INetworkNode getNode() {
		INetworkNode node = this.node;
		if (node != null) return node;

		if (y >= 0) {
			node = NetworkAPI.registry().getNode(world, x, y, z);
			if (node != null) this.node = node;
		}

		return node;
	}

	@Override
	public int getDistance() {
		return distanceTravelled;
	}

	public static SearchLoc locationInDirection(ISearchLoc loc, ForgeDirection direction) {
		return new SearchLoc(
			loc.getWorld(),
			loc.getX() + direction.offsetX,
			loc.getY() + direction.offsetY,
			loc.getZ() + direction.offsetZ,
			loc.getDistance() + 1
		);
	}
}
