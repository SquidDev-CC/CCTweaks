package org.squiddev.cctweaks.core.network.modem;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.peripheral.modem.INetwork;
import dan200.computercraft.shared.peripheral.modem.ModemPeripheral;
import net.minecraft.util.Vec3;
import org.squiddev.cctweaks.api.IWorldPosition;

import java.util.HashMap;
import java.util.Map;

/**
 * A peripheral for modems that provide peripherals
 *
 * @see BasicModem
 */
public class BasicModemPeripheral<T extends BasicModem> extends ModemPeripheral {
	public final T modem;

	public BasicModemPeripheral(T modem) {
		this.modem = modem;
	}

	@Override
	protected Vec3 getPosition() {
		IWorldPosition position = modem.getPosition();
		return Vec3.createVectorHelper(position.getX(), position.getY(), position.getZ());
	}

	@Override
	protected double getTransmitRange() {
		return 256;
	}

	@Override
	protected INetwork getNetwork() {
		return modem;
	}

	@Override
	public boolean equals(IPeripheral other) {
		return other instanceof BasicModemPeripheral && ((BasicModemPeripheral) other).modem.equals(this.modem);
	}

	@Override
	public String[] getMethodNames() {
		String[] methods = super.getMethodNames();
		String[] newMethods = new String[methods.length + 5];
		System.arraycopy(methods, 0, newMethods, 0, methods.length);

		int l = methods.length;
		newMethods[l] = "getNamesRemote";
		newMethods[l + 1] = "isPresentRemote";
		newMethods[l + 2] = "getTypeRemote";
		newMethods[l + 3] = "getMethodsRemote";
		newMethods[l + 4] = "callRemote";

		return newMethods;
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException {
		String[] methods = super.getMethodNames();
		switch (method - methods.length) {
			case 0: // getNamesRemote
				synchronized (modem.getPeripheralsOnNetwork()) {
					int idx = 1;
					Map<Object, Object> table = new HashMap<Object, Object>();
					for (String name : modem.peripheralWrappersByName.keySet()) {
						table.put(idx++, name);
					}
					return new Object[]{table};
				}

			case 1: { // isPresentRemote
				PeripheralAccess access = modem.peripheralWrappersByName.get(parseString(arguments, 0));
				return new Object[]{access != null && access.getType() != null};
			}
			case 2: { // getTypeRemote
				PeripheralAccess access = modem.peripheralWrappersByName.get(parseString(arguments, 0));
				String type = null;
				return access == null || (type = access.getType()) != null ? new Object[]{type} : null;
			}
			case 3: {// getMethodsRemote
				PeripheralAccess access = modem.peripheralWrappersByName.get(parseString(arguments, 0));
				String[] names;
				if (access != null && (names = access.getMethodNames()) != null) {
					Map<Object, Object> table = new HashMap<Object, Object>();
					for (int i = 0; i < names.length; i++) {
						table.put(i + 1, names[i]);
					}
					return new Object[]{table};
				}
				return null;
			}
			case 4: {
				String remoteName = parseString(arguments, 0);
				String methodName = parseString(arguments, 1);

				// Trim argument's first two values
				Object[] methodArgs = new Object[arguments.length - 2];
				System.arraycopy(arguments, 2, methodArgs, 0, arguments.length - 2);

				// Get the peripheral and call it
				PeripheralAccess access = modem.peripheralWrappersByName.get(remoteName);
				if (access == null) throw new LuaException("No peripheral: " + remoteName);
				return access.callMethod(context, methodName, methodArgs);
			}
		}

		return super.callMethod(computer, context, method, arguments);
	}

	@Override
	public synchronized void attach(IComputerAccess computer) {
		super.attach(computer);
		synchronized (modem.getPeripheralsOnNetwork()) {
			for (Map.Entry<String, IPeripheral> peripheral : modem.getPeripheralsOnNetwork().entrySet()) {
				modem.attachPeripheral(peripheral.getKey(), peripheral.getValue());
			}
		}
	}

	@Override
	public synchronized void detach(IComputerAccess computer) {
		super.detach(computer);
		synchronized (modem.getPeripheralsOnNetwork()) {
			for (String name : modem.getPeripheralsOnNetwork().keySet()) {
				modem.detachPeripheral(name);
			}
		}
	}

	public static String parseString(Object[] arguments, int index) throws LuaException {
		if ((arguments.length < index + 1) || (!(arguments[index] instanceof String))) {
			throw new LuaException("Expected string");
		}
		return (String) arguments[index];
	}
}
