package org.squiddev.cctweaks.core.asm.patch;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.squiddev.cctweaks.core.utils.DebugLogger;

import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ASM5;

/**
 * Strips final from methods
 */
public class StripFinal implements IPatcher {
	public final String className;

	public StripFinal(String className) {
		this.className = className;
	}

	@Override
	public byte[] patch(String className, byte[] bytes) {
		try {
			ClassWriter writer = new ClassWriter(0);
			new ClassReader(bytes).accept(new ClassVisitor(ASM5, writer) {
				@Override
				public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
					return super.visitMethod(access & ~ACC_FINAL, name, desc, signature, exceptions);
				}
			}, ClassReader.EXPAND_FRAMES);

			DebugLogger.debug(MARKER, "Removed final from " + className);
			return writer.toByteArray();
		} catch (Exception e) {
			DebugLogger.error(MARKER, "Cannot remove final from " + className + ", falling back to default", e);
			return bytes;
		}
	}

	@Override
	public boolean matches(String className) {
		return className.equals(this.className);
	}
}
