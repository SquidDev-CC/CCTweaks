package org.squiddev.cctweaks.core.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.squiddev.cctweaks.core.reference.Config;

public class PatchTurtle implements Opcodes {
	/**
	 * Disable a particular turtle command
	 */
	public static byte[] disableTurtleCommand(String name, byte[] bytes) {
		if (!Config.turtleDisabledActions.contains(name.toLowerCase())) return bytes;

		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);

		for (MethodNode method : classNode.methods) {
			if (method.name.equals("execute") && method.desc.equals("(Ldan200/computercraft/api/turtle/ITurtleAccess;)Ldan200/computercraft/api/turtle/TurtleCommandResult;")) {
				method.instructions.clear();
				method.localVariables = null;

				method.visitLdcInsn("Action disabled");
				method.visitMethodInsn(INVOKESTATIC, "dan200/computercraft/api/turtle/TurtleCommandResult", "failure", "(Ljava/lang/String;)Ldan200/computercraft/api/turtle/TurtleCommandResult;", false);
				method.visitInsn(ARETURN);
			}
		}

		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		classNode.accept(writer);
		return writer.toByteArray();
	}
}
