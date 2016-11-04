package org.squiddev.cctweaks.core.patch;

import dan200.computercraft.shared.peripheral.modem.ModemPeripheral;
import net.minecraft.util.Vec3;

/**
 * To allow multi-threading modems we remove the {@code synchronized} flags from
 * several methods.
 *
 * This issue originates when transmit is called within two objects: resulting in both
 * being locked. Each modem tries to get the other modem's range and position, resulting in
 * a deadlock.
 */
public abstract class ModemPeripheral_Patch extends ModemPeripheral {
	/**
	 * Overriding this shouldn't result in crashes. There are the following implementations:
	 *
	 * - BasicModemPeripheral (CCTweaks): gets the current world position (generally a constant) and returns it's
	 * block pos.
	 * - TileAdvancedModem: gets the tile's position, world and direction. This isn't any less safe then it
	 * would be under vanilla CC.
	 * - TileCable: ignore this, we override it anyway.
	 * - TileWirelessModem: Same as AdvancedModem
	 * - TurtleModem: Just gets the turtle's position
	 * - PocketEnderModem: Gets the entity's position. This isn't thread safe but it removing synchronization
	 * shouldn't cause any more issues then it normally would
	 * - PocketModemPeripheral: Just a field access so no less safe then it would be normally.
	 *
	 * @return The modem's position in world.
	 */
	@Override
	public Vec3 getWorldPosition() {
		return getPosition();
	}

	/**
	 * Get Transmit range always returns a constant and so is safe to delegate directly.
	 *
	 * @return The receive range of this modem.
	 */
	@Override
	public double getReceiveRange() {
		return getTransmitRange();
	}
}
