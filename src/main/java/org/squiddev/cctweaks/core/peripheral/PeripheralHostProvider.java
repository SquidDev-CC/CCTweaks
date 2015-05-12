package org.squiddev.cctweaks.core.peripheral;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.squiddev.cctweaks.api.peripheral.IPeripheralHost;
import org.squiddev.cctweaks.core.registry.IModule;

/**
 * Adds support for {@link org.squiddev.cctweaks.api.peripheral.IPeripheralHost}
 */
public class PeripheralHostProvider implements IModule, IPeripheralProvider {
	@Override
	public boolean canLoad() {
		return true;
	}

	@Override
	public void preInit() {
	}

	@Override
	public void init() {
		ComputerCraftAPI.registerPeripheralProvider(this);
	}

	@Override
	public IPeripheral getPeripheral(World world, int x, int y, int z, int side) {
		TileEntity tile = world.getTileEntity(x, y, z);
		if (tile != null && tile instanceof IPeripheralHost) {
			return ((IPeripheralHost) tile).getPeripheral(side);
		}
		return null;
	}
}
