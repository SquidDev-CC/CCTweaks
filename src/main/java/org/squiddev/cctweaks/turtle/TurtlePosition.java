package org.squiddev.cctweaks.turtle;

import dan200.computercraft.api.turtle.ITurtleAccess;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.squiddev.cctweaks.api.IWorldPosition;

import javax.annotation.Nonnull;

/**
 * Represents a dynamic position of the turtle
 */
public class TurtlePosition implements IWorldPosition {
	public final ITurtleAccess turtle;

	public TurtlePosition(ITurtleAccess turtle) {
		this.turtle = turtle;
	}

	@Nonnull
	@Override
	public IBlockAccess getBlockAccess() {
		return turtle.getWorld();
	}

	@Nonnull
	@Override
	public BlockPos getPosition() {
		return turtle.getPosition();
	}
}
