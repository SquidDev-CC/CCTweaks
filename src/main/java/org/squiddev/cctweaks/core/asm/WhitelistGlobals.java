package org.squiddev.cctweaks.core.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.*;
import org.squiddev.patcher.transformer.IPatcher;
import org.squiddev.patcher.visitors.FindingVisitor;

import static org.objectweb.asm.Opcodes.*;

/**
 * Whitelist specific globals
 */
public class WhitelistGlobals implements IPatcher {
	@Override
	public boolean matches(String className) {
		return className.equals("dan200.computercraft.core.lua.LuaJLuaMachine");
	}

	@Override
	public ClassVisitor patch(String className, ClassVisitor delegate) throws Exception {
		return new FindingVisitor(delegate,
			new VarInsnNode(ALOAD, 0),
			new FieldInsnNode(GETFIELD, "dan200/computercraft/core/lua/LuaJLuaMachine", "m_globals", "Lorg/luaj/vm2/LuaValue;"),
			new LdcInsnNode(null), // Match anything
			new FieldInsnNode(GETSTATIC, "org/luaj/vm2/LuaValue", "NIL", "Lorg/luaj/vm2/LuaValue;"),
			new MethodInsnNode(INVOKEVIRTUAL, "org/luaj/vm2/LuaValue", "set", "(Ljava/lang/String;Lorg/luaj/vm2/LuaValue;)V", false)
		) {
			@Override
			public void handle(InsnList nodes, MethodVisitor visitor) {
				Object constant = ((LdcInsnNode) nodes.get(2)).cst;
				if (constant instanceof String) {
					Label blacklistLabel = new Label();

					visitor.visitFieldInsn(GETSTATIC, "org/squiddev/cctweaks/core/Config", "globalWhitelist", "Ljava/util/Set;");
					visitor.visitLdcInsn(constant);
					visitor.visitMethodInsn(INVOKEINTERFACE, "java/util/Set", "contains", "(Ljava/lang/Object;)Z", true);
					visitor.visitJumpInsn(IFNE, blacklistLabel);

					nodes.accept(visitor);

					visitor.visitLabel(blacklistLabel);
				} else {
					nodes.accept(visitor);
				}
			}
		}.onMethod("<init>");
	}
}
