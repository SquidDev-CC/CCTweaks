package org.squiddev.cctweaks.core.patch;

import dan200.computercraft.shared.peripheral.PeripheralType;
import dan200.computercraft.shared.peripheral.common.BlockPeripheralBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.squiddev.cctweaks.core.Config;

/**
 * Lets monitors emit light
 */
public abstract class BlockPeripheral_Patch extends BlockPeripheralBase {
	@Override
	public int getLightValue(IBlockAccess world, BlockPos pos) {
		IBlockState block = world.getBlockState(pos);
		if (block.getBlock() != this) return block.getBlock().getLightValue(world, pos);

		PeripheralType type = getPeripheralType(world, pos);
		if (type == PeripheralType.Monitor) return Config.Misc.monitorLight;
		if (type == PeripheralType.AdvancedMonitor) return Config.Misc.advancedMonitorLight;
		return 0;
	}
}
