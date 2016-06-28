package org.squiddev.cctweaks.core.patch;

import dan200.computercraft.shared.peripheral.common.BlockCable;
import dan200.computercraft.shared.peripheral.common.BlockCableModemVariant;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.squiddev.cctweaks.api.network.NetworkAPI;
import org.squiddev.patcher.visitors.MergeVisitor;

/**
 * Patches {@link dan200.computercraft.shared.peripheral.common.BlockCable#isCable(IBlockAccess, BlockPos)}
 */
@SuppressWarnings("unused")
public final class BlockCable_Patch extends BlockCable {
	public static boolean isCable(IBlockAccess world, BlockPos position) {
		return NetworkAPI.registry().isNode(world, position);
	}

	@SuppressWarnings("unchecked")
	private boolean doesConnect(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing dir) {
		if (state.getValue(Properties.CABLE)) {
			if (((BlockCableModemVariant) state.getValue(Properties.MODEM)).getFacing() == dir) {
				return true;
			} else {
				return NetworkAPI.helpers().canConnect(world, pos, dir);
			}
		} else {
			return false;
		}
	}

	@Override
	@MergeVisitor.Stub
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return null;
	}
}
