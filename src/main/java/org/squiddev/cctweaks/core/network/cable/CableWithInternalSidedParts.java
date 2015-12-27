package org.squiddev.cctweaks.core.network.cable;

import net.minecraft.util.EnumFacing;

public abstract class CableWithInternalSidedParts extends BasicCable {
	private int internalConnMap;

	public abstract boolean canConnectInternally(EnumFacing direction);

	public boolean doesConnectInternally(EnumFacing direction) {
		int flag = 1 << direction.ordinal();
		return (internalConnMap & flag) == flag;
	}

	public boolean doesConnectVisually(EnumFacing direction) {
		return doesConnect(direction) || doesConnectInternally(direction);
	}

	protected void updateInternalConnectionMap() {
		internalConnMap = 0;
		for (EnumFacing dir : EnumFacing.VALUES) {
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
