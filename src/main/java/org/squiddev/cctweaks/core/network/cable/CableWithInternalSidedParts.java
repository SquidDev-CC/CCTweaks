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

	protected boolean updateInternalConnectionMap() {
		int map = 0;
		for (EnumFacing dir : EnumFacing.VALUES) {
			if (canConnectInternally(dir)) {
				map |= 1 << dir.ordinal();
			}
		}

		if (map != internalConnMap) {
			internalConnMap = map;
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean updateConnections() {
		return updateInternalConnectionMap() | super.updateConnections();
	}
}
