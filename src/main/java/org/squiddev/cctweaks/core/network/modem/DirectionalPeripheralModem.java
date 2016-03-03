package org.squiddev.cctweaks.core.network.modem;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.util.PeripheralUtil;
import net.minecraft.util.Facing;
import net.minecraft.world.World;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.api.peripheral.IPeripheralHidden;
import org.squiddev.cctweaks.core.utils.Helpers;

/**
 * A modem that finds a peripheral in the side it is updating
 */
public abstract class DirectionalPeripheralModem extends SinglePeripheralModem {
	@Override
	public IPeripheral getPeripheral() {
		int dir = getDirection();

		IWorldPosition position = getPosition();
		int x = position.getX() + Facing.offsetsXForSide[dir];
		int y = position.getY() + Facing.offsetsYForSide[dir];
		int z = position.getZ() + Facing.offsetsZForSide[dir];
		IPeripheral peripheral = PeripheralUtil.getPeripheral((World) position.getWorld(), x, y, z, Facing.oppositeSide[dir]);

		if (peripheral instanceof IPeripheralHidden) {
			peripheral = ((IPeripheralHidden) peripheral).getNetworkPeripheral();
		}

		if (peripheral == null) {
			id = -1;
			peripheral = null;
		} else if (id <= -1) {
			id = Helpers.nextId((World) position.getWorld(), peripheral);
		}

		return peripheral;
	}

	public abstract int getDirection();

}
