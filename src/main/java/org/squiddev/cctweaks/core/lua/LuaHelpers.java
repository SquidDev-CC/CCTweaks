package org.squiddev.cctweaks.core.lua;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.ILuaObject;
import dan200.computercraft.api.lua.LuaException;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import org.luaj.vm2.Varargs;
import org.objectweb.asm.ClassVisitor;
import org.squiddev.cctweaks.api.lua.ArgumentDelegator;

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
	public static BlockPos getRelative(String direction, EnumFacing facing, BlockPos coords) throws LuaException {
		if (direction.equals("forward")) {
			coords.add(facing.getDirectionVec());
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
	 * Simple method which creates varargs and delegates to the delegator. (I know how stupid that sounds).
	 *
	 * This exists so I don't have to grow the the stack size.
	 *
	 * @see org.squiddev.cctweaks.core.asm.binary.BinaryMachine#patchWrappedObject(ClassVisitor)
	 */
	public static Object[] delegateLuaObject(ILuaObject object, ILuaContext context, int method, Varargs arguments) throws LuaException, InterruptedException {
		return ArgumentDelegator.delegateLuaObject(object, context, method, new VarargArguments(arguments));
	}
}