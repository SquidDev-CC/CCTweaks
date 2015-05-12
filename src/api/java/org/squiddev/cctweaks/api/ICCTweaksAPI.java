package org.squiddev.cctweaks.api;

import org.squiddev.cctweaks.api.network.INetworkRegistry;
import org.squiddev.cctweaks.api.network.INetworkVisitor;
import org.squiddev.cctweaks.api.turtle.ITurtleFuelRegistry;

/**
 * A provider for the API interface
 */
public interface ICCTweaksAPI {
	INetworkVisitor networkVisitor();

	INetworkRegistry networkRegistry();

	ITurtleFuelRegistry fuelRegistry();
}
