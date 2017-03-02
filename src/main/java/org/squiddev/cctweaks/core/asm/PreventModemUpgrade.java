package org.squiddev.cctweaks.core.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.squiddev.patcher.transformer.IPatcher;
import org.squiddev.patcher.visitors.FindingVisitor;

import static org.objectweb.asm.Opcodes.*;

/**
 * Prevents {@link dan200.computercraft.shared.pocket.recipes.PocketComputerUpgradeRecipe} from working when there is a
 * custom upgrade.
 */
public class PreventModemUpgrade implements IPatcher {
	@Override
	public boolean matches(String className) {
		return className.equals("dan200.computercraft.shared.pocket.recipes.PocketComputerUpgradeRecipe");
	}

	@Override
	public ClassVisitor patch(String className, ClassVisitor delegate) throws Exception {
		return new FindingVisitor(delegate,
			new MethodInsnNode(INVOKEVIRTUAL, "dan200/computercraft/shared/pocket/items/ItemPocketComputer", "getHasModem", "(Lnet/minecraft/item/ItemStack;)Z", false),
			new JumpInsnNode(IFEQ, null)
		) {
			@Override
			public void handle(InsnList nodes, MethodVisitor visitor) {
				Label exit = new Label();
				Label cont = ((JumpInsnNode) nodes.get(1)).label.getLabel();

				visitor.visitMethodInsn(INVOKEVIRTUAL, "dan200/computercraft/shared/pocket/items/ItemPocketComputer", "getHasModem", "(Lnet/minecraft/item/ItemStack;)Z", false);
				visitor.visitJumpInsn(IFNE, exit);

				visitor.visitVarInsn(ALOAD, 2);
				visitor.visitMethodInsn(INVOKESTATIC, "org/squiddev/cctweaks/core/pocket/PocketHooks", "hasPocketUpgrade", "(Lnet/minecraft/item/ItemStack;)Z", false);
				visitor.visitJumpInsn(IFEQ, cont);

				visitor.visitLabel(exit);
			}
		};
	}
}
