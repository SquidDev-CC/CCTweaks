package org.squiddev.cctweaks.core.asm;

import dan200.computercraft.api.lua.ILuaObject;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.squiddev.cctweaks.api.lua.IExtendedLuaObject;
import org.squiddev.patcher.transformer.IPatcher;
import org.squiddev.patcher.visitors.FindingVisitor;

import static org.objectweb.asm.Opcodes.*;

/**
 * Allow custom fields for Lua objects
 *
 * @see dan200.computercraft.core.lua.LuaJLuaMachine#wrapLuaObject(ILuaObject)
 * @see IExtendedLuaObject#getAdditionalData()
 */
public class AddAdditionalData implements IPatcher {
	private static final String EXTENDED_OBJECT = "org/squiddev/cctweaks/api/lua/IExtendedLuaObject";
	private static final int OBJECT_SLOT = 1;
	private static final int TABLE_SLOT = 2;
	private static final int ITERATOR_SLOT = 3;
	private static final int ENTRY_SLOT = 4;

	@Override
	public boolean matches(String className) {
		return className.equals("dan200.computercraft.core.lua.LuaJLuaMachine");
	}

	@Override
	public ClassVisitor patch(String className, ClassVisitor delegate) throws Exception {
		return new FindingVisitor(delegate,
			new VarInsnNode(ALOAD, TABLE_SLOT),
			new InsnNode(ARETURN)
		) {
			@Override
			public void handle(InsnList nodes, MethodVisitor visitor) {
				Label exit = new Label();

				// if(object instanceof EXTENDED_OBJECT)
				visitor.visitVarInsn(ALOAD, OBJECT_SLOT);
				visitor.visitTypeInsn(INSTANCEOF, EXTENDED_OBJECT);
				visitor.visitJumpInsn(IFEQ, exit);

				visitor.visitFrame(F_APPEND, 2, new Object[]{"java/util/Iterator", "java/util/Map$Entry"}, 0, null);

				// object.getAdditionalData().entrySet().iterator()
				visitor.visitVarInsn(ALOAD, OBJECT_SLOT);
				visitor.visitTypeInsn(CHECKCAST, EXTENDED_OBJECT);
				visitor.visitMethodInsn(INVOKEINTERFACE, EXTENDED_OBJECT, "getAdditionalData", "()Ljava/util/Map;", true);
				visitor.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "entrySet", "()Ljava/util/Set;", true);
				visitor.visitMethodInsn(INVOKEINTERFACE, "java/util/Set", "iterator", "()Ljava/util/Iterator;", true);
				visitor.visitVarInsn(ASTORE, ITERATOR_SLOT);

				Label loop = new Label();
				visitor.visitLabel(loop);

				// while(it.hasNext())
				visitor.visitVarInsn(ALOAD, ITERATOR_SLOT);
				visitor.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z", true);
				visitor.visitJumpInsn(IFEQ, exit);

				// entry = it.next()
				visitor.visitVarInsn(ALOAD, ITERATOR_SLOT);
				visitor.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;", true);
				visitor.visitTypeInsn(CHECKCAST, "java/util/Map$Entry");
				visitor.visitVarInsn(ASTORE, ENTRY_SLOT);

				// table.set(
				visitor.visitVarInsn(ALOAD, 2);

				// this.toValue(entry.getKey())
				visitor.visitVarInsn(ALOAD, 0);
				visitor.visitVarInsn(ALOAD, ENTRY_SLOT);
				visitor.visitMethodInsn(INVOKEINTERFACE, "java/util/Map$Entry", "getKey", "()Ljava/lang/Object;", true);
				visitor.visitMethodInsn(INVOKESPECIAL, "dan200/computercraft/core/lua/LuaJLuaMachine", "toValue", "(Ljava/lang/Object;)Lorg/luaj/vm2/LuaValue;", false);

				// this.toValue(entry.getValue())
				visitor.visitVarInsn(ALOAD, 0);
				visitor.visitVarInsn(ALOAD, ENTRY_SLOT);
				visitor.visitMethodInsn(INVOKEINTERFACE, "java/util/Map$Entry", "getKey", "()Ljava/lang/Object;", true);
				visitor.visitMethodInsn(INVOKESPECIAL, "dan200/computercraft/core/lua/LuaJLuaMachine", "toValue", "(Ljava/lang/Object;)Lorg/luaj/vm2/LuaValue;", false);

				// Actual table.set(key, val)
				visitor.visitMethodInsn(INVOKEVIRTUAL, "org/luaj/vm2/LuaValue", "set", "(Lorg/luaj/vm2/LuaValue;Lorg/luaj/vm2/LuaValue;)V", false);

				visitor.visitJumpInsn(GOTO, loop);

				visitor.visitLabel(exit);
				nodes.accept(visitor);
			}
		}.onMethod("wrapLuaObject").once().mustFind();
	}
}
