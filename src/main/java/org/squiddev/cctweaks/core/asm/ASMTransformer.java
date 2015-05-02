package org.squiddev.cctweaks.core.asm;

import cpw.mods.fml.common.Loader;
import net.minecraft.launchwrapper.IClassTransformer;
import org.squiddev.cctweaks.core.asm.patch.ClassPartialPatcher;
import org.squiddev.cctweaks.core.asm.patch.ClassPatcher;
import org.squiddev.cctweaks.core.asm.patch.ClassReplacer;
import org.squiddev.cctweaks.core.asm.patch.IPatcher;
import org.squiddev.cctweaks.core.integration.multipart.MultipartIntegration;

public class ASMTransformer implements IClassTransformer {
	protected IPatcher[] patches = {
		new ClassPatcher("org.luaj.vm2.lib.DebugLib"),
		new ClassPatcher("org.luaj.vm2.lib.StringLib"),
		new ClassReplacer(
			"dan200.computercraft.shared.turtle.core.TurtleRefuelCommand",
			"org.squiddev.cctweaks.core.patch.TurtleRefuelCommand_Rewrite"
		),
		new ClassPartialPatcher(
			"dan200.computercraft.shared.peripheral.common.BlockCable",
			"org.squiddev.cctweaks.core.patch.BlockCable_Patch"
		),
		new ClassPartialPatcher(
			"dan200.computercraft.shared.peripheral.modem.TileCable",
			"org.squiddev.cctweaks.core.patch.TileCable_Patch"
		),
		new ClassPartialPatcher(
			"dan200.computercraft.shared.peripheral.common.ItemCable",
			"org.squiddev.cctweaks.core.patch.ItemCable_Patch"
		) {
			/**
			 * The item cable patch just implements multipart support.
			 * If multipart is not loaded, then we cannot patch this without errors
			 */
			@Override
			public boolean matches(String className) {
				return super.matches(className) && Loader.isModLoaded(MultipartIntegration.NAME);
			}
		},
		new ClassPartialPatcher(
			"dan200.computercraft.client.proxy.ComputerCraftProxyClient$CableBlockRenderingHandler",
			"org.squiddev.cctweaks.core.patch.CableBlockRenderingHandler_Patch"
		),
	};

	@Override
	public byte[] transform(String className, String s2, byte[] bytes) {
		if (className.equals("dan200.computercraft.core.lua.LuaJLuaMachine")) {
			return PatchComputer.patchLuaMachine(bytes);
		} else if (className.equals("dan200.computercraft.core.computer.ComputerThread$1")) {
			return PatchComputer.patchLuaThread(bytes);
		} else if (className.startsWith("dan200.computercraft.shared.turtle.core.Turtle") && className.endsWith("Command")) {
			bytes = PatchTurtle.disableTurtleCommand(className, bytes);
		}

		for (IPatcher replacer : patches) {
			if (replacer.matches(className)) {
				return replacer.patch(className, bytes);
			}
		}

		return bytes;
	}
}
