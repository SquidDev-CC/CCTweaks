package org.squiddev.cctweaks.core.patch;

import dan200.computercraft.shared.computer.blocks.TileComputer;
import dan200.computercraft.shared.computer.core.IComputer;
import dan200.computercraft.shared.computer.inventory.ContainerComputer;
import org.squiddev.cctweaks.api.IContainerComputer;
import org.squiddev.patcher.visitors.MergeVisitor;

import javax.annotation.Nullable;

/**
 * Implements {@link IContainerComputer} for the computer container.
 */
public abstract class ContainerComputer_Patch extends ContainerComputer implements IContainerComputer {
	@MergeVisitor.Stub
	private TileComputer m_computer;

	@MergeVisitor.Stub
	public ContainerComputer_Patch() {
		super(null);
	}

	@Nullable
	@Override
	public IComputer getComputer() {
		return m_computer.getComputer();
	}
}
