package org.squiddev.cctweaks.api.lua;

import dan200.computercraft.api.peripheral.IComputerAccess;

/**
 * Create an instance of {@link ILuaAPI} for a given computer
 */
public interface ILuaAPIFactory {
	/**
	 * Create the API
	 *
	 * @param computer The computer to create for
	 * @return The created API.
	 */
	ILuaAPI create(IComputerAccess computer);

	/**
	 * Globals to export this API as
	 *
	 * @return The API to export under
	 */
	String[] getNames();
}
