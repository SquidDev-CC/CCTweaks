package org.squiddev.cctweaks.core.peripheral;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import org.squiddev.cctweaks.api.CCTweaksAPI;
import org.squiddev.cctweaks.api.peripheral.IPeripheralHelpers;
import org.squiddev.cctweaks.api.peripheral.IPeripheralProxy;

import java.util.HashSet;
import java.util.Set;

/**
 * An simple peripheral proxy instance.
 */
public abstract class PeripheralProxy implements IPeripheral, IPeripheralProxy {
	protected IPeripheral instance;
	protected final Set<IComputerAccess> mounts = new HashSet<IComputerAccess>();

	protected String defaultType = null;

	public PeripheralProxy() {
	}

	public PeripheralProxy(String defaultType) {
		this.defaultType = defaultType;
	}

	protected abstract IPeripheral createPeripheral();

	@Override
	public IPeripheral getBasePeripheral() {
		IPeripheral instance = this.instance;
		if (instance == null) {
			this.instance = instance = createPeripheral();
			for (IComputerAccess mount : mounts) {
				instance.attach(mount);
			}
		}

		return instance;
	}

	@Override
	public String getType() {
		return instance == null && defaultType != null ? defaultType : getBasePeripheral().getType();
	}

	@Override
	public String[] getMethodNames() {
		return getBasePeripheral().getMethodNames();
	}

	@Override
	public Object[] callMethod(IComputerAccess access, ILuaContext context, int i, Object[] objects) throws LuaException, InterruptedException {
		return getBasePeripheral().callMethod(access, context, i, objects);
	}

	@Override
	public void attach(IComputerAccess access) {
		if (instance == null) {
			// We want to be as lazy as possible with loading
			mounts.add(access);
		} else {
			getBasePeripheral().attach(access);
		}
	}

	@Override
	public void detach(IComputerAccess access) {
		mounts.remove(access);
		if (instance != null) {
			getBasePeripheral().detach(access);
		}
	}

	@Override
	public boolean equals(IPeripheral other) {
		IPeripheralHelpers helpers = CCTweaksAPI.instance().peripheralHelpers();
		return helpers.getBasePeripheral(this).equals(helpers.getBasePeripheral(other));
	}
}
