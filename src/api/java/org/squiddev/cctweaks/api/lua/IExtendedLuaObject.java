package org.squiddev.cctweaks.api.lua;

import dan200.computercraft.api.lua.ILuaObject;

import java.util.Map;

/**
 * Extended functionality for Lua objects and APIs
 */
public interface IExtendedLuaObject extends ILuaObject {
	/**
	 * Get additional fields to add to this API. These will be converted as normal.
	 *
	 * @return Additional fields. Do not return {@code null}
	 */
	Map<Object, Object> getAdditionalData();
}
