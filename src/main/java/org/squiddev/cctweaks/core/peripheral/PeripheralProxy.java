package org.squiddev.cctweaks.core.peripheral;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import org.squiddev.cctweaks.api.CCTweaksAPI;
import org.squiddev.cctweaks.api.peripheral.IPeripheralHelpers;
import org.squiddev.cctweaks.api.peripheral.IPeripheralProxy;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

/**
 * An simple peripheral proxy instance.
 */
public abstract class PeripheralProxy implements IPeripheral, IPeripheralProxy {
	private IPeripheral instance;
	private final Set<IComputerAccess> mounts = new HashSet<IComputerAccess>();

	private String defaultType = null;

	public PeripheralProxy() {
	}

	public PeripheralProxy(String defaultType) {
		this.defaultType = defaultType;
	}

	@Nonnull
	protected abstract IPeripheral createPeripheral();

	@Nonnull
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

	@Nonnull
	@Override
	public String getType() {
		return instance == null && defaultType != null ? defaultType : getBasePeripheral().getType();
	}

	@Nonnull
	@Override
	public String[] getMethodNames() {
		return getBasePeripheral().getMethodNames();
	}

	@Override
	public Object[] callMethod(@Nonnull IComputerAccess access, @Nonnull ILuaContext context, int i, @Nonnull Object[] objects) throws LuaException, InterruptedException {
		return getBasePeripheral().callMethod(access, context, i, objects);
	}

	@Override
	public void attach(@Nonnull IComputerAccess access) {
		if (instance == null) {
			// We want to be as lazy as possible with loading
			mounts.add(access);
		} else {
			getBasePeripheral().attach(access);
		}
	}

	@Override
	public void detach(@Nonnull IComputerAccess access) {
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
