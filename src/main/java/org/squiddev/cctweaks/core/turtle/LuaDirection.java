package org.squiddev.cctweaks.core.turtle;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;

/**
 * Helpers for getting the direction
 */
public class LuaDirection {
	/**
	 * Get the relative coordinates based off a relative direction and the forward direction
	 *
	 * @param direction The string direction
	 * @param facing    The "forward" direction
	 * @param coords    The coordinates to get relative to. These will be modified in place
	 * @return The coordinates
	 * @throws LuaException
	 */
	public static BlockPos getRelative(String direction, EnumFacing facing, BlockPos coords) throws LuaException {
		if (direction.equals("forward")) {
			coords.offset(facing);
		} else if (direction.equals("up")) {
			return coords.add(0, 1, 0);
		} else if (direction.equals("down")) {
			return coords.add(0, -1, 0);
		} else {
			throw new LuaException("Unknown direction " + direction + ", expected 'up', 'down' or 'forward'");
		}

		return coords;
	}
}
