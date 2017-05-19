package org.squiddev.cctweaks.core.asm;

import dan200.computercraft.shared.pocket.recipes.PocketComputerUpgradeRecipe;
import dan200.computercraft.shared.turtle.recipes.TurtleUpgradeRecipe;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.squiddev.patcher.transformer.IPatcher;
import org.squiddev.patcher.visitors.FindingVisitor;

import static org.objectweb.asm.Opcodes.*;

/**
 * Modifies {@link TurtleUpgradeRecipe} and {@link PocketComputerUpgradeRecipe} to set the ROM id.
 */
public class CopyRom implements IPatcher {
	@Override
	public boolean matches(String className) {
		return className.equals("dan200.computercraft.shared.turtle.recipes.TurtleUpgradeRecipe")
			|| className.equals("dan200.computercraft.shared.pocket.recipes.PocketComputerUpgradeRecipe");
	}

	@Override
	public ClassVisitor patch(String className, ClassVisitor delegate) throws Exception {
		if (className.endsWith("TurtleUpgradeRecipe")) {
			return new FindingVisitor(
				delegate,
				new MethodInsnNode(INVOKESTATIC, "dan200/computercraft/shared/turtle/items/TurtleItemFactory", "create", "(ILjava/lang/String;ILdan200/computercraft/shared/computer/core/ComputerFamily;Ldan200/computercraft/api/turtle/ITurtleUpgrade;Ldan200/computercraft/api/turtle/ITurtleUpgrade;ILnet/minecraft/util/ResourceLocation;)Lnet/minecraft/item/ItemStack;", false),
				new InsnNode(ARETURN)
			) {
				@Override
				public void handle(InsnList nodes, MethodVisitor visitor) {
					nodes.getFirst().accept(visitor);
					visitor.visitVarInsn(ALOAD, 3);
					visitor.visitMethodInsn(INVOKESTATIC, "org/squiddev/cctweaks/core/rom/CraftingSetRom", "copyRom", "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;", false);
					visitor.visitInsn(ARETURN);
				}
			}.onMethod("func_77572_b").onMethod("getCraftingResult").mustFind().once();
		} else {
			return new FindingVisitor(
				delegate,
				new MethodInsnNode(INVOKESTATIC, "dan200/computercraft/shared/pocket/items/PocketComputerItemFactory", "create", "(ILjava/lang/String;ILdan200/computercraft/shared/computer/core/ComputerFamily;Ldan200/computercraft/api/pocket/IPocketUpgrade;)Lnet/minecraft/item/ItemStack;", false),
				new InsnNode(ARETURN)
			) {
				@Override
				public void handle(InsnList nodes, MethodVisitor visitor) {
					nodes.getFirst().accept(visitor);
					visitor.visitVarInsn(ALOAD, 2);
					visitor.visitMethodInsn(INVOKESTATIC, "org/squiddev/cctweaks/core/rom/CraftingSetRom", "copyRom", "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;", false);
					visitor.visitInsn(ARETURN);
				}
			}.onMethod("func_77572_b").onMethod("getCraftingResult").mustFind().once();
		}
	}
}
