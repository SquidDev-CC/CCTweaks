package org.squiddev.cctweaks.core.asm;

import cpw.mods.fml.common.Loader;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.TraceClassVisitor;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.core.utils.DebugLogger;
import org.squiddev.cctweaks.integration.multipart.MultipartIntegration;
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
		add(new Object[]{
			new ClassReplaceSource("org.luaj.vm2.lib.DebugLib"),
			new ClassReplaceSource("org.luaj.vm2.lib.StringLib"),
			new ClassReplacer(
				"dan200.computercraft.shared.turtle.core.TurtleRefuelCommand",
				"org.squiddev.cctweaks.core.patch.TurtleRefuelCommand_Rewrite"
			),
			new ClassMerger(
				"dan200.computercraft.shared.peripheral.common.BlockCable",
				"org.squiddev.cctweaks.core.patch.BlockCable_Patch"
			),
			new ClassMerger(
				"dan200.computercraft.shared.peripheral.modem.TileCable",
				"org.squiddev.cctweaks.core.patch.TileCable_Patch"
			),
			new ClassMerger(
				"dan200.computercraft.shared.peripheral.common.ItemCable",
				"org.squiddev.cctweaks.core.patch.ItemCable_Patch"
			) {
				/**
				 * The item cable patch just implements multipart support.
				 * If multipart is not loaded, then we cannot patch this without errors
				 */
				@Override
				public boolean matches(String className) {
					return super.matches(className) && Loader.isModLoaded(MultipartIntegration.NAME) && Config.Integration.cbMultipart;
				}
			},
			new ClassMerger(
				"dan200.computercraft.client.proxy.ComputerCraftProxyClient$CableBlockRenderingHandler",
				"org.squiddev.cctweaks.core.patch.CableBlockRenderingHandler_Patch"
			),
			new ClassMerger(
				"dan200.computercraft.core.apis.PeripheralAPI",
				"org.squiddev.cctweaks.core.patch.PeripheralAPI_Patch"
			),
			new ClassMerger(
				"openperipheral.addons.peripheralproxy.WrappedPeripheral",
				"org.squiddev.cctweaks.core.patch.op.PeripheralProxy_Patch"
			),
			new PatchOpenPeripheralAdapter(),
			new PatchOpenModule(),
			new DisableTurtleCommand(),
			new CustomTimeout(),
			new InjectLuaJC(),
			new WhitelistGlobals(),
			new PatchTurtleRenderer(),
		});

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
