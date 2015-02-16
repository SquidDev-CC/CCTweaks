package squiddev.cctweaks.core.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;
import squiddev.cctweaks.core.utils.DebugLogger;

import java.io.InputStream;

/**
 * LuaJ related patches
 */
public class PatchLuaJ {
	protected static final int OLD_NAME_START = "org.luaj.vm2.lib.DebugLib".length();

	protected static final String NEW_NAME = "/org/luaj/vm2/lib/DebugLib_Rewrite";

	protected static final Remapper MAPPER = new Remapper() {
		/**
		 * Map type name to the new name. Subclasses can override.
		 *
		 * @param typeName Name of the type
		 */
		@Override
		public String map(String typeName) {
			if (typeName == null) return null;

			if (typeName.contains("DebugLib_Rewrite")) {
				return typeName.replace("_Rewrite", "");
			}

			return super.map(typeName);
		}
	};

	/**
	 * Patch the Debug Library.
	 * This works by loading {@link org.luaj.vm2.lib.DebugLib_Rewrite} and renaming it.
	 * It also works on subclasses.
	 *
	 * @param bytes The bytes of the {@link org.luaj.vm2.lib.DebugLib.DebugInfo} class
	 * @return Reformatted bytes
	 */
	public static byte[] patchDebugLib(String className, byte[] bytes) {
		String source = NEW_NAME + className.substring(OLD_NAME_START) + ".class";

		InputStream stream = PatchLuaJ.class.getResourceAsStream(source);

		if (stream == null) {
			DebugLogger.error("Cannot find custom Debug library for " + className + " at " + source);
			return bytes;
		}

		try {
			ClassReader reader = new ClassReader(stream);
			ClassWriter writer = new ClassWriter(0);
			reader.accept(new RemappingClassAdapter(writer, MAPPER), ClassReader.EXPAND_FRAMES);

			DebugLogger.debug("Injected custom " + className);
			return writer.toByteArray();
		} catch (Exception e) {
			DebugLogger.debug("Cannot replace " + className + ", falling back to default");
			e.printStackTrace();
			return bytes;
		}
	}
}
