package org.squiddev.cctweaks.core.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.squiddev.patcher.transformer.IPatcher;
import org.squiddev.patcher.visitors.FindingVisitor;

import static org.objectweb.asm.Opcodes.*;

/**
 * Adds a custom timeout
 */
public class CustomTimeout implements IPatcher {
	@Override
	public boolean matches(String className) {
		return className.equals("dan200.computercraft.core.computer.ComputerThread$1");
	}

	@Override
	public ClassVisitor patch(String className, ClassVisitor delegate) throws Exception {
		return new FindingVisitor(
			delegate,
			new VarInsnNode(ALOAD, 4),
			new LdcInsnNode(7000L),
			new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Thread", "join", "(J)V", false)
		) {
			@Override
			public void handle(InsnList nodes, MethodVisitor visitor) {
				visitor.visitVarInsn(ALOAD, 4);
				visitor.visitFieldInsn(GETSTATIC, "org/squiddev/cctweaks/core/Config$Computer", "computerThreadTimeout", "I");
				visitor.visitInsn(I2L);
				visitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Thread", "join", "(J)V", false);
			}
		}.onMethod("run").once().mustFind();
	}
}
