package org.squiddev.cctweaks.core.patch;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import org.squiddev.cctweaks.api.network.INetworkNode;
import org.squiddev.cctweaks.api.network.INetworkNodeBlock;
import org.squiddev.cctweaks.api.network.NetworkRegistry;

/**
 * Patches {@link dan200.computercraft.shared.peripheral.common.BlockCable#isCable(IBlockAccess, int, int, int)}
 */
@SuppressWarnings("unused")
public final class BlockCable_Patch implements INetworkNodeBlock {
	public static boolean isCable(IBlockAccess world, int x, int y, int z) {
		return NetworkRegistry.isNode(world, x, y, z);
	}

	public INetworkNode getNode(IBlockAccess world, int x, int y, int z, int meta) {
		TileEntity entity = world.getTileEntity(x, y, z);
		if (entity != null && entity instanceof INetworkNode) {
			return (INetworkNode) entity;
		}
		return null;
	}
}
