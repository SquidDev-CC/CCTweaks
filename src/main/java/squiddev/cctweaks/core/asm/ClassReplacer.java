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
public class ClassReplacer {
	protected final static String NAME_SUFFIX = "_Rewrite";

	protected final int oldNameStart;

	/**
	 * The name of the class we are replacing
	 */
	protected final String oldName;

	/**
	 * The name of the class to load
	 */
	protected final String loadedName;

	/**
	 * The name of the class to load with / instead of .
	 */
	protected final String loadedType;

	public ClassReplacer(String className) {
		oldName = className;
		oldNameStart = className.length();

		loadedName = className + NAME_SUFFIX;
		loadedType = className.replace('.', '/') + NAME_SUFFIX;
	}

	protected final Remapper mapper = new Remapper() {
		/**
		 * Map type name to the new name. Subclasses can override.
		 *
		 * @param typeName Name of the type
		 */
		@Override
		public String map(String typeName) {
			if (typeName == null) return null;

			if (typeName.contains(loadedType)) {
				return typeName.replace(NAME_SUFFIX, "");
			}

			return super.map(typeName);
		}
	};

	/**
	 * Patch the Library.
	 * This works by loading classes beginning with _Rewrite and renaming it.
	 * It also works on subclasses.
	 *
	 * @param bytes The bytes of the original class class
	 * @return Reformatted bytes
	 */
	public byte[] patchClass(String className, byte[] bytes) {
		String source = "/" + loadedType + className.substring(oldNameStart) + ".class";

		InputStream stream = ClassReplacer.class.getResourceAsStream(source);

		if (stream == null) {
			DebugLogger.error("Cannot find custom rewrite for " + className + " at " + source);
			return bytes;
		}

		try {
			ClassReader reader = new ClassReader(stream);
			ClassWriter writer = new ClassWriter(0);
			reader.accept(new RemappingClassAdapter(writer, mapper), ClassReader.EXPAND_FRAMES);

			DebugLogger.debug("Injected custom " + className);
			return writer.toByteArray();
		} catch (Exception e) {
			DebugLogger.error("Cannot replace " + className + ", falling back to default");
			e.printStackTrace();
			return bytes;
		}
	}

	public boolean matches(String className) {
		return className.startsWith(oldName);
	}
}