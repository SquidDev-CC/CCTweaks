package org.squiddev.cctweaks.core.network;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.api.network.INetworkNode;
import org.squiddev.cctweaks.api.network.INetworkNodeHost;
import org.squiddev.cctweaks.api.network.INetworkNodeProvider;
import org.squiddev.cctweaks.api.network.INetworkRegistry;
import org.squiddev.cctweaks.core.utils.DebugLogger;

import java.util.HashSet;
import java.util.Set;

/**
 * Registry for network components
 */
public final class NetworkRegistry implements INetworkRegistry {
	private final Set<INetworkNodeProvider> providers = new HashSet<INetworkNodeProvider>();

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

		if (tile instanceof INetworkNode || tile instanceof INetworkNodeHost) return true;

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
	public INetworkNode getNode(IBlockAccess world, int x, int y, int z) {
		return y >= 0 ? getNode(world.getTileEntity(x, y, z)) : null;
	}

	@Override
	public INetworkNode getNode(TileEntity tile) {
		if (tile == null) return null;

		if (tile instanceof INetworkNode) return (INetworkNode) tile;
		if (tile instanceof INetworkNodeHost) return ((INetworkNodeHost) tile).getNode();

		for (INetworkNodeProvider provider : providers) {
			try {
				INetworkNode node = provider.getNode(tile);
				if (node != null) return node;
			} catch (Exception e) {
				DebugLogger.debug("Node provider " + provider + " threw exception", e);
			}
		}

		return null;
	}

	@Override
	public INetworkNode getNode(IWorldPosition position) {
		return getNode(position.getWorld(), position.getX(), position.getY(), position.getZ());
	}
}
