package org.squiddev.cctweaks.core.network.modem;

import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.world.World;
import org.squiddev.cctweaks.core.utils.Helpers;

import java.util.*;

/**
 * A collection of peripherals that handles id assignment
 */
public abstract class DynamicPeripheralCollection<T> {
	protected final Map<T, Integer> ids = new HashMap<T, Integer>();

	public Map<String, IPeripheral> getConnectedPeripherals() {
		Map<T, IPeripheral> peripherals = getPeripherals();
		Map<T, Integer> ids = this.ids;

		if (peripherals == null) {
			boolean changed = false;
			Iterator<Integer> items = ids.values().iterator();
			while (items.hasNext()) {
				if (items.next() >= 0) {
					items.remove();
					changed = true;
				}
			}

			if (changed) changed();

			return Collections.emptyMap();
		}

		Map<String, IPeripheral> peripheralMap = new HashMap<String, IPeripheral>(peripherals.size());

		boolean changed = false;
		Set<T> items = new HashSet<T>(ids.keySet());

		for (Map.Entry<T, IPeripheral> item : peripherals.entrySet()) {
			if (item.getValue() != null) {
				T key = item.getKey();
				IPeripheral peripheral = item.getValue();

				int id;
				if (items.remove(key)) {
					id = ids.get(key);
				} else {
					id = Helpers.nextId(getWorld(), peripheral);
					ids.put(key, id);
					changed = true;
				}

				peripheralMap.put(peripheral.getType() + "_" + id, peripheral);
			}
		}

		if (changed) changed();

		return peripheralMap;
	}

	public Collection<Integer> ids() {
		return ids.values();
	}

	/**
	 * Get the list of peripherals
	 *
	 * @return The list of peripherals. This must have a constant size
	 */
	protected abstract Map<T, IPeripheral> getPeripherals();

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
