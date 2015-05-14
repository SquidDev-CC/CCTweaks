package org.squiddev.cctweaks.core.peripheral;

import dan200.computercraft.api.peripheral.IPeripheral;
import org.squiddev.cctweaks.api.peripheral.IPeripheralHelpers;
import org.squiddev.cctweaks.api.peripheral.IPeripheralProxy;
import org.squiddev.cctweaks.api.peripheral.IPeripheralTargeted;

public class PeripheralHelpers implements IPeripheralHelpers {
	@Override
	public IPeripheral getBasePeripheral(IPeripheral peripheral) {
		IPeripheral previous = null;

		while (peripheral != null) {
			previous = peripheral;
			peripheral = peripheral instanceof IPeripheralProxy ?
				((IPeripheralProxy) peripheral).getBasePeripheral() :
				null;
		}

		return previous;
	}

	@Override
	public Object getTarget(IPeripheral peripheral) {
		IPeripheral previous = null;

		while (peripheral != null) {
			previous = peripheral;

			if (peripheral instanceof IPeripheralTargeted) {
				Object result = ((IPeripheralTargeted) peripheral).getTarget();
				if (result != null) return result;
			}

			peripheral = peripheral instanceof IPeripheralProxy ?
				((IPeripheralProxy) peripheral).getBasePeripheral() :
				null;
		}

		return previous;
	}
}
