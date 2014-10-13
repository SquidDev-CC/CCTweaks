package squiddev.cctweaks.asm;

import net.minecraft.launchwrapper.IClassTransformer;

import java.util.HashSet;

public class ASMTransformer implements IClassTransformer {
	HashSet<String> classes;

	public ASMTransformer() {
		classes = new HashSet<String>();
		classes.add("dan200.computercraft.core.lua.LuaJLuaMachine");
	}
	@Override
	public byte[] transform(String className, String s2, byte[] bytes) {
		if(className.equals("dan200.computercraft.core.lua.LuaJLuaMachine")) {
			return PatchComputer.PatchLuaMachine(bytes);
		}

		return bytes;
	}
}
