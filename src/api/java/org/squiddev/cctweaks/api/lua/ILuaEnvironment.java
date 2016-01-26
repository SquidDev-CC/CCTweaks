package org.squiddev.cctweaks.api.lua;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.ILuaTask;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;

/**
 * Various hooks and methods for interfacing with the Lua environment
 */
public interface ILuaEnvironment {
	String EVENT_NAME = "cctweaks_task";

	/**
	 * Register a custom API
	 *
	 * @param factory The API factory to register
	 */
	void registerAPI(ILuaAPIFactory factory);

	/**
	 * Issue a task to be run on the main thread.
	 *
	 * If the task is an instance of {@link IExtendedLuaTask} then the {@link IExtendedLuaTask#update()} method
	 * will be called every tick.
	 *
	 * Like {@link ILuaContext#issueMainThreadTask(ILuaTask)} this is rate limited to 1000 tasks per tick, so use
	 * this method with care.
	 *
	 * @param access The computer access object
	 * @param task   The task to run
	 * @param delay  Time in ticks to wait before running
	 * @return The task ID. You can pull an event with name {@link #EVENT_NAME} to wait for this task.
	 * @throws LuaException When there are too many tasks (>50000).
	 */
	long issueTask(IComputerAccess access, ILuaTask task, int delay) throws LuaException;

	/**
	 * Issue a task to be run on the main thread and wait for its completion
	 *
	 * If the task is an instance of {@link IExtendedLuaTask} then the {@link IExtendedLuaTask#update()} method
	 * will be called every tick.
	 *
	 * Like {@link ILuaContext#executeMainThreadTask(ILuaTask)} this is rate limited to 1000 tasks per tick, so use
	 * this method with care.
	 *
	 * @param access  The computer access object
	 * @param context The current lua context
	 * @param task    The task to run
	 * @param delay   Time in ticks to wait before running
	 * @return The return values of this task.
	 * @throws LuaException         When there are too many tasks (>50000) on when terminated
	 * @throws InterruptedException When terminated
	 */
	Object[] executeTask(IComputerAccess access, ILuaContext context, ILuaTask task, int delay) throws LuaException, InterruptedException;

	/**
	 * Sleep the Lua thread for a duration
	 *
	 * @param access  The computer access object
	 * @param context The current lua context
	 * @param delay   Time in ticks to wait before running
	 * @throws LuaException         When there are too many tasks (>50000) on when terminated
	 * @throws InterruptedException When terminated
	 */
	void sleep(IComputerAccess access, ILuaContext context, int delay) throws LuaException, InterruptedException;
}
