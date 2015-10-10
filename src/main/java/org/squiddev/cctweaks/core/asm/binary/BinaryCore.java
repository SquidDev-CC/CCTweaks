package org.squiddev.cctweaks.core.asm.binary;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.*;
import org.squiddev.patcher.transformer.IPatcher;
import org.squiddev.patcher.visitors.FindingVisitor;

import java.util.Arrays;

import static org.objectweb.asm.Opcodes.*;

public final class BinaryCore {
	private static final String IS_BINARY = "_isBinaryMode";
	public static final String BINARY_OBJECT = "org/squiddev/cctweaks/api/lua/IBinaryLuaObject";
	private static final String MACHINE = "dan200/computercraft/core/lua/LuaJLuaMachine";

	private BinaryCore() {
		throw new RuntimeException("Cannot create BinaryPatches");
	}

	public static class PatchWrappedObject implements IPatcher {
		public static final String CLASS_NAME = "dan200.computercraft.core.lua.LuaJLuaMachine$2";
		public static final String TYPE_NAME = CLASS_NAME.replace('.', '/');

		@Override
		public boolean matches(String className) {
			return className.equals(CLASS_NAME);
		}

		@Override
		public ClassVisitor patch(String className, ClassVisitor delegate) throws Exception {
			delegate.visitField(ACC_PUBLIC | ACC_FINAL, IS_BINARY, "Z", null, null);

			ClassVisitor visitor = new FindingVisitor(
				delegate,
				new VarInsnNode(ALOAD, 0),
				new VarInsnNode(ALOAD, 2)
			) {
				@Override
				public void handle(InsnList nodes, MethodVisitor visitor) {
					visitor.visitVarInsn(ALOAD, 0);
					visitor.visitVarInsn(ALOAD, 2);
					visitor.visitTypeInsn(INSTANCEOF, BINARY_OBJECT);
					visitor.visitFieldInsn(PUTFIELD, TYPE_NAME, IS_BINARY, "Z");

					nodes.accept(visitor);
				}
			}.onMethod("<init>").once().mustFind();

			visitor = new FindingVisitor(
				visitor,
				new VarInsnNode(ALOAD, 0),
				new FieldInsnNode(GETFIELD, TYPE_NAME, "this$0", "L" + MACHINE + ";"),
				new VarInsnNode(ALOAD, 1),
				new InsnNode(ICONST_1),
				new MethodInsnNode(INVOKESTATIC, MACHINE, "access$200", null, false)
			) {
				@Override
				public void handle(InsnList nodes, MethodVisitor visitor) {
					visitor.visitVarInsn(ALOAD, 0);
					visitor.visitFieldInsn(GETFIELD, TYPE_NAME, IS_BINARY, "Z");

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
	}

	public static class PatchWrappedContext implements IPatcher {
		private static final String CLASS_NAME = PatchWrappedObject.CLASS_NAME + "$1";
		private static final String TYPE_NAME = CLASS_NAME.replace('.', '/');

		@Override
		public boolean matches(String className) {
			return className.equals(CLASS_NAME);
		}

		@Override
		public ClassVisitor patch(String className, ClassVisitor delegate) throws Exception {
			return new FindingVisitor(delegate,
				new VarInsnNode(ALOAD, 0),
				new FieldInsnNode(GETFIELD, TYPE_NAME, "this$1", "L" + PatchWrappedObject.TYPE_NAME + ";"),
				new FieldInsnNode(GETFIELD, PatchWrappedObject.TYPE_NAME, "this$0", "L" + MACHINE + ";"),
				new VarInsnNode(ALOAD, 1),
				new InsnNode(ICONST_1),
				new MethodInsnNode(INVOKESTATIC, MACHINE, "access$200", null, false)
			) {
				@Override
				public void handle(InsnList nodes, MethodVisitor visitor) {
					visitor.visitVarInsn(ALOAD, 0);
					visitor.visitFieldInsn(GETFIELD, TYPE_NAME, "this$1", "L" + PatchWrappedObject.TYPE_NAME + ";");
					visitor.visitFieldInsn(GETFIELD, PatchWrappedObject.TYPE_NAME, IS_BINARY, "Z");

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
	}

	public static void injectBinary(MethodVisitor visitor) {
		visitor.visitVarInsn(ALOAD, 1);
		visitor.visitInsn(ICONST_1);
		visitor.visitMethodInsn(INVOKESTATIC, "org/squiddev/cctweaks/core/lua/BinaryConverter", "toObjects", "(Lorg/luaj/vm2/Varargs;I)[Ljava/lang/Object;", false);
	}

	public static class PatchToValue implements IPatcher {
		@Override
		public boolean matches(String className) {
			return className.equals("dan200.computercraft.core.lua.LuaJLuaMachine");
		}

		@Override
		public ClassVisitor patch(String className, ClassVisitor delegate) throws Exception {
			return new FindingVisitor(
				delegate,
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

	public static String[] addBinaryInterface(String[] interfaces) {
		String[] newInterfaces = Arrays.copyOf(interfaces, interfaces.length + 1);
		newInterfaces[interfaces.length] = BinaryCore.BINARY_OBJECT;
		return newInterfaces;
	}

}
