package org.squiddev.cctweaks.api.lua;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.ILuaTask;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;

/**
 * A Lua task task that has a method called every world tick
 *
 * @see ILuaEnvironment#issueTask(IComputerAccess, ILuaContext, ILuaTask, int)
 * @see ILuaEnvironment#executeTask(IComputerAccess, ILuaContext, ILuaTask, int)
 */
public interface IExtendedLuaTask extends ILuaTask {
	/**
	 * A method called every world tick before the task has executed.
	 *
	 * @throws LuaException If an error occurs. There will be no delay if this occurs.
	 */
	void update() throws LuaException;
}
