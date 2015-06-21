package org.squiddev.cctweaks.core.network.modem;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.util.IDAssigner;
import dan200.computercraft.shared.util.PeripheralUtil;
import net.minecraft.util.Facing;
import net.minecraft.world.World;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.api.peripheral.IPeripheralHidden;

import java.io.File;

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

		if (peripheral == null || peripheral instanceof IPeripheralHidden) {
			id = -1;
			peripheral = null;
		} else if (id <= -1) {
			id = IDAssigner.getNextIDFromFile(new File(ComputerCraft.getWorldDir((World) position.getWorld()), "computer/lastid_" + peripheral.getType() + ".txt"));
		}

		return peripheral;
	}

	public abstract int getDirection();

}
