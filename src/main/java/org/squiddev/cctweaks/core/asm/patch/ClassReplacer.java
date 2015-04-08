package org.squiddev.cctweaks.core.asm.patch;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.squiddev.cctweaks.core.utils.DebugLogger;

/**
 * Replaces a class named {@link #className} with {@link #patchName}
 */
public class ClassReplacer extends ClassRewriter {
	public final static String NAME_SUFFIX = "_Rewrite";

	public ClassReplacer(String className, String actualName) {
		super(className, actualName);
	}

	public ClassReplacer(String className) {
		this(className, className + NAME_SUFFIX);
	}


	/**
	 * Patches a class. This loads files (by default called _Rewrite) and
	 * renames all references
	 *
	 * @param className The name of the class
	 * @param bytes     The original bytes to patch
	 * @return The patched bytes
	 */
	public byte[] patch(String className, byte[] bytes) {
		try {
			ClassReader reader = getSource(patchType + className.substring(classNameStart));
			if (reader == null) return bytes;

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
}
