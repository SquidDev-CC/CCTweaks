package org.squiddev.cctweaks.core.patcher.utils;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.tree.*;
import org.squiddev.patcher.Logger;
import org.squiddev.patcher.transformer.IPatcher;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.objectweb.asm.Opcodes.*;

/**
 * Inserts a e.printStackTrace after every exception
 */
public class TryCatch implements IPatcher {
	private final Set<String> prefixes = new HashSet<String>();

	public TryCatch(String... prefixes) {
		Collections.addAll(this.prefixes, prefixes);
	}

	public TryCatch addPrefix(String prefix) {
		this.prefixes.add(prefix);
		return this;
	}

	@Override
	public boolean matches(String className) {
		for (String prefix : prefixes) {
			if (className.startsWith(prefix)) return true;
		}
		return false;
	}

	@Override
	public ClassVisitor patch(String className, final ClassVisitor delegate) throws Exception {
		final ClassNode node = new ClassNode();
		return new ClassVisitor(ASM5, node) {
			@Override
			public void visitEnd() {
				for (MethodNode method : node.methods) {
					for (TryCatchBlockNode tryCatch : method.tryCatchBlocks) {
						AbstractInsnNode insn = tryCatch.handler;
						insn = insn.getNext();
						if (insn instanceof LineNumberNode) insn = insn.getNext();

						if (insn instanceof FrameNode) {
							AbstractInsnNode insn1 = new InsnNode(DUP);
							AbstractInsnNode insn2 = new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Throwable", "printStackTrace", "()V", false);
							method.instructions.insert(insn, insn1);
							method.instructions.insert(insn1, insn2);
						} else {
							Logger.debug("Unexpected instruction " + insn.getOpcode());
						}
					}
				}

				node.accept(delegate);
			}
		};
	}
}
