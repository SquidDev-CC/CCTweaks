package org.squiddev.cctweaks.core.asm.patch;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.*;

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
		public final static String STUB = Type.getDescriptor(Stub.class);
		public final static String REWRITE = Type.getDescriptor(Rewrite.class);

		private final ClassNode override;

		private Set<String> fields = new HashSet<String>();
		private Set<String> methods = new HashSet<String>();

		private Map<String, Integer> access = new HashMap<String, Integer>();

		private final Remapper mapper;

		/**
		 * Merge two classes together.
		 *
		 * @param cv       The visitor to write to
		 * @param override The node that holds override methods
		 * @param mapper   Mapper for override classes to new ones
		 */
		public MergeVisitor(ClassVisitor cv, ClassNode override, Remapper mapper) {
			super(Opcodes.ASM5, new RemappingClassAdapter(cv, mapper));
			this.override = override;
			this.mapper = mapper;
		}

		/**
		 * Merge two classes together.
		 *
		 * @param cv       The visitor to write to
		 * @param override The class reader that holds override properties
		 * @param mapper   Mapper for override classes to new ones
		 */
		public MergeVisitor(ClassVisitor cv, ClassReader override, Remapper mapper) {
			this(cv, makeNode(override), mapper);
		}

		private static ClassNode makeNode(ClassReader reader) {
			ClassNode node = new ClassNode();
			reader.accept(node, ClassReader.EXPAND_FRAMES);
			return node;
		}

		@Override
		public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
			if (hasAnnotation(override.invisibleAnnotations, STUB)) {
				super.visit(version, access, name, signature, superName, interfaces);
			} else if (hasAnnotation(override.invisibleAnnotations, REWRITE)) {
				override.accept(cv);
				cv = null; // Send no more requests
			} else {
				// Merge both interfaces
				Set<String> overrideInterfaces = new HashSet<String>();
				for (String inter : override.interfaces) {
					overrideInterfaces.add(mapper.mapType(inter));
				}
				Collections.addAll(overrideInterfaces, interfaces);

				interfaces = overrideInterfaces.toArray(new String[overrideInterfaces.size()]);

				super.visit(override.version, override.access, name, override.signature, superName, interfaces);

				for (FieldNode field : override.fields) {
					if (!hasAnnotation(field.invisibleAnnotations, STUB)) {
						field.accept(this);
					} else {
						this.access.put(field.name, field.access);
					}
				}

				for (MethodNode method : override.methods) {
					if (!method.name.equals("<init>") && !method.name.equals("<cinit>") && !hasAnnotation(method.invisibleAnnotations, STUB)) {
						method.accept(this);
					} else {
						this.access.put(method.name + "|" + mapper.mapMethodDesc(method.desc), method.access);
					}
				}
			}
		}

		@Override
		public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
			// Allows overriding access types
			Integer newAccess;
			if ((newAccess = this.access.get(name)) != null) {
				access = newAccess;
			}

			if (fields.add(name)) {
				return super.visitField(access, name, desc, signature, value);
			}

			return null;
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
			String wholeName = name + "|" + mapper.mapMethodDesc(desc);

			// Allows overriding access types
			Integer newAccess;
			if ((newAccess = this.access.get(wholeName)) != null) {
				access = newAccess;
			}

			if (methods.add(wholeName)) {
				return super.visitMethod(access, name, desc, signature, exceptions);
			}

			return null;
		}

		protected static boolean hasAnnotation(List<AnnotationNode> nodes, String name) {
			if (nodes == null) return false;
			for (AnnotationNode node : nodes) {
				if (node.desc.equals(name)) return true;
			}
			return false;
		}
	}

	/**
	 * Don't rewrite the original class
	 */
	@Target({ElementType.METHOD, ElementType.FIELD, ElementType.TYPE, ElementType.CONSTRUCTOR})
	@Retention(RetentionPolicy.CLASS)
	public @interface Stub {
	}

	/**
	 * Rewrite the original class instead of merging
	 */
	@Target({ElementType.TYPE})
	@Retention(RetentionPolicy.CLASS)
	public @interface Rewrite {
	}

}
