package org.squiddev.cctweaks.core.turtle;

import com.google.common.collect.Maps;
import dan200.computercraft.api.lua.LuaException;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;
import java.util.Map;

/**
 * Helpers for getting the direction
 */
public class LuaDirection {
	private static final Map<String, EnumFacing> directionMapping;

	static {
		directionMapping = Maps.newHashMap();
		directionMapping.put("up", EnumFacing.DOWN);
		directionMapping.put("down", EnumFacing.UP);
		directionMapping.put("bottom", EnumFacing.DOWN);
		directionMapping.put("top", EnumFacing.UP);
		directionMapping.put("back", EnumFacing.SOUTH);
		directionMapping.put("front", EnumFacing.NORTH);
		directionMapping.put("right", EnumFacing.WEST);
		directionMapping.put("left", EnumFacing.EAST);
	}

	/**
	 * Get the relative coordinates based off a relative direction and the forward direction
	 *
	 * @param direction The string direction
	 * @param facing    The "forward" direction
	 * @param coords    The coordinates to get relative to. These will be modified in place
	 * @return The coordinates
	 * @throws LuaException If the direction is invalid.
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

	/**
	 * Parse a string as a direction
	 *
	 * @param direction This uses the Redstone names (top, bottom, front, back, etc...) for sides
	 * @return The resulting direction
	 */
	@Nullable
	public static EnumFacing getDirection(@Nonnull String direction) {
		return directionMapping.get(direction.toLowerCase(Locale.ENGLISH));
	}

	/**
	 * Orient direction around facing. No change occurs if we are looking up/down.
	 *
	 * @param direction The direction to orient
	 * @param facing    The direction we are looking
	 * @return The adjusted direction.
	 */
	@Nonnull
	public static EnumFacing orient(@Nonnull EnumFacing direction, @Nonnull EnumFacing facing) {
		switch (facing) {
			case UP:
			case DOWN:
			case NORTH:
				return direction;
			case WEST:
				return direction.rotateYCCW();
			case SOUTH:
				return direction.getOpposite();
			case EAST:
				return direction.rotateY();
			default:
				throw new RuntimeException("Illegal direction");
		}
	}
}
