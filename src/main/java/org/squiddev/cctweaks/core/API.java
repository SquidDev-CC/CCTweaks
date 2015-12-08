package org.squiddev.cctweaks.core;

import org.squiddev.cctweaks.api.ICCTweaksAPI;
import org.squiddev.cctweaks.api.lua.ILuaEnvironment;
import org.squiddev.cctweaks.api.network.INetworkHelpers;
import org.squiddev.cctweaks.api.network.INetworkRegistry;
import org.squiddev.cctweaks.api.peripheral.IPeripheralHelpers;
import org.squiddev.cctweaks.api.turtle.ITurtleFuelRegistry;
import org.squiddev.cctweaks.core.lua.LuaEnvironment;
import org.squiddev.cctweaks.core.network.NetworkHelpers;
import org.squiddev.cctweaks.core.network.NetworkRegistry;
import org.squiddev.cctweaks.core.peripheral.PeripheralHelpers;
import org.squiddev.cctweaks.core.turtle.TurtleFuelRegistry;

/**
 * The implementation for {@link org.squiddev.cctweaks.api.CCTweaksAPI}
 */
public final class API implements ICCTweaksAPI {
	private final INetworkRegistry networkRegistry = new NetworkRegistry();
	private final INetworkHelpers networkHelpers = new NetworkHelpers();

	private final ITurtleFuelRegistry fuelRegistry = new TurtleFuelRegistry();
	private final IPeripheralHelpers peripheralHelpers = new PeripheralHelpers();

	private final ILuaEnvironment luaEnvironment = LuaEnvironment.instance;

	@Override
	public INetworkRegistry networkRegistry() {
		return networkRegistry;
	}

	@Override
	public INetworkHelpers networkHelpers() {
		return networkHelpers;
	}

	@Override
	public ITurtleFuelRegistry fuelRegistry() {
		return fuelRegistry;
	}

	@Override
	public IPeripheralHelpers peripheralHelpers() {
		return peripheralHelpers;
	}

	@Override
	public ILuaEnvironment luaEnvironment() {
		return luaEnvironment;
	}
}
