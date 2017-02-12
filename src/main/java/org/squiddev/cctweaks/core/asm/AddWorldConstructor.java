package org.squiddev.cctweaks.core.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.squiddev.patcher.transformer.IPatcher;

import static org.objectweb.asm.Opcodes.*;

/**
 * Adds a constructor which takes {@link net.minecraft.world.World} to
 * {@link dan200.computercraft.shared.turtle.core.TurtlePlayer} in order for entity registration to work.
 */
public class AddWorldConstructor implements IPatcher {
	@Override
	public boolean matches(String className) {
		return className.equals("dan200.computercraft.shared.turtle.core.TurtlePlayer");
	}

	@Override
	public ClassVisitor patch(final String name, ClassVisitor delegate) throws Exception {
		return new ClassVisitor(ASM5, delegate) {
			@Override
			public void visitEnd() {
				MethodVisitor visitor = super.visitMethod(ACC_PUBLIC, "<init>", "(Lnet/minecraft/world/World;)V", null, null);
				visitor.visitCode();
				visitor.visitVarInsn(ALOAD, 0);
				visitor.visitVarInsn(ALOAD, 1);
				visitor.visitTypeInsn(CHECKCAST, "net/minecraft/world/WorldServer");
				visitor.visitMethodInsn(INVOKESPECIAL, "dan200/computercraft/shared/turtle/core/TurtlePlayer", "<init>", "(Lnet/minecraft/world/WorldServer;)V", false);
				visitor.visitInsn(RETURN);
				visitor.visitMaxs(2, 2);
				visitor.visitEnd();

				super.visitEnd();
			}
		};
	}
}
