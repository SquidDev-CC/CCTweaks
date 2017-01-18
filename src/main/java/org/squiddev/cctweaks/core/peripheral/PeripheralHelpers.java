package org.squiddev.cctweaks.core.peripheral;

import dan200.computercraft.api.peripheral.IPeripheral;
import org.squiddev.cctweaks.api.peripheral.IPeripheralHelpers;
import org.squiddev.cctweaks.api.peripheral.IPeripheralProxy;
import org.squiddev.cctweaks.api.peripheral.IPeripheralTargeted;

import javax.annotation.Nonnull;

public class PeripheralHelpers implements IPeripheralHelpers {
	@Nonnull
	@Override
	public IPeripheral getBasePeripheral(@Nonnull IPeripheral peripheral) {
		IPeripheral previous = null;

		while (peripheral != null) {
			previous = peripheral;
			peripheral = peripheral instanceof IPeripheralProxy ?
				((IPeripheralProxy) peripheral).getBasePeripheral() :
				null;
		}

		return previous;
	}

	@Nonnull
	@Override
	public Object getTarget(@Nonnull IPeripheral peripheral) {
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
