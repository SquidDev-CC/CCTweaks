package org.squiddev.cctweaks.core;

import org.squiddev.cctweaks.api.ICCTweaksAPI;
import org.squiddev.cctweaks.api.network.INetworkRegistry;
import org.squiddev.cctweaks.api.network.INetworkVisitor;
import org.squiddev.cctweaks.api.peripheral.IPeripheralHelpers;
import org.squiddev.cctweaks.api.turtle.ITurtleFuelRegistry;
import org.squiddev.cctweaks.core.network.NetworkRegistry;
import org.squiddev.cctweaks.core.network.visitor.NetworkVisitor;
import org.squiddev.cctweaks.core.peripheral.PeripheralHelpers;
import org.squiddev.cctweaks.core.turtle.TurtleFuelRegistry;

/**
 * The implementation for {@link org.squiddev.cctweaks.api.CCTweaksAPI}
 */
public final class API implements ICCTweaksAPI {
	private final INetworkVisitor networkVisitor = new NetworkVisitor();
	private final INetworkRegistry networkRegistry = new NetworkRegistry();
	private final ITurtleFuelRegistry fuelRegistry = new TurtleFuelRegistry();
	private final IPeripheralHelpers peripheralHelpers = new PeripheralHelpers();

	@Override
	public INetworkVisitor networkVisitor() {
		return networkVisitor;
	}

	@Override
	public INetworkRegistry networkRegistry() {
		return networkRegistry;
	}

	@Override
	public ITurtleFuelRegistry fuelRegistry() {
		return fuelRegistry;
	}

	@Override
	public IPeripheralHelpers peripheralHelpers() {
		return peripheralHelpers;
	}
}
