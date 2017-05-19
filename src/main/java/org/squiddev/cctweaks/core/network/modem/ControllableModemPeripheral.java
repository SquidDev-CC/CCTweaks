package org.squiddev.cctweaks.core.network.modem;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;

import javax.annotation.Nonnull;

/**
 * A modem peripheral that can be enabled/disabled
 */
public class ControllableModemPeripheral<T extends BasicModem> extends BasicModemPeripheral<T> {
	private final int methodLength;

	public ControllableModemPeripheral(T modem) {
		super(modem);
		methodLength = super.getMethodNames().length;
	}

	@Nonnull
	@Override
	public String[] getMethodNames() {
		String[] methods = super.getMethodNames();
		String[] newMethods = new String[methods.length + 2];
		System.arraycopy(methods, 0, newMethods, 0, methods.length);

		int l = methods.length;
		newMethods[l] = "enableRemote";
		newMethods[l + 1] = "disableRemote";

		return newMethods;
	}

	@Override
	public Object[] callMethod(@Nonnull IComputerAccess computer, @Nonnull ILuaContext context, int method, @Nonnull Object[] arguments) throws LuaException, InterruptedException {
		switch (method - methodLength) {
			case 0: { // enableRemote
				if (!modem.isPeripheralEnabled()) {
					modem.setPeripheralEnabled(true);
					modem.updateEnabled();
					modem.refreshState();

					if (modem.isPeripheralEnabled()) changed = true;
				}

				return new Object[]{modem.isPeripheralEnabled()};
			}
			case 1: { // disableRemote
				if (modem.isPeripheralEnabled()) {
					modem.setPeripheralEnabled(false);
					modem.refreshState();

					if (!modem.isPeripheralEnabled()) changed = true;
				}

				return new Object[]{modem.isPeripheralEnabled()};
			}
			default:
				return super.callMethod(computer, context, method, arguments);
		}
	}
}
