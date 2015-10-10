package org.squiddev.cctweaks.core.asm.binary;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.TypeInsnNode;
import org.squiddev.patcher.visitors.FindingVisitor;

import java.util.Arrays;

import static org.objectweb.asm.Opcodes.*;

/**
 * Utilities for injecting binary patches
 */
public final class BinaryUtils {
	private BinaryUtils() {
		throw new RuntimeException("Cannot creat instance of BinaryUtils");
	}

	private static String[] addBinaryInterface(String[] interfaces) {
		String[] newInterfaces = Arrays.copyOf(interfaces, interfaces.length + 1);
		newInterfaces[interfaces.length] = BinaryCore.BINARY_OBJECT;
		return newInterfaces;
	}

	/**
	 * Add a binary interface to a class
	 *
	 * @param visitor The original visitor
	 * @return The new visitor
	 */
	public static ClassVisitor withBinaryInterface(ClassVisitor visitor) {
		return new ClassVisitor(ASM5, visitor) {
			@Override
			public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
				super.visit(version, access, name, signature, superName, addBinaryInterface(interfaces));
			}
		};
	}

	/**
	 * Replace string casts with byte array casts then string creation
	 *
	 * @param visitor The original visitor
	 * @return The new visitor
	 */
	public static ClassVisitor withStringCasts(ClassVisitor visitor, String... methodNames) {
		// Whilst these seem pretty pointless, they ensure that os.queueEvent doesn't scramble bytes, only
		// the event name gets scrambled
		FindingVisitor finder = new FindingVisitor(
			visitor,
			new TypeInsnNode(INSTANCEOF, "java/lang/String")
		) {
			@Override
			public void handle(InsnList nodes, MethodVisitor visitor) {
				visitor.visitTypeInsn(INSTANCEOF, "[B");
			}
		}.onMethod("callMethod");
		for (String name : methodNames) {
			finder.onMethod(name);
		}

		finder = new FindingVisitor(
			finder,
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
		for (String name : methodNames) {
			finder.onMethod(name);
		}

		return finder;
	}

}
