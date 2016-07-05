package org.squiddev.cctweaks.blocks.network;

import org.squiddev.cctweaks.api.network.IWorldNetworkNodeHost;
import org.squiddev.cctweaks.blocks.TileBase;
import org.squiddev.cctweaks.core.network.AbstractWorldNode;
import org.squiddev.cctweaks.core.network.NetworkHelpers;

/**
 * Abstract world node host
 */
public abstract class TileNetworked extends TileBase implements IWorldNetworkNodeHost {
	@Override
	public abstract AbstractWorldNode getNode();

	@Override
	public void create() {
		super.create();
		NetworkHelpers.scheduleConnect(getNode(), this);
	}

	@Override
	public void destroy() {
		super.destroy();
		getNode().destroy();
	}
}
