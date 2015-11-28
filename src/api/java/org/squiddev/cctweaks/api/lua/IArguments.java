package org.squiddev.cctweaks.api.lua;

import dan200.computercraft.api.lua.LuaException;

/**
 * Custom wrapper for arguments, allowing more advanced handling
 */
public interface IArguments {
	/**
	 * Number of arguments passed
	 *
	 * @return The number of arguments passed
	 */
	int size();

	/**
	 * Attempt to read a argument as a number.
	 *
	 * This will attempt to do string to number conversion.
	 *
	 * @param index The argument index, starting at 0
	 * @return The number that was read
	 * @throws LuaException In the form {@code "Expected number"}, when the argument is not a number.
	 */
	double getNumber(int index) throws LuaException;

	/**
	 * Attempt to read a argument as a boolean.
	 *
	 * @param index The argument index, starting at 0
	 * @return The boolean that was read
	 * @throws LuaException In the form {@code "Expected boolean"}, when the argument is not a boolean.
	 */
	boolean getBoolean(int index) throws LuaException;

	/**
	 * Attempt to read a argument as a string.
	 *
	 * @param index The argument index, starting at 0
	 * @return The string that was read
	 * @throws LuaException In the form {@code "Expected string"}, when the argument is not a string.
	 */
	String getString(int index) throws LuaException;

	/**
	 * Attempt to read a argument as a string in byte form.
	 *
	 * @param index The argument index, starting at 0
	 * @return The string that was read
	 * @throws LuaException In the form {@code "Expected string"}, when the argument is not a string.
	 */
	byte[] getStringBytes(int index) throws LuaException;

	/**
	 * Attempt to read an argument
	 *
	 * @param index The argument index
	 * @return The argument or {@code null} if not set
	 */
	Object getArgumentBinary(int index);

	/**
	 * Attempt to read an argument
	 *
	 * @param index The argument index
	 * @return The argument or {@code null} if not set
	 */
	Object getArgument(int index);

	/**
	 * Attempt to get the arguments in normal form, suitable for {@link dan200.computercraft.api.peripheral.IPeripheral} or {@link dan200.computercraft.api.lua.ILuaObject}
	 *
	 * @return Array of arguments
	 */
	Object[] asArguments();

	/**
	 * Attempt to get the arguments in binary form, suitable for {@link IBinaryHandler}.
	 *
	 * @return Array of arguments
	 */
	Object[] asBinary();

	/**
	 * Get a subset of the current arguments
	 *
	 * @param offset The offset to start from, 0 would be identical to the current instance
	 * @return The subargs
	 */
	IArguments subArgs(int offset);
}
