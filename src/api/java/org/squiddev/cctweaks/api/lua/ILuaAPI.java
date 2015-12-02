package org.squiddev.cctweaks.api.lua;

import dan200.computercraft.api.lua.ILuaObject;

/**
 * A custom API to inject into a computer.
 */
public interface ILuaAPI extends ILuaObject {
	/**
	 * Called when the computer is turned on
	 */
	void startup();

	/**
	 * Called when the computer is shutdown
	 */
	void shutdown();

	/**
	 * A method called every tick
	 *
	 * @param timestep The time since last tick in seconds
	 */
	void advance(double timestep);
}
