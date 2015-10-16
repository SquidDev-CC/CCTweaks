package org.squiddev.cctweaks.core.asm.binary;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.*;
import org.squiddev.patcher.transformer.IPatcher;
import org.squiddev.patcher.visitors.FindingVisitor;

import static org.objectweb.asm.Opcodes.*;
import static org.squiddev.cctweaks.core.asm.binary.BinaryUtils.BINARY_CONVERTER;
import static org.squiddev.cctweaks.core.asm.binary.BinaryUtils.BINARY_OBJECT;

/**
 * Patches various {@link dan200.computercraft.core.lua.LuaJLuaMachine} methods to support binary mode
 */
public class BinaryMachine implements IPatcher {
	private static final String IS_BINARY = "_isBinaryMode";

	private static final String CLASS_MACHINE = "dan200.computercraft.core.lua.LuaJLuaMachine";
	private static final String TYPE_MACHINE = CLASS_MACHINE.replace('.', '/');
	public static final String CLASS_WRAPPED = CLASS_MACHINE + "$2";
	public static final String TYPE_WRAPPED = CLASS_WRAPPED.replace('.', '/');
	private static final String CLASS_CONTEXT = CLASS_WRAPPED + "$1";
	private static final String TYPE_CONTEXT = CLASS_CONTEXT.replace('.', '/');

	@Override
	public boolean matches(String className) {
		// TODO: Simplify based off length
		if (!className.startsWith(CLASS_MACHINE)) return false;
		return className.endsWith("Machine") || className.endsWith("Machine$2") || className.endsWith("Machine$2$1");
	}

	@Override
	public ClassVisitor patch(String className, ClassVisitor delegate) throws Exception {
		// TODO: Simplify based off length
		if (className.endsWith("Machine$2")) {
			return patchWrappedObject(delegate);
		} else if (className.endsWith("Machine$2$1")) {
			return patchWrappedContext(delegate);
		} else if (className.endsWith("Machine")) {
			return patchMachine(delegate);
		} else {
			return delegate;
		}
	}

	private ClassVisitor patchWrappedObject(ClassVisitor visitor) {
		visitor.visitField(ACC_PUBLIC | ACC_FINAL, IS_BINARY, "Z", null, null);

		visitor = new FindingVisitor(
			visitor,
			new VarInsnNode(ALOAD, 0),
			new VarInsnNode(ALOAD, 2)
		) {
			@Override
			public void handle(InsnList nodes, MethodVisitor visitor) {
				visitor.visitVarInsn(ALOAD, 0);
				visitor.visitVarInsn(ALOAD, 2);
				visitor.visitTypeInsn(INSTANCEOF, BINARY_OBJECT);
				visitor.visitFieldInsn(PUTFIELD, TYPE_WRAPPED, IS_BINARY, "Z");

				nodes.accept(visitor);
			}
		}.onMethod("<init>").once().mustFind();

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
				visitor.visitVarInsn(ALOAD, 0);
				visitor.visitFieldInsn(GETFIELD, TYPE_WRAPPED, IS_BINARY, "Z");

				Label normal = new Label(), cont = new Label();
				visitor.visitJumpInsn(IFEQ, normal);

				injectBinary(visitor);
				visitor.visitJumpInsn(GOTO, cont);

				visitor.visitLabel(normal);
				nodes.accept(visitor);

				visitor.visitLabel(cont);
			}
		}.onMethod("invoke").once().mustFind();

		return visitor;
	}

	private ClassVisitor patchWrappedContext(ClassVisitor visitor) {
		return new FindingVisitor(visitor,
			new VarInsnNode(ALOAD, 0),
			new FieldInsnNode(GETFIELD, TYPE_CONTEXT, "this$1", "L" + TYPE_WRAPPED + ";"),
			new FieldInsnNode(GETFIELD, TYPE_WRAPPED, "this$0", "L" + TYPE_MACHINE + ";"),
			new VarInsnNode(ALOAD, 1),
			new InsnNode(ICONST_1),
			new MethodInsnNode(INVOKESTATIC, TYPE_MACHINE, "access$200", null, false)
		) {
			@Override
			public void handle(InsnList nodes, MethodVisitor visitor) {
				visitor.visitVarInsn(ALOAD, 0);
				visitor.visitFieldInsn(GETFIELD, TYPE_CONTEXT, "this$1", "L" + TYPE_WRAPPED + ";");
				visitor.visitFieldInsn(GETFIELD, TYPE_WRAPPED, IS_BINARY, "Z");

				Label normal = new Label(), cont = new Label();
				visitor.visitJumpInsn(IFEQ, normal);

				injectBinary(visitor);
				visitor.visitJumpInsn(GOTO, cont);

				visitor.visitLabel(normal);
				nodes.accept(visitor);

				visitor.visitLabel(cont);
			}
		};
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

	private static void injectBinary(MethodVisitor visitor) {
		visitor.visitVarInsn(ALOAD, 1);
		visitor.visitInsn(ICONST_1);
		visitor.visitMethodInsn(INVOKESTATIC, BINARY_CONVERTER, "toObjects", "(Lorg/luaj/vm2/Varargs;I)[Ljava/lang/Object;", false);
	}
}
