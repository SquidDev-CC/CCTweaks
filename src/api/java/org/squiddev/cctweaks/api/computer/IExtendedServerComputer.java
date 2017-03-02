package org.squiddev.cctweaks.api.computer;

import dan200.computercraft.shared.computer.core.IComputer;
import dan200.computercraft.shared.computer.core.ServerComputer;

/**
 * Various extension methods to {@link ServerComputer}.
 *
 * You should not implement this interface yourself. Instead, cast {@link ServerComputer} to this and use the resulting
 * object.
 */
public interface IExtendedServerComputer extends IComputer {
	/**
	 * Set the custom ROM file to be the specified disk
	 *
	 * @param diskId The disk to use as a custom ROM.
	 * @see ICustomRomItem
	 */
	void setCustomRom(int diskId);

	/**
	 * Determine whether a computer is mostly on or not
	 *
	 * @return Whether this computer is on, starting up or about has a shutdown queued but is not shutdown.
	 */
	boolean isMostlyOn();
}
