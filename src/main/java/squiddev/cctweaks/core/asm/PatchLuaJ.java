package squiddev.cctweaks.core.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import squiddev.cctweaks.core.reference.Config;
import squiddev.cctweaks.core.utils.DebugLogger;

import static org.objectweb.asm.Opcodes.*;

/**
 * LuaJ related patches
 */
public class PatchLuaJ {
	protected static final String DEBUG_INFO = "org/luaj/vm2/lib/DebugLib$DebugInfo";
	protected static final String IGETSOURCE = "org/luaj/vm2/luajc/IGetSource";
	protected static final String IGETSOURCE_TYPE = "L" + IGETSOURCE + ";";

	/**
	 * Patch the Debug Library
	 * TODO: Make this work better than it does. Use ChickenLib or something
	 *
	 * @param bytes The bytes of the {@link org.luaj.vm2.lib.DebugLib.DebugInfo} class
	 * @return Reformatted bytes
	 */
	public static byte[] patchDebugLib(byte[] bytes) {
		if (!Config.config.luaJC) return bytes;

		// This is semi-auto generate code from the CCStudio patch
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);

		classNode.visitField(0, "getSource", IGETSOURCE_TYPE, null, null).visitEnd();

		for (MethodNode method : classNode.methods) {
			if (method.name.equals("sourceline") && method.desc.equals("()Ljava/lang/String;")) {
				method.instructions.clear();
				method.localVariables = null;

				method.visitVarInsn(ALOAD, 0);
				method.visitFieldInsn(GETFIELD, "org/luaj/vm2/lib/DebugLib$DebugInfo", "closure", "Lorg/luaj/vm2/LuaClosure;");
				Label l0 = new Label();
				method.visitJumpInsn(IFNONNULL, l0);
				method.visitVarInsn(ALOAD, 0);
				method.visitFieldInsn(GETFIELD, "org/luaj/vm2/lib/DebugLib$DebugInfo", "getSource", "Lorg/luaj/vm2/luajc/IGetSource;");
				Label l1 = new Label();
				method.visitJumpInsn(IFNULL, l1);
				method.visitTypeInsn(NEW, "java/lang/StringBuilder");
				method.visitInsn(DUP);
				method.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
				method.visitVarInsn(ALOAD, 0);
				method.visitFieldInsn(GETFIELD, "org/luaj/vm2/lib/DebugLib$DebugInfo", "getSource", "Lorg/luaj/vm2/luajc/IGetSource;");
				method.visitMethodInsn(INVOKEINTERFACE, "org/luaj/vm2/luajc/IGetSource", "getSource", "()Ljava/lang/String;", true);
				method.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
				method.visitLdcInsn(":");
				method.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
				method.visitVarInsn(ALOAD, 0);
				method.visitFieldInsn(GETFIELD, "org/luaj/vm2/lib/DebugLib$DebugInfo", "getSource", "Lorg/luaj/vm2/luajc/IGetSource;");
				method.visitMethodInsn(INVOKEINTERFACE, "org/luaj/vm2/luajc/IGetSource", "getLine", "()I", true);
				method.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(I)Ljava/lang/StringBuilder;", false);
				method.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
				method.visitInsn(ARETURN);
				method.visitLabel(l1);
				method.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
				method.visitVarInsn(ALOAD, 0);
				method.visitFieldInsn(GETFIELD, "org/luaj/vm2/lib/DebugLib$DebugInfo", "func", "Lorg/luaj/vm2/LuaValue;");
				method.visitMethodInsn(INVOKEVIRTUAL, "org/luaj/vm2/LuaValue", "tojstring", "()Ljava/lang/String;", false);
				method.visitInsn(ARETURN);
				method.visitLabel(l0);
				method.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
				method.visitVarInsn(ALOAD, 0);
				method.visitFieldInsn(GETFIELD, "org/luaj/vm2/lib/DebugLib$DebugInfo", "closure", "Lorg/luaj/vm2/LuaClosure;");
				method.visitFieldInsn(GETFIELD, "org/luaj/vm2/LuaClosure", "p", "Lorg/luaj/vm2/Prototype;");
				method.visitFieldInsn(GETFIELD, "org/luaj/vm2/Prototype", "source", "Lorg/luaj/vm2/LuaString;");
				method.visitMethodInsn(INVOKEVIRTUAL, "org/luaj/vm2/LuaString", "tojstring", "()Ljava/lang/String;", false);
				method.visitVarInsn(ASTORE, 1);
				method.visitVarInsn(ALOAD, 0);
				method.visitMethodInsn(INVOKEVIRTUAL, "org/luaj/vm2/lib/DebugLib$DebugInfo", "currentline", "()I", false);
				method.visitVarInsn(ISTORE, 2);
				method.visitTypeInsn(NEW, "java/lang/StringBuilder");
				method.visitInsn(DUP);
				method.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
				method.visitVarInsn(ALOAD, 1);
				method.visitLdcInsn("@");
				method.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "startsWith", "(Ljava/lang/String;)Z", false);
				Label l2 = new Label();
				method.visitJumpInsn(IFNE, l2);
				method.visitVarInsn(ALOAD, 1);
				method.visitLdcInsn("=");
				method.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "startsWith", "(Ljava/lang/String;)Z", false);
				Label l3 = new Label();
				method.visitJumpInsn(IFEQ, l3);
				method.visitLabel(l2);
				method.visitFrame(Opcodes.F_FULL, 3, new Object[]{"org/luaj/vm2/lib/DebugLib$DebugInfo", "java/lang/String", Opcodes.INTEGER}, 1, new Object[]{"java/lang/StringBuilder"});
				method.visitVarInsn(ALOAD, 1);
				method.visitInsn(ICONST_1);
				method.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "substring", "(I)Ljava/lang/String;", false);
				Label l4 = new Label();
				method.visitJumpInsn(GOTO, l4);
				method.visitLabel(l3);
				method.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[]{"java/lang/StringBuilder"});
				method.visitVarInsn(ALOAD, 1);
				method.visitLabel(l4);
				method.visitFrame(Opcodes.F_FULL, 3, new Object[]{"org/luaj/vm2/lib/DebugLib$DebugInfo", "java/lang/String", Opcodes.INTEGER}, 2, new Object[]{"java/lang/StringBuilder", "java/lang/String"});
				method.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
				method.visitLdcInsn(":");
				method.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
				method.visitVarInsn(ILOAD, 2);
				method.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(I)Ljava/lang/StringBuilder;", false);
				method.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
				method.visitInsn(ARETURN);
			} else if (method.name.equals("setfunction") && method.desc.equals("(Lorg/luaj/vm2/LuaValue;)V")) {
				method.instructions.clear();
				method.localVariables = null;

				method.visitVarInsn(ALOAD, 0);
				method.visitVarInsn(ALOAD, 1);
				method.visitFieldInsn(PUTFIELD, DEBUG_INFO, "func", "Lorg/luaj/vm2/LuaValue;");
				method.visitVarInsn(ALOAD, 0);
				method.visitVarInsn(ALOAD, 1);
				method.visitTypeInsn(INSTANCEOF, "org/luaj/vm2/LuaClosure");
				Label l0 = new Label();
				method.visitJumpInsn(IFEQ, l0);
				method.visitVarInsn(ALOAD, 1);
				method.visitTypeInsn(CHECKCAST, "org/luaj/vm2/LuaClosure");
				Label l1 = new Label();
				method.visitJumpInsn(GOTO, l1);
				method.visitLabel(l0);
				method.visitFrame(F_SAME1, 0, null, 1, new Object[]{DEBUG_INFO});
				method.visitInsn(ACONST_NULL);
				method.visitLabel(l1);
				method.visitFrame(F_FULL, 2, new Object[]{DEBUG_INFO, "org/luaj/vm2/LuaValue"}, 2, new Object[]{DEBUG_INFO, "org/luaj/vm2/LuaClosure"});
				method.visitFieldInsn(PUTFIELD, DEBUG_INFO, "closure", "Lorg/luaj/vm2/LuaClosure;");
				method.visitVarInsn(ALOAD, 0);
				method.visitVarInsn(ALOAD, 1);
				method.visitTypeInsn(INSTANCEOF, IGETSOURCE);
				Label l2 = new Label();
				method.visitJumpInsn(IFEQ, l2);
				method.visitVarInsn(ALOAD, 1);
				method.visitTypeInsn(CHECKCAST, IGETSOURCE);
				Label l3 = new Label();
				method.visitJumpInsn(GOTO, l3);
				method.visitLabel(l2);
				method.visitFrame(F_SAME1, 0, null, 1, new Object[]{DEBUG_INFO});
				method.visitInsn(ACONST_NULL);
				method.visitLabel(l3);
				method.visitFrame(F_FULL, 2, new Object[]{DEBUG_INFO, "org/luaj/vm2/LuaValue"}, 2, new Object[]{DEBUG_INFO, IGETSOURCE});
				method.visitFieldInsn(PUTFIELD, DEBUG_INFO, "getSource", IGETSOURCE_TYPE);
				method.visitInsn(RETURN);
			}
		}

		DebugLogger.debug("Inject extra methods into DebugLib$DebugInfo");

		// Something breaks
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classNode.accept(writer);
		return writer.toByteArray();
	}
}
