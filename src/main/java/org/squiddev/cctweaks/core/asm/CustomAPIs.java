package org.squiddev.cctweaks.core.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.squiddev.patcher.transformer.IPatcher;

import static org.objectweb.asm.Opcodes.*;

/**
 * Ability to inject custom APIs into computer
 */
public class CustomAPIs implements IPatcher {
	@Override
	public boolean matches(String className) {
		return className.equals("dan200/computercraft/core/computer/Computer");
	}

	@Override
	public ClassVisitor patch(String className, ClassVisitor delegate) throws Exception {
		return new ClassVisitor(ASM5, delegate) {
			@Override
			public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
				MethodVisitor visitor = super.visitMethod(access, name, desc, signature, exceptions);
				if (name.equals("createAPIs")) {
					visitor = new MethodVisitor(ASM5, visitor) {
						@Override
						public void visitCode() {
							super.visitCode();
							mv.visitVarInsn(ALOAD, 0);
							mv.visitMethodInsn(INVOKESTATIC, "org/squiddev/cctweaks/core/lua/LuaEnvironment", "inject", "(Ldan200/computercraft/core/computer/Computer;)V", false);
						}
					};
				}
				return visitor;
			}
		};
	}
}
