package org.squiddev.cctweaks.core.network.bridge;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;

/**
 * org.squiddev.cctweaks.core.network.bridge (CC-Tweaks
 */
public class NetworkBindingPeripheral implements IPeripheral {
	private final NetworkBindingWithModem binding;

	public NetworkBindingPeripheral(NetworkBindingWithModem binding) {
		this.binding = binding;
	}

	@Override
	public String getType() {
		return "network_binding";
	}

	@Override
	public String[] getMethodNames() {
		return new String[]{"getOpenRemote", "openRemote", "closeRemote"};
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException {
		switch (method) {
			case 0: {
				Integer id = binding.getId();
				if (id == null) {
					return new Object[]{false};
				} else {
					return new Object[]{id};
				}
			}

			case 1: {
				if (arguments.length == 0 || !(arguments[0] instanceof Number)) {
					throw new LuaException("Expected number");
				}

				if (binding.getId() != null) throw new LuaException("Already open");

				binding.setId(((Number) arguments[0]).intValue());
				binding.markDirty();
				return null;
			}

			case 2: {
				if (binding.getId() == null) throw new LuaException("Not opened");
				binding.removeId();
				binding.markDirty();
				return null;
			}
		}

		return null;
	}

	@Override
	public void attach(IComputerAccess iComputerAccess) {

	}

	@Override
	public void detach(IComputerAccess iComputerAccess) {

	}

	@Override
	public boolean equals(IPeripheral peripheral) {
		return equals((Object) peripheral);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof NetworkBindingPeripheral)) return false;

		NetworkBindingPeripheral that = (NetworkBindingPeripheral) o;

		return binding.equals(that.binding);

	}

	@Override
	public int hashCode() {
		return binding.hashCode();
	}
}
