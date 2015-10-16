package org.squiddev.cctweaks.core.asm.binary;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.squiddev.patcher.transformer.IPatcher;

import static org.objectweb.asm.Opcodes.*;

/**
 * File system specific patches
 */
public class BinaryFS implements IPatcher {
	public static final String READER_OBJECT = "org.squiddev.cctweaks.core.patch.binfs.ReaderObject";
	public static final String WRITER_OBJECT = "org.squiddev.cctweaks.core.patch.binfs.WriterObject";

	@Override
	public boolean matches(String className) {
		return className.equals("dan200.computercraft.core.apis.FSAPI") || className.equals("dan200.computercraft.core.filesystem.IMountedFileNormal");
	}

	@Override
	public ClassVisitor patch(final String className, final ClassVisitor delegate) throws Exception {
		if (className.equals("dan200.computercraft.core.filesystem.IMountedFileNormal")) {
			/**
			 * We need to convert the mounted file to look like
			 * {@link org.squiddev.cctweaks.core.patch.binfs.INormalFile}.
			 *
			 * This involves converting strings to byte[]s and adding a readAll() method
			 */
			return new ClassVisitor(ASM5, delegate) {
				@Override
				public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
					return super.visitMethod(access, name, desc.replace("Ljava/lang/String;", "[B"), signature, exceptions);
				}

				@Override
				public void visitEnd() {
					// Add the read all method
					super.visitMethod(ACC_PUBLIC | ACC_ABSTRACT, "readAll", "()[B", null, new String[]{"java/io/IOException"}).visitEnd();
					super.visitEnd();
				}
			};

		} else if (className.equals("dan200.computercraft.core.apis.FSAPI")) {
			/**
			 * We need to override the buffered reader/writers. We don't set this as a binary API as
			 * no binary data should be passed through it. Also there are CHECKCAST instructions as a result
			 * of generic methods.
			 */
			return new ClassVisitor(ASM5, delegate) {
				@Override
				public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
					MethodVisitor visitor = super.visitMethod(access, name, desc, signature, exceptions);
					if (name.equals("wrapBufferedWriter")) {
						writeWrapObject(WRITER_OBJECT.replace('.', '/'), visitor);
						return null;
					} else if (name.equals("wrapBufferedReader")) {
						writeWrapObject(READER_OBJECT.replace('.', '/'), visitor);
						return null;
					} else {
						return visitor;
					}
				}
			};
		} else {
			throw new RuntimeException("Unexpected class " + className);
		}
	}

	private static void writeWrapObject(String name, MethodVisitor visitor) {
		visitor.visitCode();
		// Create array of length 1 and duplicate it
		visitor.visitInsn(ICONST_1);
		visitor.visitTypeInsn(ANEWARRAY, "java/lang/Object");
		visitor.visitInsn(DUP);
		visitor.visitInsn(ICONST_0); // We will store it to position 0
		// Create a reader
		visitor.visitTypeInsn(NEW, name);
		visitor.visitInsn(DUP);
		// Invoke it with the stream
		visitor.visitVarInsn(ALOAD, 0);
		visitor.visitMethodInsn(INVOKESPECIAL, name, "<init>", "(Ldan200/computercraft/core/filesystem/IMountedFileNormal;)V", false);
		visitor.visitInsn(AASTORE); // Save it to index 0
		visitor.visitInsn(ARETURN); // Return array
		visitor.visitMaxs(6, 1);
		visitor.visitEnd();
	}

}
