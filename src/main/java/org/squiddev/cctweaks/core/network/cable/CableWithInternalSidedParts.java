package org.squiddev.cctweaks.core.network.cable;

import net.minecraftforge.common.util.ForgeDirection;

public abstract class CableWithInternalSidedParts extends BasicCable {
	private int internalConnMap;

	public abstract boolean canConnectInternally(ForgeDirection direction);

	public boolean doesConnectInternally(ForgeDirection direction) {
		int flag = 1 << direction.ordinal();
		return (internalConnMap & flag) == flag;
	}

	public boolean doesConnectVisually(ForgeDirection direction) {
		return doesConnect(direction) || doesConnectInternally(direction);
	}

	protected void updateInternalConnectionMap() {
		internalConnMap = 0;
		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			if (canConnectInternally(dir)) {
				internalConnMap |= 1 << dir.ordinal();
			}
		}
	}

	@Override
	public boolean updateConnections() {
		int internal = internalConnMap;
		updateInternalConnectionMap();
		return super.updateConnections() || internal != internalConnMap;
	}
}
