package org.squiddev.cctweaks.core.patch;

import dan200.computercraft.shared.peripheral.PeripheralType;
import dan200.computercraft.shared.peripheral.common.BlockPeripheral;
import dan200.computercraft.shared.peripheral.common.BlockPeripheralBase;
import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;
import org.squiddev.cctweaks.core.Config;

/**
 * Lets monitors emit light
 *
 * Note. This inherits {@link BlockPeripheralBase} instead of {@link BlockPeripheral} as the
 * latter is final.
 */
public abstract class BlockPeripheral_Patch extends BlockPeripheralBase {
	@Override
	public int getLightValue(IBlockAccess world, int x, int y, int z) {
		Block block = world.getBlock(x, y, z);
		if (block != this) return block.getLightValue(world, x, y, z);

		PeripheralType type = getPeripheralType(world, x, y, z);
		if (type == PeripheralType.Monitor) return Config.Misc.monitorLight;
		if (type == PeripheralType.AdvancedMonitor) return Config.Misc.advancedMonitorLight;
		return 0;
	}
}
