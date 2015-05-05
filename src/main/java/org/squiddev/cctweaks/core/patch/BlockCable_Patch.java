package org.squiddev.cctweaks.core.patch;

import net.minecraft.world.IBlockAccess;
import org.squiddev.cctweaks.api.network.NetworkAPI;

/**
 * Patches {@link dan200.computercraft.shared.peripheral.common.BlockCable#isCable(IBlockAccess, int, int, int)}
 */
@SuppressWarnings("unused")
public final class BlockCable_Patch {
	public static boolean isCable(IBlockAccess world, int x, int y, int z) {
		return NetworkAPI.registry().isNode(world, x, y, z);
	}
}
