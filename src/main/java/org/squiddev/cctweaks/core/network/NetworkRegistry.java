package org.squiddev.cctweaks.core.network;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.api.network.INetworkNodeProvider;
import org.squiddev.cctweaks.api.network.INetworkRegistry;
import org.squiddev.cctweaks.api.network.IWorldNetworkNode;
import org.squiddev.cctweaks.api.network.IWorldNetworkNodeHost;
import org.squiddev.cctweaks.core.utils.DebugLogger;

import java.util.HashSet;
import java.util.Set;

/**
 * Registry for network components
 */
public final class NetworkRegistry implements INetworkRegistry {
	private final Set<INetworkNodeProvider> providers = new HashSet<INetworkNodeProvider>();

	@Override
	public void addNodeProvider(INetworkNodeProvider provider) {
		if (provider != null) providers.add(provider);
	}

	@Override
	public boolean isNode(IBlockAccess world, int x, int y, int z) {
		return y >= 0 && isNode(world.getTileEntity(x, y, z));
	}

	@Override
	public boolean isNode(TileEntity tile) {
		if (tile == null) return false;

		if (tile instanceof IWorldNetworkNode || tile instanceof IWorldNetworkNodeHost) return true;

		for (INetworkNodeProvider provider : providers) {
			try {
				if (provider.isNode(tile)) return true;
			} catch (Exception e) {
				DebugLogger.debug("Node provider " + provider + " threw exception", e);
			}
		}

		return false;
	}

	@Override
	public boolean isNode(IWorldPosition position) {
		return isNode(position.getWorld(), position.getX(), position.getY(), position.getZ());
	}


	@Override
	public IWorldNetworkNode getNode(IBlockAccess world, int x, int y, int z) {
		return y >= 0 ? getNode(world.getTileEntity(x, y, z)) : null;
	}

	@Override
	public IWorldNetworkNode getNode(TileEntity tile) {
		if (tile == null) return null;

		if (tile instanceof IWorldNetworkNode) return (IWorldNetworkNode) tile;
		if (tile instanceof IWorldNetworkNodeHost) return ((IWorldNetworkNodeHost) tile).getNode();

		for (INetworkNodeProvider provider : providers) {
			try {
				IWorldNetworkNode node = provider.getNode(tile);
				if (node != null) return node;
			} catch (Exception e) {
				DebugLogger.debug("Node provider " + provider + " threw exception", e);
			}
		}

		return null;
	}

	@Override
	public IWorldNetworkNode getNode(IWorldPosition position) {
		return getNode(position.getWorld(), position.getX(), position.getY(), position.getZ());
	}
}
