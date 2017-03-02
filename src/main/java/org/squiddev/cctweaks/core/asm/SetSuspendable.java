package org.squiddev.cctweaks.core.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.squiddev.patcher.transformer.IPatcher;
import org.squiddev.patcher.visitors.FindingVisitor;

import static org.objectweb.asm.Opcodes.*;

/**
 * Sets {@link org.squiddev.cctweaks.core.patch.ServerComputer_Patch#setSuspendable()} for computers
 */
public class SetSuspendable implements IPatcher {
	@Override
	public boolean matches(String className) {
		return className.equals("dan200.computercraft.shared.computer.blocks.TileComputerBase");
	}

	@Override
	public ClassVisitor patch(String className, ClassVisitor delegate) throws Exception {
		return new FindingVisitor(
			delegate,
			new MethodInsnNode(INVOKEVIRTUAL, "dan200/computercraft/shared/computer/blocks/TileComputerBase", "createComputer", "(II)Ldan200/computercraft/shared/computer/core/ServerComputer;", false),
			new VarInsnNode(ASTORE, 2)
		) {
			@Override
			public void handle(InsnList nodes, MethodVisitor visitor) {
				nodes.accept(visitor);

				visitor.visitVarInsn(ALOAD, 2);
				visitor.visitMethodInsn(INVOKEVIRTUAL, "dan200/computercraft/shared/computer/core/ServerComputer", "setSuspendable", "()V", false);
			}
		}.onMethod("createServerComputer").once().mustFind();
	}
}
