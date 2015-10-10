package org.squiddev.cctweaks.core.asm.binary;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.TypeInsnNode;
import org.squiddev.patcher.transformer.IPatcher;
import org.squiddev.patcher.visitors.FindingVisitor;

import static org.objectweb.asm.Opcodes.*;

public class BinaryOS implements IPatcher {
	public static final String CLASS_NAME = "dan200.computercraft.core.apis.OSAPI";

	@Override
	public boolean matches(String className) {
		return className.equals(CLASS_NAME);
	}

	@Override
	public ClassVisitor patch(String className, ClassVisitor delegate) throws Exception {
		ClassVisitor visitor = new ClassVisitor(ASM5, delegate) {
			@Override
			public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
				super.visit(version, access, name, signature, superName, BinaryCore.addBinaryInterface(interfaces));
			}
		};

		// Whilst these seem pretty pointless, they ensure that os.queueEvent doesn't scramble bytes, only
		// the event name gets scrambled
		visitor = new FindingVisitor(
			visitor,
			new TypeInsnNode(INSTANCEOF, "java/lang/String")
		) {
			@Override
			public void handle(InsnList nodes, MethodVisitor visitor) {
				visitor.visitTypeInsn(INSTANCEOF, "[B");
			}
		}.onMethod("callMethod");

		visitor = new FindingVisitor(
			visitor,
			new TypeInsnNode(CHECKCAST, "java/lang/String")
		) {
			@Override
			public void handle(InsnList nodes, MethodVisitor visitor) {
				visitor.visitTypeInsn(CHECKCAST, "[B");
				visitor.visitTypeInsn(NEW, "java/lang/String");
				visitor.visitInsn(DUP_X1);
				visitor.visitInsn(SWAP);
				visitor.visitMethodInsn(INVOKESPECIAL, "java/lang/String", "<init>", "([B)V", false);
			}
		}.onMethod("callMethod");

		return visitor;
	}
}
