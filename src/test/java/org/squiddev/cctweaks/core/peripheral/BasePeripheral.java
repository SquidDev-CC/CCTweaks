package org.squiddev.cctweaks.core.peripheral;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;

/**
 * A basic peripheral that does nothing
 */
public class BasePeripheral implements IPeripheral {
	@Override
	public String getType() {
		return null;
	}

	@Override
	public String[] getMethodNames() {
		return null;
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int index, Object[] args) throws LuaException, InterruptedException {
		return null;
	}

	@Override
	public void attach(IComputerAccess computer) {
	}

	@Override
	public void detach(IComputerAccess computer) {
	}

	@Override
	public boolean equals(IPeripheral peripheral) {
		return peripheral == this;
	}
}
