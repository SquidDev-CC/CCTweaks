package squiddev.cctweaks.core.asm;

import net.minecraft.launchwrapper.IClassTransformer;

public class ASMTransformer implements IClassTransformer {
	protected ClassReplacer[] patches = {
		new ClassReplacer("org.luaj.vm2.lib.DebugLib"),
		new ClassReplacer("org.luaj.vm2.lib.StringLib"),
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

		for (ClassReplacer replacer : patches) {
			if (replacer.matches(className)) {
				return replacer.patchClass(className, bytes);
			}
		}

		return bytes;
	}
}
