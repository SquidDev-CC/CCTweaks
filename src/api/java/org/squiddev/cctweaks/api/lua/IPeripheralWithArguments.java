package org.squiddev.cctweaks.api.lua;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;

/**
 * A peripheral interface with support for being called with {@link IArguments}
 *
 * This is primarily aimed at objects that need to delegate to other objects.
 */
public interface IPeripheralWithArguments extends IPeripheral {
	/**
	 * This is called when a lua program on an attached computercraft calls peripheral.call() with
	 * one of the methods exposed by getMethodNames().
	 *
	 * Be aware that this will be called from the ComputerCraft Lua thread, and must be thread-safe
	 * when interacting with Minecraft objects.
	 *
	 * @param computer  The interface to the computercraft that is making the call.
	 * @param context   The context of the currently running lua thread.
	 * @param method    The method index to call
	 * @param arguments An instance of {@link IArguments}, representing the arguments passed into peripheral.call().
	 * @return An array of objects, representing values you wish to return to the lua program.
	 * @throws LuaException If the wrong arguments are supplied to your method.
	 * @throws InterruptedException If the computer is terminated.
	 * @see #callMethod(IComputerAccess, ILuaContext, int, Object[])
	 */
	Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, IArguments arguments) throws LuaException, InterruptedException;
}
