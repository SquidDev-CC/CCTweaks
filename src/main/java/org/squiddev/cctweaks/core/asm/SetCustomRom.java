package org.squiddev.cctweaks.core.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.squiddev.patcher.transformer.IPatcher;
import org.squiddev.patcher.visitors.FindingVisitor;

import static org.objectweb.asm.Opcodes.*;

/**
 * Sets {@link org.squiddev.cctweaks.core.patch.ServerComputer_Patch#setCustomRom(int)} for computers
 */
public class SetCustomRom implements IPatcher {
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

				Label finish = new Label();

				visitor.visitVarInsn(ALOAD, 0);
				visitor.visitFieldInsn(GETFIELD, "dan200/computercraft/shared/computer/blocks/TileComputerBase", "hasDisk", "Z");
				visitor.visitInsn(ICONST_0);
				visitor.visitJumpInsn(IF_ICMPEQ, finish);

				visitor.visitVarInsn(ALOAD, 2);
				visitor.visitVarInsn(ALOAD, 0);
				visitor.visitFieldInsn(GETFIELD, "dan200/computercraft/shared/computer/blocks/TileComputerBase", "diskId", "I");
				visitor.visitMethodInsn(INVOKEVIRTUAL, "dan200/computercraft/shared/computer/core/ServerComputer", "setCustomRom", "(I)V", false);

				visitor.visitLabel(finish);
			}
		}.onMethod("createServerComputer").once().mustFind();
	}
}
