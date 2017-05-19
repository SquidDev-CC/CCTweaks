package org.squiddev.cctweaks.api;

import org.squiddev.cctweaks.api.block.IRotationRegistry;
import org.squiddev.cctweaks.api.lua.ILuaEnvironment;
import org.squiddev.cctweaks.api.network.INetworkHelpers;
import org.squiddev.cctweaks.api.network.INetworkRegistry;
import org.squiddev.cctweaks.api.peripheral.IPeripheralHelpers;
import org.squiddev.cctweaks.api.turtle.ITurtleFuelRegistry;
import org.squiddev.cctweaks.api.turtle.ITurtleRegistry;

/**
 * A provider for the API interface
 */
public interface ICCTweaksAPI {
	INetworkRegistry networkRegistry();

	INetworkHelpers networkHelpers();

	ITurtleFuelRegistry fuelRegistry();

	ITurtleRegistry turtleRegistry();

	IPeripheralHelpers peripheralHelpers();

	ILuaEnvironment luaEnvironment();

	IRotationRegistry rotationRegistry();
}
