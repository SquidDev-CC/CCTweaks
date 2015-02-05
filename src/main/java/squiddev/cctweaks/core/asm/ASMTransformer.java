package squiddev.cctweaks.core.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.TraceClassVisitor;
import squiddev.cctweaks.core.reference.Config;
import squiddev.cctweaks.core.utils.DebugLogger;

import java.io.InputStream;
import java.io.PrintWriter;

public class ASMTransformer implements IClassTransformer {
	private static byte[] javaBuilder = null;

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
		} else if (className.equals("squiddev.cctweaks.core.asm.luaj.JavaBuilder") && Config.config.luaJC) {
			byte[] builder = getJavaBuilder();

			// If we can find the builder, and their lengths are different (because they won't really be the same)
			// then we return the new builder
			if (builder != null && builder.length != bytes.length) {
				DebugLogger.debug("Using custom squiddev.cctweaks.core.asm.luaj.JavaBuilder");
				ClassReader reader = new ClassReader(builder);
				reader.accept(new TraceClassVisitor(new PrintWriter(System.out)), ClassReader.SKIP_CODE);

				return builder;
			}
		}

		return bytes;
	}

	protected byte[] getJavaBuilder() {
		byte[] builder = javaBuilder;
		if (builder == null) {
			try {
				DebugLogger.debug("Fetching from " + getClass());
				InputStream in = getClass().getResourceAsStream("/org/luaj/vm2/luajc/JavaBuilder.class");
				javaBuilder = builder = IOUtils.toByteArray(in);
			} catch (Exception e) {
				DebugLogger.error("Cannot find custom JavaBuilder");
				e.printStackTrace();
			}
		}

		return builder;
	}
}
