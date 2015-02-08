package squiddev.cctweaks.core.asm;

import org.objectweb.asm.*;
import org.objectweb.asm.util.TraceClassVisitor;
import squiddev.cctweaks.core.reference.Config;
import squiddev.cctweaks.core.utils.DebugLogger;

import java.io.PrintWriter;
import java.io.StringWriter;

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
	 *
	 * @param bytes The bytes of the {@link org.luaj.vm2.lib.DebugLib.DebugInfo} class
	 * @return Reformatted bytes
	 */
	public static byte[] patchDebugLib(byte[] bytes) {
		if(!Config.config.luaJC) return bytes;

		// This is semi-auto generate code from the CCStudio patch
		ClassWriter writer = new ClassWriter(0);
		new ClassReader(bytes).accept(new DebugStateClassAdapter(writer), 0);

		writer.visitField(0, "getSource", IGETSOURCE_TYPE, null, null).visitEnd();

		DebugLogger.debug("Inject extra methods into DebugLib$DebugInfo");
		try {
			byte[] result = writer.toByteArray();
			AsmUtils.validateClass(result);

			StringWriter sWriter = new StringWriter();
			PrintWriter printWriter = new PrintWriter(sWriter);
			new ClassReader(result).accept(new TraceClassVisitor(printWriter), 0);
			DebugLogger.debug("Validation result: " + sWriter.toString());
			DebugLogger.debug("Validated");
			return result;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return bytes;
	}

	public static class DebugStateClassAdapter extends ClassVisitor {
		public DebugStateClassAdapter(ClassVisitor cv) {
			super(ASM5, cv);
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
			MethodVisitor visitor = cv.visitMethod(access, name, desc, signature, exceptions);
			if(visitor != null) {
				if(name.equals("sourceline") && desc.equals("()Ljava/lang/String;")) {
					return new DebugState_SourceLine_MethodAdapter(visitor);
				} else if(name.equals("setfunction") && desc.equals("(Lorg/luaj/vm2/LuaValue;)V")) {
					return new DebugState_SetFunction_MethodAdapter(visitor);
				}
			}
			return visitor;
		}

		public static class DebugState_SetFunction_MethodAdapter extends MethodVisitor {
			protected MethodVisitor _method;

			public DebugState_SetFunction_MethodAdapter(MethodVisitor mv) {
				super(ASM5);
				_method = mv;
			}

			@Override
			public void visitEnd() {
				/*
					void setfunction(LuaValue func) {
						this.func = func;
						this.closure = (func instanceof LuaClosure ? (LuaClosure) func : null);
						this.getSource = (func instanceof IGetSource ? (IGetSource) func : null);
					}
				*/
				MethodVisitor mv = _method;

				mv.visitCode();
				mv.visitVarInsn(ALOAD, 0);
				mv.visitVarInsn(ALOAD, 1);
				mv.visitFieldInsn(PUTFIELD, DEBUG_INFO, "func", "Lorg/luaj/vm2/LuaValue;");
				mv.visitVarInsn(ALOAD, 0);
				mv.visitVarInsn(ALOAD, 1);
				mv.visitTypeInsn(INSTANCEOF, "org/luaj/vm2/LuaClosure");
				Label l0 = new Label();
				mv.visitJumpInsn(IFEQ, l0);
				mv.visitVarInsn(ALOAD, 1);
				mv.visitTypeInsn(CHECKCAST, "org/luaj/vm2/LuaClosure");
				Label l1 = new Label();
				mv.visitJumpInsn(GOTO, l1);
				mv.visitLabel(l0);
				mv.visitFrame(F_SAME1, 0, null, 1, new Object[] {DEBUG_INFO});
				mv.visitInsn(ACONST_NULL);
				mv.visitLabel(l1);
				mv.visitFrame(F_FULL, 2, new Object[] {DEBUG_INFO, "org/luaj/vm2/LuaValue"}, 2, new Object[] {DEBUG_INFO, "org/luaj/vm2/LuaClosure"});
				mv.visitFieldInsn(PUTFIELD, DEBUG_INFO, "closure", "Lorg/luaj/vm2/LuaClosure;");
				mv.visitVarInsn(ALOAD, 0);
				mv.visitVarInsn(ALOAD, 1);
				mv.visitTypeInsn(INSTANCEOF, IGETSOURCE);
				Label l2 = new Label();
				mv.visitJumpInsn(IFEQ, l2);
				mv.visitVarInsn(ALOAD, 1);
				mv.visitTypeInsn(CHECKCAST, IGETSOURCE);
				Label l3 = new Label();
				mv.visitJumpInsn(GOTO, l3);
				mv.visitLabel(l2);
				mv.visitFrame(F_SAME1, 0, null, 1, new Object[]{DEBUG_INFO});
				mv.visitInsn(ACONST_NULL);
				mv.visitLabel(l3);
				mv.visitFrame(F_FULL, 2, new Object[] {DEBUG_INFO, "org/luaj/vm2/LuaValue"}, 2, new Object[] {DEBUG_INFO, IGETSOURCE});
				mv.visitFieldInsn(PUTFIELD, DEBUG_INFO, "getSource", IGETSOURCE_TYPE);
				mv.visitInsn(RETURN);
				mv.visitMaxs(2, 2);
				mv.visitEnd();
			}
		}

		public static class DebugState_SourceLine_MethodAdapter extends MethodVisitor {
			protected MethodVisitor _method;

			public DebugState_SourceLine_MethodAdapter(MethodVisitor mv) {
				super(ASM5);
				_method = mv;
			}

			@Override
			public void visitEnd() {
				/*
					if (closure == null) {
						if (getSource != null) {
							return getSource.getSource() + ":" + getSource.getLine();
						}
						return func.tojstring();
					}
					String s = closure.p.source.tojstring();
					int line = currentline();
					return (s.startsWith("@") || s.startsWith("=") ? s.substring(1) : s) + ":" + line;
				*/
				MethodVisitor mv = _method;

				mv.visitCode();
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, "squiddev/cctweaks/DebugLib$DebugInfo", "closure", "Lorg/luaj/vm2/LuaClosure;");
				Label l0 = new Label();
				mv.visitJumpInsn(IFNONNULL, l0);
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, "squiddev/cctweaks/DebugLib$DebugInfo", "getSource", "Lorg/luaj/vm2/luajc/IGetSource;");
				Label l1 = new Label();
				mv.visitJumpInsn(IFNULL, l1);
				mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
				mv.visitInsn(DUP);
				mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, "squiddev/cctweaks/DebugLib$DebugInfo", "getSource", "Lorg/luaj/vm2/luajc/IGetSource;");
				mv.visitMethodInsn(INVOKEINTERFACE, "org/luaj/vm2/luajc/IGetSource", "getSource", "()Ljava/lang/String;", true);
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
				mv.visitLdcInsn(":");
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, "squiddev/cctweaks/DebugLib$DebugInfo", "getSource", "Lorg/luaj/vm2/luajc/IGetSource;");
				mv.visitMethodInsn(INVOKEINTERFACE, "org/luaj/vm2/luajc/IGetSource", "getLine", "()I", true);
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(I)Ljava/lang/StringBuilder;", false);
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
				mv.visitInsn(ARETURN);
				mv.visitLabel(l1);
				mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, "squiddev/cctweaks/DebugLib$DebugInfo", "func", "Lorg/luaj/vm2/LuaValue;");
				mv.visitMethodInsn(INVOKEVIRTUAL, "org/luaj/vm2/LuaValue", "tojstring", "()Ljava/lang/String;", false);
				mv.visitInsn(ARETURN);
				mv.visitLabel(l0);
				mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, "squiddev/cctweaks/DebugLib$DebugInfo", "closure", "Lorg/luaj/vm2/LuaClosure;");
				mv.visitFieldInsn(GETFIELD, "org/luaj/vm2/LuaClosure", "p", "Lorg/luaj/vm2/Prototype;");
				mv.visitFieldInsn(GETFIELD, "org/luaj/vm2/Prototype", "source", "Lorg/luaj/vm2/LuaString;");
				mv.visitMethodInsn(INVOKEVIRTUAL, "org/luaj/vm2/LuaString", "tojstring", "()Ljava/lang/String;", false);
				mv.visitVarInsn(ASTORE, 1);
				mv.visitVarInsn(ALOAD, 0);
				mv.visitMethodInsn(INVOKEVIRTUAL, "squiddev/cctweaks/DebugLib$DebugInfo", "currentline", "()I", false);
				mv.visitVarInsn(ISTORE, 2);
				mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
				mv.visitInsn(DUP);
				mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
				mv.visitVarInsn(ALOAD, 1);
				mv.visitLdcInsn("@");
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "startsWith", "(Ljava/lang/String;)Z", false);
				Label l2 = new Label();
				mv.visitJumpInsn(IFNE, l2);
				mv.visitVarInsn(ALOAD, 1);
				mv.visitLdcInsn("=");
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "startsWith", "(Ljava/lang/String;)Z", false);
				Label l3 = new Label();
				mv.visitJumpInsn(IFEQ, l3);
				mv.visitLabel(l2);
				mv.visitFrame(Opcodes.F_FULL, 3, new Object[] {"squiddev/cctweaks/DebugLib$DebugInfo", "java/lang/String", Opcodes.INTEGER}, 1, new Object[] {"java/lang/StringBuilder"});
				mv.visitVarInsn(ALOAD, 1);
				mv.visitInsn(ICONST_1);
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "substring", "(I)Ljava/lang/String;", false);
				Label l4 = new Label();
				mv.visitJumpInsn(GOTO, l4);
				mv.visitLabel(l3);
				mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {"java/lang/StringBuilder"});
				mv.visitVarInsn(ALOAD, 1);
				mv.visitLabel(l4);
				mv.visitFrame(Opcodes.F_FULL, 3, new Object[] {"squiddev/cctweaks/DebugLib$DebugInfo", "java/lang/String", Opcodes.INTEGER}, 2, new Object[] {"java/lang/StringBuilder", "java/lang/String"});
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
				mv.visitLdcInsn(":");
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
				mv.visitVarInsn(ILOAD, 2);
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(I)Ljava/lang/StringBuilder;", false);
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
				mv.visitInsn(ARETURN);
				mv.visitMaxs(3, 3);
				mv.visitEnd();
			}
		}
	}
}
