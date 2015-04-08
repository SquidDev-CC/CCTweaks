package org.squiddev.cctweaks.core.asm.patch;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Collection of class visitors to use
 */
public class Visitors {
	/**
	 * Renames types based on their prefix
	 */
	public static class PrefixRemapper extends Remapper {
		public final String originalType;

		public final String newType;

		public PrefixRemapper(String originalType, String newType) {
			this.originalType = originalType;
			this.newType = newType;
		}

		/**
		 * Map type name to the new name.
		 *
		 * @param typeName Name of the type
		 */
		@Override
		public String map(String typeName) {
			if (typeName == null) return null;

			return typeName.replace(originalType, newType);
		}
	}

	/**
	 * Merge two classes together
	 */
	public static class MergeVisitor extends ClassVisitor {
		private final ClassNode override;

		private Set<String> fields = new HashSet<String>();
		private Set<String> methods = new HashSet<String>();

		/**
		 * Merge two classes together.
		 * This will not rename properties, that should be handled in a {@link Remapper}
		 *
		 * @param cv       The visitor to write to
		 * @param override The node that holds override methods
		 */
		public MergeVisitor(ClassVisitor cv, ClassNode override) {
			super(Opcodes.ASM5, cv);
			this.override = override;
		}

		/**
		 * Merge two classes together.
		 * This will not rename properties, that should be handled in a {@link Remapper}
		 *
		 * @param cv       The visitor to write to
		 * @param override The class reader that holds override properties
		 */
		public MergeVisitor(ClassVisitor cv, ClassReader override) {
			this(cv, makeNode(override));
		}

		private static ClassNode makeNode(ClassReader reader) {
			ClassNode node = new ClassNode();
			reader.accept(node, ClassReader.EXPAND_FRAMES);
			return node;
		}

		@Override
		public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
			// Merge both interfaces
			Set<String> overrideInterfaces = new HashSet<String>();
			overrideInterfaces.addAll(override.interfaces);
			Collections.addAll(overrideInterfaces, interfaces);

			interfaces = overrideInterfaces.toArray(new String[overrideInterfaces.size()]);
			super.visit(override.version, override.access, name, signature, superName, interfaces);

			for (FieldNode field : override.fields) {
				field.accept(this);
			}

			for (MethodNode method : override.methods) {
				if (!method.name.equals("<init>") && !method.name.equals("<cinit>")) {
					method.accept(this);
				}
			}
		}

		@Override
		public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
			if (fields.add(name)) {
				return super.visitField(access, name, desc, signature, value);
			}

			return null;
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
			if (methods.add(name + "|" + desc)) {
				return super.visitMethod(access, name, desc, signature, exceptions);
			}

			return null;
		}
	}

}
