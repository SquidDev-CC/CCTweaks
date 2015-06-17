package org.squiddev.cctweaks.api.peripheral;

import dan200.computercraft.api.peripheral.IPeripheral;

/**
 * A peripheral that cannot be accessed on a network, only
 * by a computer or turtle.
 *
 * You may want to implement this on network nodes,
 * so users do not attempt to connect to them using modems.
 */
public interface IPeripheralHidden extends IPeripheral {
}
