package org.squiddev.cctweaks.api;

import dan200.computercraft.shared.computer.core.IComputer;
import dan200.computercraft.shared.computer.core.IComputerContainer;

import javax.annotation.Nullable;

/**
 * An instance of {@link net.minecraft.inventory.Container} which provides a computer. You should implement this
 * if you provide custom computers/GUIs to interact with them.
 *
 * This seems identical to {@link IComputerContainer}, but exists to better document the class and not re-use CC
 * internal classes.
 */
public interface IContainerComputer {
	/**
	 * Get the computer you are interacting with.
	 *
	 * This will only be called on the server.
	 *
	 * @return The computer you are interacting with.
	 */
	@Nullable
	IComputer getComputer();
}
