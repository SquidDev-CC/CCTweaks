package squiddev.cctweaks.core.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import squiddev.cctweaks.core.reference.Config;

public class ASMTransformer implements IClassTransformer {
	protected IPatcher[] patches = {
		new ClassPatcher("org.luaj.vm2.lib.DebugLib"),
		new ClassPatcher("org.luaj.vm2.lib.StringLib"),
		new ClassPatcher("org.luaj.vm2.LuaString") {
			@Override
			public boolean matches(String className) {
				return super.matches(className) && Config.config.patchString;
			}
		},
		new ClassReplacer(
			"dan200.computercraft.shared.turtle.core.TurtleRefuelCommand",
			"squiddev.cctweaks.core.turtle.TurtleRefuelCommand_Rewrite"
		),
	};

	@Override
	public byte[] transform(String className, String s2, byte[] bytes) {
		if (className.equals("dan200.computercraft.core.lua.LuaJLuaMachine")) {
			return PatchComputer.patchLuaMachine(bytes);
		} else if (className.equals("dan200.computercraft.core.computer.ComputerThread$1")) {
			return PatchComputer.patchLuaThread(bytes);
		} else if (className.startsWith("dan200.computercraft.shared.turtle.core.Turtle") && className.endsWith("Command")) {
			return PatchTurtle.disableTurtleCommand(className, bytes);
		}

		for (IPatcher replacer : patches) {
			if (replacer.matches(className)) {
				return replacer.patch(className, bytes);
			}
		}

		return bytes;
	}
}
