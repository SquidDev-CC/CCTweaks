package squiddev.cctweaks.core.asm;

import net.minecraft.launchwrapper.IClassTransformer;

public class ASMTransformer implements IClassTransformer {
	@Override
	public byte[] transform(String className, String s2, byte[] bytes) {
		if (className.equals("dan200.computercraft.core.lua.LuaJLuaMachine")) {
			return PatchComputer.PatchLuaMachine(bytes);
		} else if (className.equals("dan200.computercraft.core.computer.ComputerThread$1")) {
			return PatchComputer.PatchLuaThread(bytes);
		} else if (className.equals("dan200.computercraft.shared.turtle.core.TurtleRefuelCommand")) {
			return PatchTurtle.PatchRefuelCommand(bytes);
		} else if (className.startsWith("dan200.computercraft.shared.turtle.core.Turtle") && className.endsWith("Command")) {
			return PatchTurtle.DisableTurtleCommand(className, bytes);
		} else if (className.equals("org.luaj.vm2.luajc.JavaBuilder")) {
			return PatchComputer.PatchJavaBuilder(bytes);
		}

		return bytes;
	}
}
