package org.squiddev.cctweaks.core.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.*;
import org.squiddev.patcher.transformer.ClassMerger;
import org.squiddev.patcher.visitors.FindingVisitor;

import static org.objectweb.asm.Opcodes.*;

public class PatchTurtleRenderer extends ClassMerger {
	public PatchTurtleRenderer() {
		super("dan200.computercraft.client.render.TileEntityTurtleRenderer", "org.squiddev.cctweaks.core.patch.TurtleRenderer_Patch");
	}

	@Override
	public ClassVisitor patch(String className, ClassVisitor delegate) throws Exception {
		// Render turtles with a name of "Dinnerbone" or "Grumm" upside down.
		ClassVisitor mutator = new FindingVisitor(
			super.patch(className, delegate),
			new VarInsnNode(ALOAD, 9),
			new VarInsnNode(FLOAD, 8),
			new MethodInsnNode(INVOKEINTERFACE, "dan200/computercraft/shared/turtle/blocks/ITurtleTile", "getRenderYaw", "(F)F", true),
			new InsnNode(FCONST_0),
			new LdcInsnNode(new Float("-1.0")),
			new InsnNode(FCONST_0),
			new MethodInsnNode(INVOKESTATIC, "org/lwjgl/opengl/GL11", "glRotatef", "(FFFF)V", false)
		) {
			@Override
			public void handle(InsnList nodes, MethodVisitor visitor) {
				nodes.accept(visitor);
				visitor.visitVarInsn(ALOAD, 0);
				visitor.visitVarInsn(ALOAD, 13);
				visitor.visitInsn(ICONST_0);
				visitor.visitMethodInsn(INVOKEVIRTUAL, classType, "applyCustomNames", "(Ljava/lang/String;Z)V", false);
			}
		}.onMethod("func_147500_a").onMethod("renderTileEntityAt").once().mustFind();

		// Add custom drawing flags and scale
		mutator = new FindingVisitor(
			mutator,
			new VarInsnNode(ALOAD, 11),
			new FieldInsnNode(GETFIELD, "net/minecraft/util/Vec3", null, "D"),
			new MethodInsnNode(INVOKESTATIC, "org/lwjgl/opengl/GL11", "glTranslated", "(DDD)V", false)
		) {
			@Override
			public void handle(InsnList nodes, MethodVisitor visitor) {
				nodes.accept(visitor);
				visitor.visitVarInsn(ALOAD, 0);
				visitor.visitVarInsn(ALOAD, 1);
				visitor.visitMethodInsn(INVOKEVIRTUAL, classType, "scale", "(Lnet/minecraft/tileentity/TileEntity;)V", false);
			}
		}.onMethod("func_147500_a").onMethod("renderTileEntityAt").once().mustFind();

		// Remove custom drawing flags
		mutator = new FindingVisitor(
			mutator,
			new MethodInsnNode(INVOKESTATIC, "org/lwjgl/opengl/GL11", "glPopMatrix", "()V", false)
		) {
			@Override
			public void handle(InsnList nodes, MethodVisitor visitor) {
				visitor.visitVarInsn(ALOAD, 0);
				visitor.visitMethodInsn(INVOKEVIRTUAL, classType, "postScale", "()V", false);
				nodes.accept(visitor);
			}
		}.onMethod("func_147500_a").onMethod("renderTileEntityAt").once().mustFind();

		return mutator;
	}
}
