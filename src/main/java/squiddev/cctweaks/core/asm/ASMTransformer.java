package squiddev.cctweaks.core.asm;

import net.minecraft.launchwrapper.IClassTransformer;

public class ASMTransformer implements IClassTransformer {
	@Override
	public byte[] transform(String className, String s2, byte[] bytes) {
		if (className.equals("dan200.computercraft.core.lua.LuaJLuaMachine")) {
			return PatchComputer.patchLuaMachine(bytes);
		} else if (className.equals("dan200.computercraft.core.computer.ComputerThread$1")) {
			return PatchComputer.patchLuaThread(bytes);
		} else if (className.equals("dan200.computercraft.shared.turtle.core.TurtleRefuelCommand")) {
			return PatchTurtle.patchRefuelCommand(bytes);
		} else if (className.startsWith("dan200.computercraft.shared.turtle.core.Turtle") && className.endsWith("Command")) {
			return PatchTurtle.disableTurtleCommand(className, bytes);
		} else if (className.startsWith("org.luaj.vm2.lib.DebugLib")) {
			return PatchLuaJ.patchDebugLib(className, bytes);
		}

		return bytes;
	}
}
