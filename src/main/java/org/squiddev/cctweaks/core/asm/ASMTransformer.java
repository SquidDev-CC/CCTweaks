package org.squiddev.cctweaks.core.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.TraceClassVisitor;
import org.squiddev.cctweaks.core.asm.binary.BinaryUtils;
import org.squiddev.cctweaks.core.utils.DebugLogger;
import org.squiddev.patcher.Logger;
import org.squiddev.patcher.transformer.*;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ASMTransformer implements IClassTransformer {
	protected final TransformationChain patches = new TransformationChain();

	protected void add(Object[] patchers) {
		for (Object patcher : patchers) {
			if (patcher instanceof IPatcher) patches.add((IPatcher) patcher);
			if (patcher instanceof ISource) patches.add((ISource) patcher);
		}
	}

	public ASMTransformer() {
		/*
			TODO: Look into moving some rewrites into compile-time processing instead.
			This probably includes *_Rewrite as well as many of the binary handlers as only exist
			because they need to stub classes that we patch anyway.
		 */

		add(new Object[]{
			// General stuff
			new ClassReplaceSource("org.luaj.vm2.lib.DebugLib"),
			new ClassReplaceSource("org.luaj.vm2.lib.StringLib"),
			new ClassReplacer(
				"dan200.computercraft.shared.turtle.core.TurtleRefuelCommand",
				"org.squiddev.cctweaks.core.patch.TurtleRefuelCommand_Rewrite"
			),
			new ClassMerger(
				"dan200.computercraft.shared.peripheral.common.BlockPeripheral",
				"org.squiddev.cctweaks.core.patch.BlockPeripheral_Patch"
			),
			new ClassMerger(
				"dan200.computercraft.shared.turtle.core.TurtleBrain",
				"org.squiddev.cctweaks.core.patch.TurtleBrain_Patch"
			),
			new DisableTurtleCommand(),
			new CustomTimeout(),
			new InjectLuaJC(),
			new WhitelistGlobals(),
			new PatchTurtleRenderer(),
			new CustomAPIs(),

			// Networking
			new ClassMerger(
				"dan200.computercraft.shared.peripheral.common.BlockCable",
				"org.squiddev.cctweaks.core.patch.BlockCable_Patch"
			),
			new ClassMerger(
				"dan200.computercraft.shared.peripheral.modem.TileCable",
				"org.squiddev.cctweaks.core.patch.TileCable_Patch"
			),
			new ClassMerger(
				"dan200.computercraft.core.apis.PeripheralAPI",
				"org.squiddev.cctweaks.core.patch.PeripheralAPI_Patch"
			),

			// Open peripheral
			new ClassMerger(
				"openperipheral.addons.peripheralproxy.WrappedPeripheral",
				"org.squiddev.cctweaks.core.patch.op.PeripheralProxy_Patch"
			),
			new PatchOpenPeripheralAdapter(),
			new PatchOpenModule(),

			// Targeted peripherals
			new ClassMerger(
				"dan200.computercraft.shared.computer.blocks.ComputerPeripheral",
				"org.squiddev.cctweaks.core.patch.targeted.ComputerPeripheral_Patch"
			),
			new ClassMerger(
				"dan200.computercraft.shared.peripheral.diskdrive.DiskDrivePeripheral",
				"org.squiddev.cctweaks.core.patch.targeted.DiskDrivePeripheral_Patch"
			),
			new ClassMerger(
				"dan200.computercraft.shared.peripheral.printer.PrinterPeripheral",
				"org.squiddev.cctweaks.core.patch.targeted.PrinterPeripheral_Patch"
			),
		});
		BinaryUtils.inject(patches);

		patches.finalise();

		// Patch the logger instance
		Logger.instance = new Logger() {
			@Override
			public void doDebug(String message) {
				DebugLogger.debug(message);
			}

			@Override
			public void doError(String message, Throwable e) {
				DebugLogger.error(message, e);
			}

			@Override
			public void doWarn(String message) {
				DebugLogger.warn(message);
			}
		};
	}

	@Override
	public byte[] transform(String className, String s2, byte[] bytes) {
		try {
			return patches.transform(className, bytes);
		} catch (Exception e) {
			DebugLogger.error("Cannot patch " + className + ", falling back to default", e);
			return bytes;
		}
	}

	public void dump(String className, byte[] bytes) {
		StringWriter writer = new StringWriter();
		new ClassReader(bytes).accept(new TraceClassVisitor(new PrintWriter(writer)), 0);
		DebugLogger.debug("Dump for " + className + "\n" + writer.toString());
	}
}
