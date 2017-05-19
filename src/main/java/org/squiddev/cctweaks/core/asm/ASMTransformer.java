package org.squiddev.cctweaks.core.asm;

import joptsimple.internal.Strings;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.ICrashCallable;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.apache.logging.log4j.Level;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.core.utils.DebugLogger;
import org.squiddev.cctweaks.integration.multipart.MultipartIntegration;
import org.squiddev.cctweaks.lua.TweaksLogger;
import org.squiddev.cctweaks.lua.asm.Tweaks;
import org.squiddev.patcher.Logger;
import org.squiddev.patcher.transformer.*;

import java.io.*;

public class ASMTransformer implements IClassTransformer {
	private final TransformationChain patches = new TransformationChain();

	private void add(Object[] patchers) {
		for (Object patcher : patchers) {
			if (patcher instanceof IPatcher) patches.add((IPatcher) patcher);
			if (patcher instanceof ISource) patches.add((ISource) patcher);
		}
	}

	public ASMTransformer() {
		// Patch the logger instance
		TweaksLogger.instance = new Logger() {
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

		/*
			TODO: Look into moving some rewrites into compile-time processing instead.
			This probably includes *_Rewrite as well as many of the binary handlers as they only exist
			because they need to stub classes that we patch anyway.
		 */
		Tweaks.setup(patches);

		add(new Object[]{
			// General stuff
			new ClassReplacer(TweaksLogger.instance,
				"dan200.computercraft.shared.turtle.core.TurtleRefuelCommand",
				"org.squiddev.cctweaks.core.patch.TurtleRefuelCommand_Rewrite"
			),
			new ClassMerger(TweaksLogger.instance,
				"dan200.computercraft.shared.turtle.core.TurtleBrain",
				"org.squiddev.cctweaks.core.patch.TurtleBrain_Patch"
			),
			new DisableTurtleCommand(),

			// Networking
			new ClassMerger(TweaksLogger.instance,
				"dan200.computercraft.shared.peripheral.common.BlockCable",
				"org.squiddev.cctweaks.core.patch.BlockCable_Patch"
			),
			new ClassMerger(TweaksLogger.instance,
				"dan200.computercraft.shared.peripheral.modem.TileCable",
				"org.squiddev.cctweaks.core.patch.TileCable_Patch"
			),
			new ClassMerger(TweaksLogger.instance,
				"dan200.computercraft.core.apis.PeripheralAPI",
				"org.squiddev.cctweaks.core.patch.PeripheralAPI_Patch"
			),
			new ClassMerger(TweaksLogger.instance,
				"dan200.computercraft.shared.peripheral.common.ItemCable",
				"org.squiddev.cctweaks.core.patch.ItemCable_Patch"
			) {
				/**
				 * The item cable patch just implements multipart support.
				 * If multipart is not loaded, then we cannot patch this without errors
				 */
				@Override
				public boolean matches(String className) {
					return super.matches(className) && Loader.isModLoaded(MultipartIntegration.MOD_NAME) && Config.Integration.mcMultipart;
				}
			},

			// Targeted peripherals
			new ClassMerger(TweaksLogger.instance,
				"dan200.computercraft.shared.computer.blocks.ComputerPeripheral",
				"org.squiddev.cctweaks.core.patch.targeted.ComputerPeripheral_Patch"
			),
			new ClassMerger(TweaksLogger.instance,
				"dan200.computercraft.shared.peripheral.diskdrive.DiskDrivePeripheral",
				"org.squiddev.cctweaks.core.patch.targeted.DiskDrivePeripheral_Patch"
			),
			new ClassMerger(TweaksLogger.instance,
				"dan200.computercraft.shared.peripheral.printer.PrinterPeripheral",
				"org.squiddev.cctweaks.core.patch.targeted.PrinterPeripheral_Patch"
			),

			// Pocket upgrades
			new PreventModemUpgrade(),
			new ClassMerger(TweaksLogger.instance,
				"dan200.computercraft.shared.pocket.items.ItemPocketComputer",
				"org.squiddev.cctweaks.core.patch.ItemPocketComputer_Patch"
			),

			// Attempt to fix concurrent modification exceptions.
			new ClassMerger(TweaksLogger.instance,
				"dan200.computercraft.shared.computer.core.ServerComputerRegistry",
				"org.squiddev.cctweaks.core.patch.ServerComputerRegistry_Patch"
			),

			// Attempt to fix computers not starting up
			new ClassMerger(TweaksLogger.instance,
				"dan200.computercraft.shared.computer.core.ServerComputer",
				"org.squiddev.cctweaks.core.patch.ServerComputer_Patch"
			),
			new ClassMerger(TweaksLogger.instance,
				"dan200.computercraft.shared.computer.blocks.TileComputerBase",
				"org.squiddev.cctweaks.core.patch.TileComputerBase_Patch"
			),

			// Allow suspending computers which time out.
			new SetSuspendable(),

			// Allow specifying direction on turtle.place methods
			new ClassMerger(TweaksLogger.instance,
				"dan200.computercraft.shared.turtle.core.TurtlePlaceCommand",
				"org.squiddev.cctweaks.core.patch.TurtlePlaceCommand_Patch"
			),

			// Implement IComputerItemFactory
			new ClassMerger(TweaksLogger.instance,
				"dan200.computercraft.shared.computer.items.ItemComputerBase",
				"org.squiddev.cctweaks.core.patch.ItemComputerBase_Patch"
			),

			// Optimisations for terminal packets
			new ClassMerger(TweaksLogger.instance,
				"dan200.computercraft.core.terminal.Terminal",
				"org.squiddev.cctweaks.core.patch.Terminal_Patch"
			),
			new TurtlePermissions(),

			// Fix JEI preventing repeat events
			new ClassMerger(TweaksLogger.instance,
				"dan200.computercraft.client.gui.GuiTurtle",
				"org.squiddev.cctweaks.core.patch.GuiContainer_Extension"
			),
			new ClassMerger(TweaksLogger.instance,
				"dan200.computercraft.client.gui.GuiComputer",
				"org.squiddev.cctweaks.core.patch.GuiContainer_Extension"
			),

			// Custom ROM booting
			new SetCustomRom(),
			new CopyRom(),
			new ClassMerger(TweaksLogger.instance,
				"dan200.computercraft.shared.computer.items.ComputerItemFactory",
				"org.squiddev.cctweaks.core.patch.ComputerItemFactory_Patch"
			),
			new ClassMerger(TweaksLogger.instance,
				"dan200.computercraft.shared.turtle.items.TurtleItemFactory",
				"org.squiddev.cctweaks.core.patch.TurtleItemFactory_Patch"
			),
		});

		patches.finalise();
	}

	private boolean loadedCC = false;
	private String[] message;

	private void checkCC() {
		loadedCC = true;
		for (ModContainer x : Loader.instance().getModList()) {
			if (x.getName().equals("ComputerCraft") && !x.getVersion().equals("${cc_version}")) {
				message = new String[]{
					"CCTweaks ${mod_version} was tested against ComputerCraft ${cc_version} but is running against " + x.getVersion() + ".",
					"Some CCTweaks/ComputerCraft features may not work correctly - please check CCTweaks for updates.",
					"If you encounter issues then try to reproduce without CCTweaks installed, then report to the appropriate mod author.",
				};
				DebugLogger.major(Level.WARN, message);

				FMLCommonHandler.instance().registerCrashCallable(new ICrashCallable() {
					@Override
					public String getLabel() {
						return "CCTweaks version issue";
					}

					@Override
					public String call() throws Exception {
						return Strings.join(message, "\n\t");
					}
				});
			}
		}
	}

	@Override
	public byte[] transform(String className, String s2, byte[] bytes) {
		if (!loadedCC && className.startsWith("dan200.computercraft.")) checkCC();

		try {
			byte[] rewritten = patches.transform(className, bytes);
			if (rewritten != bytes) writeDump(className, rewritten);
			return rewritten;
		} catch (Exception e) {
			String contents = "Cannot patch " + className + ", falling back to default";
			if (message != null) {
				DebugLogger.beginMajor(Level.ERROR);
				DebugLogger.error(contents, e);
				for (String line : message) {
					DebugLogger.error(line);
				}
				DebugLogger.endMajor(Level.ERROR);
			} else {
				DebugLogger.error(contents, e);
			}

			return bytes;
		}
	}

	public void dump(String className, byte[] bytes) {
		StringWriter writer = new StringWriter();
		new ClassReader(bytes).accept(new TraceClassVisitor(new PrintWriter(writer)), 0);
		DebugLogger.debug("Dump for " + className + "\n" + writer.toString());
	}

	private void writeDump(String className, byte[] bytes) {
		if (className.endsWith("TurtlePlaceCommand")) {
			StringWriter writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			ClassReader reader = new ClassReader(bytes);
			Exception error = null;
			try {
				CheckClassAdapter.verify(reader, getClass().getClassLoader(), false, printWriter);
			} catch (Exception e) {
				error = e;
			}

			String contents = writer.toString();
			if (error != null || contents.length() > 0) {
				DebugLogger.debug("Cannot load " + className + "\n" + contents, error);
			}
		}

		if (org.squiddev.cctweaks.lua.Config.Testing.dumpAsm) {
			File file = new File(TweaksLoadingPlugin.dump, className.replace('.', '/') + ".class");
			File directory = file.getParentFile();
			if (directory.exists() || directory.mkdirs()) {
				try {
					OutputStream stream = new FileOutputStream(file);
					try {
						stream.write(bytes);
					} catch (IOException e) {
						DebugLogger.error("Cannot write " + file, e);
					} finally {
						stream.close();
					}
				} catch (FileNotFoundException e) {
					DebugLogger.error("Cannot write " + file, e);
				} catch (IOException e) {
					DebugLogger.error("Cannot write " + file, e);
				}
			} else {
				DebugLogger.error("Cannot create folder for " + file);
			}
		}
	}
}
