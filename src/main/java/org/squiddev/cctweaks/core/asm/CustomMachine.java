package org.squiddev.cctweaks.core.asm;

import dan200.computercraft.core.computer.Computer;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.*;
import org.squiddev.patcher.transformer.IPatcher;
import org.squiddev.patcher.visitors.FindingVisitor;

import static org.objectweb.asm.Opcodes.*;

/**
 * Allows adding custom machines
 *
 * @see org.squiddev.cctweaks.core.lua.LuaHelpers#createMachine(Computer)
 * @see Computer#initLua()
 */
public class CustomMachine implements IPatcher {
	@Override
	public boolean matches(String className) {
		return className.equals("dan200.computercraft.core.computer.Computer");
	}

	@Override
	public ClassVisitor patch(String className, ClassVisitor delegate) throws Exception {
		return new FindingVisitor(
			delegate,
			new TypeInsnNode(NEW, "dan200/computercraft/core/lua/LuaJLuaMachine"),
			new InsnNode(DUP),
			new VarInsnNode(ALOAD, 0),
			new MethodInsnNode(INVOKESPECIAL, "dan200/computercraft/core/lua/LuaJLuaMachine", "<init>", "(Ldan200/computercraft/core/computer/Computer;)V", false)
		) {
			@Override
			public void handle(InsnList nodes, MethodVisitor visitor) {
				visitor.visitVarInsn(ALOAD, 0);
				visitor.visitMethodInsn(INVOKESTATIC, "org/squiddev/cctweaks/core/lua/LuaHelpers", "createMachine", "(Ldan200/computercraft/core/computer/Computer;)Ldan200/computercraft/core/lua/ILuaMachine;", false);
			}
		};
	}
}
