package org.squiddev.cctweaks.core.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.squiddev.patcher.transformer.IPatcher;
import org.squiddev.patcher.visitors.FindingVisitor;

import static org.objectweb.asm.Opcodes.*;

/**
 * Various patchers for pocket computers.
 *
 * @see org.squiddev.cctweaks.core.pocket.PocketHooks
 * @see org.squiddev.cctweaks.core.patch.ItemPocketComputer_Patch
 */
public class PocketUpgrades implements IPatcher {
	@Override
	public boolean matches(String className) {
		return className.equals("dan200.computercraft.shared.pocket.items.ItemPocketComputer");
	}

	@Override
	public ClassVisitor patch(String className, final ClassVisitor delegate) throws Exception {
		return new FindingVisitor(delegate,
			new TypeInsnNode(NEW, "dan200/computercraft/shared/pocket/apis/PocketAPI"),
			new InsnNode(DUP),
			new MethodInsnNode(INVOKESPECIAL, "dan200/computercraft/shared/pocket/apis/PocketAPI", "<init>", "()V", false),
			new MethodInsnNode(INVOKEVIRTUAL, "dan200/computercraft/shared/computer/core/ServerComputer", "addAPI", "(Ldan200/computercraft/core/apis/ILuaAPI;)V", false)) {
			@Override
			public void handle(InsnList nodes, MethodVisitor visitor) {
				// Use custom API instead
				visitor.visitTypeInsn(NEW, "org/squiddev/cctweaks/core/pocket/PocketAPIExtensions");
				visitor.visitInsn(DUP);
				visitor.visitVarInsn(ALOAD, 7);
				visitor.visitMethodInsn(INVOKESPECIAL, "org/squiddev/cctweaks/core/pocket/PocketAPIExtensions", "<init>", "(Ldan200/computercraft/shared/computer/core/ServerComputer;)V", false);
				visitor.visitMethodInsn(INVOKEVIRTUAL, "dan200/computercraft/shared/computer/core/ServerComputer", "addAPI", "(Ldan200/computercraft/core/apis/ILuaAPI;)V", false);

				// Create pocket access
				visitor.visitVarInsn(ALOAD, 2);
				visitor.visitVarInsn(ALOAD, 3);
				visitor.visitVarInsn(ALOAD, 7);
				visitor.visitMethodInsn(INVOKESTATIC, "org/squiddev/cctweaks/core/pocket/PocketHooks", "create", "(Lnet/minecraft/inventory/IInventory;Lnet/minecraft/item/ItemStack;Ldan200/computercraft/shared/computer/core/ServerComputer;)V", false);
			}
		}.onMethod("createServerComputer").once().mustFind();
	}
}
