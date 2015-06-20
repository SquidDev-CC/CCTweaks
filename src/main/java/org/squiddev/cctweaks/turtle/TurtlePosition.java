package org.squiddev.cctweaks.turtle;

import dan200.computercraft.api.turtle.ITurtleAccess;
import net.minecraft.world.IBlockAccess;
import org.squiddev.cctweaks.api.IWorldPosition;

/**
 * Represents a dynamic position of the turtle
 */
public class TurtlePosition implements IWorldPosition {
	public final ITurtleAccess turtle;

	public TurtlePosition(ITurtleAccess turtle) {
		this.turtle = turtle;
	}

	@Override
	public IBlockAccess getWorld() {
		return turtle.getWorld();
	}

	@Override
	public int getX() {
		return turtle.getPosition().posX;
	}

	@Override
	public int getY() {
		return turtle.getPosition().posY;
	}

	@Override
	public int getZ() {
		return turtle.getPosition().posZ;
	}
}
