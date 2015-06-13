package org.squiddev.cctweaks.core.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.squiddev.cctweaks.core.utils.DebugLogger;
import org.squiddev.patcher.transformer.IPatcher;

import static org.objectweb.asm.Opcodes.*;

/**
 * Disables turtle commands.
 * This list can be modified at run time as it checks
 * every time the event is called
 */
public class DisableTurtleCommand implements IPatcher {
	protected static final String PREFIX = "dan200.computercraft.shared.turtle.core.Turtle";
	protected static final String SUFFIX = "Command";

	@Override
	public boolean matches(String className) {
		return className.startsWith(PREFIX) && className.endsWith(SUFFIX);
	}

	@Override
	public ClassVisitor patch(final String className, final ClassVisitor delegate) throws Exception {
		return new ClassVisitor(ASM5, delegate) {
			@Override
			public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
				MethodVisitor visitor = super.visitMethod(access, name, desc, signature, exceptions);
				if (name.equals("execute") && desc.equals("(Ldan200/computercraft/api/turtle/ITurtleAccess;)Ldan200/computercraft/api/turtle/TurtleCommandResult;")) {
					return new DisabledCommandVisitor(visitor, className);
				}
				return visitor;
			}
		};
	}

	protected static class DisabledCommandVisitor extends MethodVisitor {
		protected final String className;

		public DisabledCommandVisitor(MethodVisitor mv, String name) {
			super(ASM5, mv);
			this.className = name;
		}

		@Override
		public void visitCode() {
			super.visitCode();

			Label continueLabel = new Label();

			visitFieldInsn(GETSTATIC, "org/squiddev/cctweaks/core/Config", "turtleDisabledActions", "Ljava/util/Set;");
			visitLdcInsn(className.substring(PREFIX.length(), className.length() - SUFFIX.length()).toLowerCase());
			DebugLogger.debug("Method is " + className.substring(PREFIX.length(), className.length() - SUFFIX.length()).toLowerCase());
			visitMethodInsn(INVOKEINTERFACE, "java/util/Set", "contains", "(Ljava/lang/Object;)Z", true);
			visitJumpInsn(IFEQ, continueLabel);

			visitLdcInsn("Action disabled");
			visitMethodInsn(INVOKESTATIC, "dan200/computercraft/api/turtle/TurtleCommandResult", "failure", "(Ljava/lang/String;)Ldan200/computercraft/api/turtle/TurtleCommandResult;", false);
			visitInsn(ARETURN);

			visitLabel(continueLabel);
		}

		@Override
		public void visitMaxs(int maxStack, int maxLocals) {
			super.visitMaxs(Math.max(maxStack, 2), maxLocals);
		}
	}
}
