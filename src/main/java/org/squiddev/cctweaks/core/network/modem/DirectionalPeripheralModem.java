package org.squiddev.cctweaks.core.network.modem;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.util.PeripheralUtil;
import net.minecraft.util.EnumFacing;
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
		EnumFacing dir = getDirection();

		IWorldPosition position = getPosition();
		IPeripheral peripheral = PeripheralUtil.getPeripheral((World) position.getBlockAccess(), position.getPosition().add(dir.getDirectionVec()), dir.getOpposite());

		if (peripheral == null || peripheral instanceof IPeripheralHidden) {
			id = -1;
			peripheral = null;
		} else if (id <= -1) {
			id = Helpers.nextId((World) position.getBlockAccess(), peripheral);
		}

		return peripheral;
	}

	public abstract EnumFacing getDirection();

}
