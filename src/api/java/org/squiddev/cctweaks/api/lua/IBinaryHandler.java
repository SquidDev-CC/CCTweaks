package org.squiddev.cctweaks.api.lua;

/**
 * A marker interface for {@link dan200.computercraft.api.peripheral.IPeripheral} and
 * {@link dan200.computercraft.api.lua.ILuaObject}s. When these are marked with this interface,
 * strings are converted as byte arrays instead ({@code byte[]}), allowing safe binary conversion.
 *
 * You cannot guarantee that the arguments passed will be {@code byte[]}s though, and so must
 * be able to handle both {@link String} and {@code byte[]}.
 *
 * However, this will not convert anything passed from {@link dan200.computercraft.api.lua.ILuaContext#yield(Object[])},
 * and so try to avoid accepting binary data from that.
 *
 * You can always return {@code byte[]} instead of string from any context and it will be converted correctly.
 */
public interface IBinaryHandler {
}
