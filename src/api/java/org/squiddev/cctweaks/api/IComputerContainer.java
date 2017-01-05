package org.squiddev.cctweaks.api;

import dan200.computercraft.shared.computer.core.IComputer;

import javax.annotation.Nonnull;

public interface IComputerContainer {
	@Nonnull
	IComputer getComputer();
}
