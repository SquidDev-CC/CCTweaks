package org.squiddev.cctweaks.core.network.modem;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.util.PeripheralUtil;
import net.minecraft.util.Facing;
import net.minecraft.world.World;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.api.peripheral.IPeripheralHidden;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * A peripheral that checks all 6 sides to connect to
 */
public abstract class MultiPeripheralModem extends BasicModem {
	public final PeripheralCollection peripherals = new PeripheralCollection(6) {
		@Override
		protected IPeripheral[] getPeripherals() {
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

				if ((peripheral instanceof BasicModemPeripheral && ((BasicModemPeripheral) peripheral).modem instanceof MultiPeripheralModem)) {
					peripherals[dir] = null;
				} else if (peripheral instanceof IPeripheralHidden) {
					peripherals[dir] = ((IPeripheralHidden) peripheral).getNetworkPeripheral();
				}
			}

			return peripherals;
		}

		@Override
		protected World getWorld() {
			return (World) MultiPeripheralModem.this.getPosition().getWorld();
		}
	};

	@Override
	public Map<String, IPeripheral> getConnectedPeripherals() {
		if (!isPeripheralEnabled()) return Collections.emptyMap();
		return peripherals.getConnectedPeripherals();
	}

	public Set<String> getPeripheralNames() {
		return getConnectedPeripherals().keySet();
	}

	/**
	 * Checks if the peripheral attachment has changed
	 *
	 * Simply compares IDs
	 *
	 * @return If peripherals have changed
	 */
	public boolean hasChanged() {
		int[] ids = Arrays.copyOf(peripherals.ids, 6);

		return updateEnabled() || !Arrays.equals(ids, peripherals.ids);
	}
}
