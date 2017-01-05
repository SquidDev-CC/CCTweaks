package org.squiddev.cctweaks.core.patch;

import dan200.computercraft.shared.computer.core.IComputer;
import dan200.computercraft.shared.turtle.blocks.TileTurtle;
import dan200.computercraft.shared.turtle.inventory.ContainerTurtle;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.squiddev.cctweaks.api.IContainerComputer;
import org.squiddev.patcher.visitors.MergeVisitor;

import javax.annotation.Nullable;

/**
 * Implements {@link IContainerComputer} for the turtle container.
 */
public abstract class ContainerTurtle_Patch extends ContainerTurtle implements IContainerComputer {
	private IComputer computer;
	private boolean searched;

	@MergeVisitor.Stub
	public ContainerTurtle_Patch() {
		super(null, null);
	}

	@Nullable
	@Override
	public IComputer getComputer() {
		if (!searched) {
			searched = true;

			World world = m_turtle.getWorld();
			BlockPos pos = m_turtle.getPosition();

			if (world == null || pos == null) return null;

			TileEntity te = world.getTileEntity(pos);
			if (!(te instanceof TileTurtle)) return null;

			computer = ((TileTurtle) te).getComputer();
		}

		return computer;
	}
}
