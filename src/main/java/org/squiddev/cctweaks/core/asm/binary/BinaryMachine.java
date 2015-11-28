package org.squiddev.cctweaks.core.asm.binary;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.*;
import org.squiddev.patcher.transformer.IPatcher;
import org.squiddev.patcher.visitors.FindingVisitor;

import static org.objectweb.asm.Opcodes.*;

/**
 * Patches various {@link dan200.computercraft.core.lua.LuaJLuaMachine} methods to support binary mode
 */
public class BinaryMachine implements IPatcher {
	private static final String CLASS_MACHINE = "dan200.computercraft.core.lua.LuaJLuaMachine";
	private static final String TYPE_MACHINE = CLASS_MACHINE.replace('.', '/');
	public static final String CLASS_WRAPPED = CLASS_MACHINE + "$2";
	public static final String TYPE_WRAPPED = CLASS_WRAPPED.replace('.', '/');

	@Override
	public boolean matches(String className) {
		return className.startsWith(CLASS_MACHINE) && (className.endsWith("Machine") || className.endsWith("Machine$2"));
	}

	@Override
	public ClassVisitor patch(String className, ClassVisitor delegate) throws Exception {
		if (className.endsWith("Machine$2")) {
			return patchWrappedObject(delegate);
		} else if (className.endsWith("Machine")) {
			return patchMachine(delegate);
		} else {
			return delegate;
		}
	}

	private ClassVisitor patchWrappedObject(ClassVisitor visitor) {
		visitor = new FindingVisitor(
			visitor,
			new VarInsnNode(ALOAD, 0),
			new FieldInsnNode(GETFIELD, TYPE_WRAPPED, "this$0", "L" + TYPE_MACHINE + ";"),
			new VarInsnNode(ALOAD, 1),
			new InsnNode(ICONST_1),
			new MethodInsnNode(INVOKESTATIC, TYPE_MACHINE, "access$200", null, false)
		) {
			@Override
			public void handle(InsnList nodes, MethodVisitor visitor) {
				visitor.visitInsn(ACONST_NULL);
			}
		}.onMethod("invoke").once().mustFind();

		visitor = new FindingVisitor(
			visitor,
			new VarInsnNode(ALOAD, 2),
			new MethodInsnNode(INVOKEINTERFACE, "dan200/computercraft/api/lua/ILuaObject", "callMethod", "(Ldan200/computercraft/api/lua/ILuaContext;I[Ljava/lang/Object;)[Ljava/lang/Object;", true)
		) {
			@Override
			public void handle(InsnList nodes, MethodVisitor visitor) {
				visitor.visitVarInsn(ALOAD, 1);
				visitor.visitMethodInsn(INVOKESTATIC, "org/squiddev/cctweaks/core/lua/LuaHelpers", "delegateLuaObject", "(Ldan200/computercraft/api/lua/ILuaObject;Ldan200/computercraft/api/lua/ILuaContext;ILorg/luaj/vm2/Varargs;)[Ljava/lang/Object;", false);
			}
		}.onMethod("invoke").once().mustFind();

		return visitor;
	}

	private ClassVisitor patchMachine(ClassVisitor visitor) {
		return new FindingVisitor(
			visitor,
			new VarInsnNode(ALOAD, 1),
			new TypeInsnNode(INSTANCEOF, "java/lang/String")
		) {
			@Override
			public void handle(InsnList nodes, MethodVisitor visitor) {
				visitor.visitVarInsn(ALOAD, 1);
				visitor.visitTypeInsn(INSTANCEOF, "[B");

				Label cont = new Label();
				visitor.visitJumpInsn(IFEQ, cont);
				visitor.visitVarInsn(ALOAD, 1);
				visitor.visitTypeInsn(CHECKCAST, "[B");
				visitor.visitMethodInsn(INVOKESTATIC, "org/luaj/vm2/LuaValue", "valueOf", "([B)Lorg/luaj/vm2/LuaString;", false);
				visitor.visitInsn(ARETURN);

				visitor.visitLabel(cont);
				nodes.accept(visitor);
			}
		}.onMethod("toValue").once().mustFind();
	}
}
