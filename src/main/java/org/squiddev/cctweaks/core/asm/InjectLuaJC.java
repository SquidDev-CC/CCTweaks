package org.squiddev.cctweaks.core.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.squiddev.patcher.transformer.IPatcher;
import org.squiddev.patcher.visitors.FindingVisitor;

import static org.objectweb.asm.Opcodes.*;

/**
 * Optionally inject LuaJC
 */
public class InjectLuaJC implements IPatcher {
	@Override
	public boolean matches(String className) {
		return className.equals("dan200.computercraft.core.lua.LuaJLuaMachine");
	}

	@Override
	public ClassVisitor patch(String className, ClassVisitor delegate) throws Exception {
		return new FindingVisitor(
			delegate,
			new VarInsnNode(ALOAD, 0),
			new MethodInsnNode(INVOKESTATIC, "org/luaj/vm2/lib/jse/JsePlatform", "debugGlobals", "()Lorg/luaj/vm2/LuaTable;", false),
			new FieldInsnNode(PUTFIELD, "dan200/computercraft/core/lua/LuaJLuaMachine", "m_globals", "Lorg/luaj/vm2/LuaValue;")

		) {
			@Override
			public void handle(InsnList nodes, MethodVisitor visitor) {
				Label continueLabel = new Label();
				visitor.visitFieldInsn(GETSTATIC, "org/squiddev/cctweaks/core/Config$Computer", "luaJC", "Z");
				visitor.visitJumpInsn(IFEQ, continueLabel);
				visitor.visitMethodInsn(INVOKESTATIC, "org/squiddev/cctweaks/core/lua/FallbackLuaJC", "install", "()V", false);
				visitor.visitLabel(continueLabel);

				nodes.accept(visitor);
			}
		}.onMethod("<init>").once().mustFind();
	}
}
