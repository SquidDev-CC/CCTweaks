package org.squiddev.cctweaks.core.asm.binary;

import org.objectweb.asm.ClassVisitor;
import org.squiddev.patcher.transformer.IPatcher;

import java.util.HashSet;

public class BinaryGeneric implements IPatcher {
	private final HashSet<String> classes = new HashSet<String>();

	public BinaryGeneric() {
		classes.add("dan200.computercraft.core.apis.OSAPI");
		classes.add("dan200.computercraft.shared.peripheral.modem.ModemPeripheral");
		classes.add("dan200.computercraft.core.apis.PeripheralAPI");
	}

	@Override
	public boolean matches(String className) {
		return className.startsWith("dan200.computercraft.") && classes.contains(className);
	}

	@Override
	public ClassVisitor patch(String className, ClassVisitor delegate) throws Exception {
		/**
		 * {@link dan200.computercraft.core.apis.PeripheralAPI}:
		 * We need such a trivial patcher as the grunt work is done in {@link org.squiddev.cctweaks.core.patch.PeripheralAPI_Patch}.
		 *
		 * We do need to convert method names to strings though, and so patch the invoker
		 */
		/**
		 * {@link dan200.computercraft.core.apis.OSAPI}:
		 * Event names and labels are converted to strings, though the main arguments to {@code os.queueEvent} are not
		 * converted and so preserve encoding.
		 */

		/**
		 * {@link dan200.computercraft.shared.peripheral.modem.ModemPeripheral}:
		 * Very trivial: Just needs the binary interface
		 */
		return BinaryUtils.withStringCasts(BinaryUtils.withBinaryInterface(delegate));
	}
}
