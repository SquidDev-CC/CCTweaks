package org.squiddev.cctweaks.api;

import org.squiddev.cctweaks.api.lua.ILuaEnvironment;
import org.squiddev.cctweaks.api.network.INetworkHelpers;
import org.squiddev.cctweaks.api.network.INetworkRegistry;
import org.squiddev.cctweaks.api.peripheral.IPeripheralHelpers;
import org.squiddev.cctweaks.api.turtle.ITurtleFuelRegistry;

/**
 * A provider for the API interface
 */
public interface ICCTweaksAPI {
	INetworkRegistry networkRegistry();

	INetworkHelpers networkHelpers();

	ITurtleFuelRegistry fuelRegistry();

	IPeripheralHelpers peripheralHelpers();

	ILuaEnvironment luaEnvironment();
}
