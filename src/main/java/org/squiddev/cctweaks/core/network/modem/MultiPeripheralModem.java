package org.squiddev.cctweaks.core.network.modem;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.util.IDAssigner;
import dan200.computercraft.shared.util.PeripheralUtil;
import net.minecraft.util.Facing;
import net.minecraft.world.World;
import org.squiddev.cctweaks.api.IWorldPosition;

import java.io.File;
import java.util.*;

/**
 * A peripheral that checks all 6 sides to connect to
 */
public abstract class MultiPeripheralModem extends BasicModem {
	public final int[] ids;

	public MultiPeripheralModem() {
		ids = new int[]{-1, -1, -1, -1, -1, -1};
	}

	@Override
	public Map<String, IPeripheral> getConnectedPeripherals() {
		if (!peripheralEnabled) return Collections.emptyMap();

		IPeripheral[] peripherals = getPeripherals();

		if (peripherals == null) return Collections.emptyMap();

		Map<String, IPeripheral> peripheralMap = new HashMap<String, IPeripheral>(6);
		for (int i = 0; i < 6; i++) {
			IPeripheral peripheral = peripherals[i];
			if (peripheral != null) {
				peripheralMap.put(peripheral.getType() + "_" + ids[i], peripheral);
			}
		}

		return peripheralMap;
	}

	public Set<String> getPeripheralNames() {
		if (!peripheralEnabled) return null;

		IPeripheral[] peripherals = getPeripherals();
		if (peripherals == null) return null;

		Set<String> names = new HashSet<String>(6);

		for (int i = 0; i < 6; i++) {
			IPeripheral peripheral = peripherals[i];
			if (peripheral != null) {
				names.add(peripheral.getType() + "_" + ids[i]);
			}
		}

		return names;
	}

	public IPeripheral[] getPeripherals() {
		IWorldPosition position = getPosition();

		IPeripheral[] peripherals = new IPeripheral[6];

		World world = (World) position.getWorld();
		int x = position.getX(), y = position.getY(), z = position.getZ();

		for (int dir = 0; dir < 6; dir++) {
			IPeripheral peripheral = peripherals[dir] = PeripheralUtil.getPeripheral(
				world,
				x + Facing.offsetsXForSide[dir],
				y + Facing.offsetsYForSide[dir],
				z + Facing.offsetsZForSide[dir],
				Facing.oppositeSide[dir]
			);

			if (peripheral == null || (peripheral instanceof BasicModemPeripheral && ((BasicModemPeripheral) peripheral).modem instanceof MultiPeripheralModem)) {
				ids[dir] = -1;
				peripherals[dir] = null;
			} else if (ids[dir] <= -1) {
				ids[dir] = IDAssigner.getNextIDFromFile(new File(ComputerCraft.getWorldDir(world), "computer/lastid_" + peripheral.getType() + ".txt"));
			}
		}

		return peripherals;
	}

	/**
	 * Checks if the peripheral attachment has changed
	 *
	 * Simply compares IDs
	 *
	 * @return If peripherals have changed
	 */
	public boolean hasChanged() {
		int[] ids = Arrays.copyOf(this.ids, 6);

		return updateEnabled() || !Arrays.equals(ids, this.ids);
	}
}
