package org.squiddev.cctweaks.blocks.network;

import org.squiddev.cctweaks.api.network.IWorldNetworkNodeHost;
import org.squiddev.cctweaks.blocks.TileBase;
import org.squiddev.cctweaks.core.FmlEvents;
import org.squiddev.cctweaks.core.network.AbstractWorldNode;

/**
 * Abstract world node host
 */
public abstract class TileNetworked extends TileBase implements IWorldNetworkNodeHost {
	@Override
	public abstract AbstractWorldNode getNode();

	@Override
	public void create() {
		super.create();
		FmlEvents.schedule(new Runnable() {
			@Override
			public void run() {
				getNode().connect();
			}
		});
	}

	@Override
	public void destroy() {
		super.destroy();
		getNode().destroy();
	}
}
