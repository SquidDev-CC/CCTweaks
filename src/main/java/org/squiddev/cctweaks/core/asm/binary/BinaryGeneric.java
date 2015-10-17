package org.squiddev.cctweaks.core.asm.binary;

import org.objectweb.asm.ClassVisitor;
import org.squiddev.patcher.transformer.IPatcher;

public class BinaryGeneric implements IPatcher {
	@Override
	public boolean matches(String className) {
		if (!className.startsWith("dan200.computercraft.")) return false;

		return className.equals("dan200.computercraft.core.apis.OSAPI") ||
			className.equals("dan200.computercraft.shared.peripheral.modem.ModemPeripheral") ||
			className.endsWith("dan200.computercraft.core.apis.PeripheralAPI");
	}

	@Override
	public ClassVisitor patch(String className, ClassVisitor delegate) throws Exception {
		/**
		 * {@link dan200.computercraft.core.apis.OSAPI}:
		 * Event names and labels are converted to strings, though the main arguments to {@code os.queueEvent} are not
		 * converted and so preserve encoding.
		 *
		 * {@link dan200.computercraft.shared.peripheral.modem.ModemPeripheral}:
		 * Very trivial: Just needs the binary interface
		 *
		 * {@link dan200.computercraft.core.apis.PeripheralAPI}:
		 * We need such a trivial patcher as the grunt work is done in
		 * {@link org.squiddev.cctweaks.core.patch.PeripheralAPI_Patch}
		 */
		return BinaryUtils.withStringCasts(BinaryUtils.withBinaryInterface(delegate));
	}
}
