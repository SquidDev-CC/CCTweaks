package org.squiddev.cctweaks.core.patch.iface;

import dan200.computercraft.shared.computer.blocks.IComputerTile;

public interface IExtendedComputerTile extends IComputerTile {
	/**
	 * Get the custom rom for this tile
	 *
	 * @return The custom ROM, or {@code -1} if none exists
	 */
	int getCustomRom();
}
