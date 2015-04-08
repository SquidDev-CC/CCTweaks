package org.squiddev.cctweaks.core.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.squiddev.cctweaks.core.utils.DebugLogger;

import java.io.InputStream;

/**
 * Replaces a class named {@link #oldName} with {@link #loadedName}
 */
public class ClassReplacer implements IPatcher {
	protected final static String NAME_SUFFIX = "_Rewrite";

	protected final int oldNameStart;

	/**
	 * The name of the class we are replacing
	 */
	protected final String oldName;

	/**
	 * The name of the class we are replacing with / instead of .
	 */
	protected final String oldType;

	/**
	 * The name of the class to load
	 */
	protected final String loadedName;

	/**
	 * The name of the class to load with / instead of .
	 */
	protected final String loadedType;

	public ClassReplacer(String className, String actualName) {
		oldName = className;
		oldNameStart = className.length();
		oldType = className.replace('.', '/');

		loadedName = actualName;
		loadedType = actualName.replace('.', '/');
	}

	public ClassReplacer(String className) {
		this(className, className + NAME_SUFFIX);
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
				return typeName.replace(loadedType, oldType);
			}

			return super.map(typeName);
		}
	};

	/**
	 * Patches a class. This loads files (by default called _Rewrite) and
	 * renames all references
	 *
	 * @param className The name of the class
	 * @param bytes     The original bytes to patch
	 * @return The patched bytes
	 */
	public byte[] patch(String className, byte[] bytes) {
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

	/**
	 * Checks if the class matches
	 *
	 * @param className The name of the class
	 * @return If it should be patched
	 */
	public boolean matches(String className) {
		return className.startsWith(oldName);
	}
}
