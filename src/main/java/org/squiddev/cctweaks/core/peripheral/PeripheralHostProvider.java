package org.squiddev.cctweaks.core.peripheral;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.squiddev.cctweaks.api.peripheral.IPeripheralHost;
import org.squiddev.cctweaks.core.registry.Module;

import javax.annotation.Nonnull;

/**
 * Adds support for {@link org.squiddev.cctweaks.api.peripheral.IPeripheralHost}
 */
public class PeripheralHostProvider extends Module implements IPeripheralProvider {
	@Override
	public void init() {
		ComputerCraftAPI.registerPeripheralProvider(this);
	}

	@Override
	public IPeripheral getPeripheral(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumFacing side) {
		TileEntity tile = world.getTileEntity(pos);
		if (tile != null && tile instanceof IPeripheralHost) {
			return ((IPeripheralHost) tile).getPeripheral(side);
		}
		return null;
	}
}
