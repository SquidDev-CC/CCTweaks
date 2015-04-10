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
		public final static String RENAME = Type.getDescriptor(Rename.class);
		public final static String ANNOTATION = "ANNOTATION";

		private final ClassNode node;

		private Set<String> fields = new HashSet<String>();
		private Set<String> methods = new HashSet<String>();

		private Map<String, Integer> access = new HashMap<String, Integer>();

		private Remapper mapper;

		/**
		 * Merge two classes together.
		 *
		 * @param cv     The visitor to write to
		 * @param node   The node that holds override methods
		 * @param mapper Mapper for override classes to new ones
		 */
		public MergeVisitor(ClassVisitor cv, ClassNode node, Remapper mapper) {
			super(Opcodes.ASM5, new RemappingClassAdapter(cv, mapper = createRemapper(getAnnotation(node.invisibleAnnotations, RENAME), mapper)));
			this.node = node;
			this.mapper = mapper;
		}

		/**
		 * Merge two classes together.
		 *
		 * @param cv     The visitor to write to
		 * @param node   The class reader that holds override properties
		 * @param mapper Mapper for override classes to new ones
		 */
		public MergeVisitor(ClassVisitor cv, ClassReader node, Remapper mapper) {
			this(cv, makeNode(node), mapper);
		}

		private static ClassNode makeNode(ClassReader reader) {
			ClassNode node = new ClassNode();
			reader.accept(node, ClassReader.EXPAND_FRAMES);
			return node;
		}

		@Override
		public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
			if (hasAnnotation(node, STUB)) {
				super.visit(version, access, name, signature, superName, interfaces);
			} else if (hasAnnotation(node, REWRITE)) {
				node.accept(cv);
				cv = null; // Send no more requests
			} else {
				// Merge both interfaces
				Set<String> overrideInterfaces = new HashSet<String>();
				for (String inter : node.interfaces) {
					overrideInterfaces.add(mapper.mapType(inter));
				}
				Collections.addAll(overrideInterfaces, interfaces);

				interfaces = overrideInterfaces.toArray(new String[overrideInterfaces.size()]);

				super.visit(node.version, node.access, name, node.signature, superName, interfaces);

				for (FieldNode field : node.fields) {
					if (!hasAnnotation(field.invisibleAnnotations, STUB) && !field.name.equals(ANNOTATION)) {
						field.accept(this);
					} else {
						this.access.put(field.name, field.access);
					}
				}

				for (MethodNode method : node.methods) {
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

		protected static AnnotationNode getAnnotation(List<AnnotationNode> nodes, String name) {
			if (nodes == null) return null;
			for (AnnotationNode node : nodes) {
				if (node.desc.equals(name)) return node;
			}
			return null;
		}

		protected static boolean hasAnnotation(List<AnnotationNode> nodes, String name) {
			return getAnnotation(nodes, name) != null;
		}

		protected static boolean hasAnnotation(ClassNode node, String name) {
			if (hasAnnotation(node.invisibleAnnotations, name)) return true;

			for (FieldNode field : node.fields) {
				if (field.name.equals(ANNOTATION)) {
					return hasAnnotation(field.invisibleAnnotations, name);
				}
			}

			return false;
		}

		/**
		 * Creates a remapper from a {@link org.squiddev.cctweaks.core.asm.patch.Visitors.Rename} annotation
		 *
		 * @param annotation The annotation node
		 * @param mapper     The existing remapper (or null if none exists)
		 * @return The resulting remapper
		 */
		@SuppressWarnings("unchecked")
		public static Remapper createRemapper(AnnotationNode annotation, final Remapper mapper) {
			if (annotation != null && annotation.values.size() == 4) {
				final Map<String, String> renames = new HashMap<String, String>();

				List<String> from = (List<String>) annotation.values.get(1);
				List<String> to = (List<String>) annotation.values.get(3);

				if (annotation.values.get(0).equals("to")) {
					List<String> temp = from;
					from = to;
					to = temp;
				}

				for (int i = 0; i < from.size(); i++) {
					renames.put(from.get(i), to.get(i));
				}

				return new Remapper() {
					@Override
					public String map(String typeName) {
						if (mapper != null) typeName = mapper.map(typeName);

						String result = renames.get(typeName);
						if (result != null) return result;

						return typeName;
					}
				};
			}

			return mapper;
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
	 * Put on a field called ANNOTATION
	 */
	@Target({ElementType.TYPE, ElementType.FIELD})
	@Retention(RetentionPolicy.CLASS)
	public @interface Rewrite {
	}

	/**
	 * Rename a class inside
	 */
	@Target({ElementType.TYPE})
	@Retention(RetentionPolicy.CLASS)
	public @interface Rename {
		String[] from();

		String[] to();
	}

}
