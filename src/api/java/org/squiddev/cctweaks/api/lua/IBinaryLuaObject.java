package org.squiddev.cctweaks.api.lua;

/**
 * A marker interface for {@link dan200.computercraft.api.peripheral.IPeripheral} and
 * {@link dan200.computercraft.api.lua.ILuaObject}s. When these are marked with this interface,
 * strings are converted as byte arrays instead ({@code byte[]}), allowing safe binary conversion.
 */
public interface IBinaryLuaObject {
}
