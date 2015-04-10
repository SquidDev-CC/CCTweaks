package org.squiddev.cctweaks.core.asm.patch;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.squiddev.cctweaks.core.utils.DebugLogger;

/**
 * Replaces parts of the class with
 */
public class ClassPartialPatcher extends ClassRewriter {
	public final static String NAME_SUFFIX = "_Patch";

	public ClassPartialPatcher(String className, String patchName) {
		super(className, patchName);
	}

	public ClassPartialPatcher(String className) {
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
			ClassReader original = new ClassReader(bytes);

			ClassReader override = getSource(patchType + className.substring(classNameStart));
			if (override == null) return bytes;

			ClassWriter writer = new ClassWriter(0);
			original.accept(new Visitors.MergeVisitor(writer, override, mapper), ClassReader.EXPAND_FRAMES);

			DebugLogger.debug("Injected custom " + className);
			return writer.toByteArray();
		} catch (Exception e) {
			DebugLogger.error("Cannot replace " + className + ", falling back to default");
			e.printStackTrace();
			return bytes;
		}
	}
}
