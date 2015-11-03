package org.squiddev.cctweaks.core.lua;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Facing;

/**
 * Various classes for helping with Lua conversion
 */
public class LuaHelpers {
	/**
	 * Get the relative coordinates based off a relative direction and the forward direction
	 *
	 * @param direction The string direction
	 * @param facing    The "forward" direction
	 * @param coords    The coordinates to get relative to. These will be modified in place
	 * @return The coordinates
	 * @throws LuaException
	 */
	public static ChunkCoordinates getRelative(String direction, int facing, ChunkCoordinates coords) throws LuaException {
		if (direction.equals("forward")) {
			coords.posX += Facing.offsetsXForSide[facing];
			coords.posY += Facing.offsetsYForSide[facing];
			coords.posZ += Facing.offsetsZForSide[facing];
		} else if (direction.equals("up")) {
			coords.posY += 1;
		} else if (direction.equals("down")) {
			coords.posY -= 1;
		} else {
			throw new LuaException("Unknown direction " + direction + ", expected 'up', 'down' or 'forward'");
		}

		return coords;
	}

}
