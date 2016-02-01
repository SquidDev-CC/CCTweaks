package org.squiddev.cctweaks.core.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
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
		return className.equals("dan200.computercraft.shared.pocket.items.ItemPocketComputer") || className.equals("dan200.computercraft.shared.computer.core.ServerComputer");
	}

	@Override
	public ClassVisitor patch(String className, final ClassVisitor delegate) throws Exception {
		if (className.equals("dan200.computercraft.shared.computer.core.ServerComputer")) {
			return new FindingVisitor(delegate, new InsnNode(RETURN)
			) {
				@Override
				public void handle(InsnList nodes, MethodVisitor visitor) {
					visitor.visitVarInsn(ALOAD, 0);
					visitor.visitMethodInsn(INVOKESTATIC, "org/squiddev/cctweaks/core/pocket/PocketHooks", "destroy", "(Ldan200/computercraft/shared/computer/core/ServerComputer;)V", false);

					nodes.accept(visitor);
				}
			}.onMethod("unload").once().mustFind();
		} else {
			ClassVisitor visitor = new FindingVisitor(delegate,
				new VarInsnNode(ALOAD, 7),
				new InsnNode(ICONST_2),
				new MethodInsnNode(INVOKEVIRTUAL, "dan200/computercraft/shared/computer/core/ServerComputer", "getPeripheral", "(I)Ldan200/computercraft/api/peripheral/IPeripheral;", false)
			) {
				@Override
				public void handle(InsnList nodes, MethodVisitor visitor) {
					visitor.visitVarInsn(ALOAD, 3);
					visitor.visitVarInsn(ALOAD, 1);
					visitor.visitVarInsn(ALOAD, 7);
					visitor.visitMethodInsn(INVOKESTATIC, "org/squiddev/cctweaks/core/pocket/PocketHooks", "update", "(Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;Ldan200/computercraft/shared/computer/core/ServerComputer;)V", false);

					nodes.accept(visitor);
				}
			}.onMethod("func_77663_a").onMethod("onUpdate").once().mustFind();

			visitor = new FindingVisitor(visitor,
				new MethodInsnNode(INVOKESPECIAL, "dan200/computercraft/shared/pocket/apis/PocketAPI", "<init>", "()V", false),
				new MethodInsnNode(INVOKEVIRTUAL, "dan200/computercraft/shared/computer/core/ServerComputer", "addAPI", "(Ldan200/computercraft/core/apis/ILuaAPI;)V", false)) {
				@Override
				public void handle(InsnList nodes, MethodVisitor visitor) {
					nodes.accept(visitor);

					visitor.visitVarInsn(ALOAD, 2);
					visitor.visitVarInsn(ALOAD, 3);
					visitor.visitVarInsn(ALOAD, 7);
					visitor.visitMethodInsn(INVOKESTATIC, "org/squiddev/cctweaks/core/pocket/PocketHooks", "create", "(Lnet/minecraft/inventory/IInventory;Lnet/minecraft/item/ItemStack;Ldan200/computercraft/shared/computer/core/ServerComputer;)V", false);
				}
			}.onMethod("createServerComputer").once().mustFind();

			return visitor;
		}
	}
}
