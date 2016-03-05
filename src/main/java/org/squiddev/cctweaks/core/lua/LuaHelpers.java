package org.squiddev.cctweaks.core.lua;

import com.google.common.base.Strings;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.ILuaObject;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.core.computer.Computer;
import dan200.computercraft.core.lua.ILuaMachine;
import dan200.computercraft.core.lua.LuaJLuaMachine;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import org.luaj.vm2.Varargs;
import org.objectweb.asm.ClassVisitor;
import org.squiddev.cctweaks.api.lua.ArgumentDelegator;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.core.lua.cobalt.CobaltMachine;
import org.squiddev.cctweaks.core.lua.luaj.LuaJArguments;

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
	 * Simple method which creates varargs and delegates to the delegator. (I know how stupid that sounds).
	 *
	 * This exists so I don't have to grow the the stack size.
	 *
	 * @see org.squiddev.cctweaks.core.asm.binary.BinaryMachine#patchWrappedObject(ClassVisitor)
	 */
	public static Object[] delegateLuaObject(ILuaObject object, ILuaContext context, int method, Varargs arguments) throws LuaException, InterruptedException {
		return ArgumentDelegator.delegateLuaObject(object, context, method, new LuaJArguments(arguments));
	}

	/**
	 * Wraps an exception, defaulting to another string on an empty message
	 *
	 * @param e   The exception to wrap
	 * @param def The default message
	 * @return The created exception
	 */
	public static LuaException rewriteException(Throwable e, String def) {
		String message = e.getMessage();
		return new LuaException(Strings.isNullOrEmpty(message) ? def : message);
	}

	public static ILuaMachine createMachine(Computer computer) {
		if (Config.Computer.cobalt) {
			return new CobaltMachine(computer);
		} else {
			return new LuaJLuaMachine(computer);
		}
	}
}
