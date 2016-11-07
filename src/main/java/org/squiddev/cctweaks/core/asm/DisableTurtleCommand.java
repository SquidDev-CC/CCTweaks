package org.squiddev.cctweaks.core.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.squiddev.patcher.transformer.IPatcher;

import static org.objectweb.asm.Opcodes.*;

/**
 * Disables turtle commands.
 * This list can be modified at run time as it checks
 * every time the event is called
 */
public class DisableTurtleCommand implements IPatcher {
	private static final String PREFIX = "dan200.computercraft.shared.turtle.core.Turtle";
	private static final String SUFFIX = "Command";

	@Override
	public boolean matches(String className) {
		return className.startsWith(PREFIX) && className.endsWith(SUFFIX);
	}

	@Override
	public ClassVisitor patch(String className, ClassVisitor delegate) throws Exception {
		return new DisabledCommandClassVisitor(className, delegate);
	}

	private static class DisabledCommandClassVisitor extends ClassVisitor {
		private static final String DESC = "(Ldan200/computercraft/api/turtle/ITurtleAccess;)Ldan200/computercraft/api/turtle/TurtleCommandResult;";

		private final String className;
		private String parentName;
		private boolean visited = false;

		public DisabledCommandClassVisitor(String className, ClassVisitor cv) {
			super(ASM5, cv);
			this.className = className;
		}

		@Override
		public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
			super.visit(version, access, name, signature, superName, interfaces);
			parentName = superName;
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
			MethodVisitor visitor = super.visitMethod(access, name, desc, signature, exceptions);
			if (name.equals("execute") && desc.equals(DESC)) {
				visited = true;
				return new DisabledCommandVisitor(visitor, className);
			}
			return visitor;
		}

		@Override
		public void visitEnd() {
			if (!visited) {
				MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "execute", DESC, null, null);
				mv.visitCode();
				write(className, mv);

				mv.visitVarInsn(ALOAD, 0);
				mv.visitVarInsn(ALOAD, 1);
				mv.visitMethodInsn(INVOKESPECIAL, parentName, "execute", DESC, false);
				mv.visitInsn(ARETURN);
			}
			super.visitEnd();
		}
	}

	private static class DisabledCommandVisitor extends MethodVisitor {
		private final String className;

		public DisabledCommandVisitor(MethodVisitor mv, String name) {
			super(ASM5, mv);
			this.className = name;
		}

		@Override
		public void visitCode() {
			super.visitCode();
			write(className, mv);
		}

		@Override
		public void visitMaxs(int maxStack, int maxLocals) {
			super.visitMaxs(Math.max(maxStack, 2), maxLocals);
		}
	}

	private static void write(String className, MethodVisitor mv) {
		Label continueLabel = new Label();

		mv.visitFieldInsn(GETSTATIC, "org/squiddev/cctweaks/core/Config", "turtleDisabledActions", "Ljava/util/Set;");
		mv.visitLdcInsn(className.substring(PREFIX.length(), className.length() - SUFFIX.length()).toLowerCase());
		mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Set", "contains", "(Ljava/lang/Object;)Z", true);
		mv.visitJumpInsn(IFEQ, continueLabel);

		mv.visitLdcInsn("Action disabled");
		mv.visitMethodInsn(INVOKESTATIC, "dan200/computercraft/api/turtle/TurtleCommandResult", "failure", "(Ljava/lang/String;)Ldan200/computercraft/api/turtle/TurtleCommandResult;", false);
		mv.visitInsn(ARETURN);

		mv.visitLabel(continueLabel);
	}
}
