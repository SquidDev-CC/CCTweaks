package org.squiddev.cctweaks.core.network.modem;

import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.world.World;
import org.squiddev.cctweaks.core.utils.Helpers;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A collection of peripherals that handles id assignment
 */
public abstract class PeripheralCollection {
	public final int[] ids;
	protected final int size;

	public PeripheralCollection(int size) {
		this.size = size;

		ids = new int[size];
		Arrays.fill(ids, -1);
	}

	public Map<String, IPeripheral> getConnectedPeripherals() {
		IPeripheral[] peripherals = getPeripherals();
		int[] ids = this.ids;
		int size = this.size;

		if (peripherals == null) {
			boolean changed = false;
			for (int i = 0; i < size; i++) {
				if (ids[i] != -1) {
					ids[i] = -1;
					changed = true;
				}
			}

			if (changed) changed();

			return Collections.emptyMap();
		} else if (peripherals.length != size) {
			throw new IllegalStateException("Peripherals size is incorrect: " + peripherals.length + " != " + size);
		}

		Map<String, IPeripheral> peripheralMap = new HashMap<String, IPeripheral>(size);

		boolean changed = false;
		for (int i = 0; i < size; i++) {
			IPeripheral peripheral = peripherals[i];
			if (peripheral == null) {
				if (ids[i] >= 0) changed = true;
				ids[i] = -1;
			} else {
				if (ids[i] <= -1) {
					ids[i] = Helpers.nextId(getWorld(), peripheral);
					changed = true;
				}
				peripheralMap.put(peripheral.getType() + "_" + ids[i], peripheral);
			}
		}

		if (changed) changed();

		return peripheralMap;
	}

	/**
	 * Get the list of peripherals
	 *
	 * @return The list of peripherals. This must have a constant size
	 */
	protected abstract IPeripheral[] getPeripherals();

	/**
	 * Get the current world
	 *
	 * @return The current world
	 */
	protected abstract World getWorld();

	/**
	 * Callback for when the id list changes
	 */
	protected void changed() {
	}
}
