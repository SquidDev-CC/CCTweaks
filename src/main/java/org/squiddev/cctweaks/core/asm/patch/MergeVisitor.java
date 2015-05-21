package org.squiddev.cctweaks.core.asm.patch;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.squiddev.cctweaks.core.Config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Modifier;
import java.util.*;

import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.squiddev.cctweaks.core.asm.patch.AnnotationHelper.*;

/**
 * Merge two classes together
 */
public class MergeVisitor extends ClassVisitor {
	private final ClassNode node;

	private final Set<String> visited = new HashSet<String>();

	private final Map<String, Integer> access = new HashMap<String, Integer>();

	private final Map<String, String> memberNames = new HashMap<String, String>();

	private RenameContext context;

	protected boolean writingOverride = false;
	protected String superClass = null;

	/**
	 * Merge two classes together.
	 *
	 * @param cv      The visitor to write to
	 * @param node    The node that holds override methods
	 * @param context Mapper for override classes to new ones
	 */
	public MergeVisitor(ClassVisitor cv, ClassNode node, RenameContext context) {
		super(Opcodes.ASM5);
		this.cv = new RemappingClassAdapter(cv, context);
		this.node = node;
		this.context = context;
		populateRename();
	}

	/**
	 * Merge two classes together.
	 *
	 * @param cv      The visitor to write to
	 * @param node    The class reader that holds override properties
	 * @param context Mapper for override classes to new ones
	 */
	public MergeVisitor(ClassVisitor cv, ClassReader node, RenameContext context) {
		this(cv, makeNode(node), context);
	}

	/**
	 * Helper method to make a {@link ClassNode}
	 *
	 * @param reader The class reader to make a node
	 * @return The created node
	 */
	private static ClassNode makeNode(ClassReader reader) {
		ClassNode node = new ClassNode();
		reader.accept(node, ClassReader.EXPAND_FRAMES);
		return node;
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		if (hasAnnotation(node, STUB)) {
			// If we are a stub, visit normally
			super.visit(version, access, name, signature, superName, interfaces);
		} else if (hasAnnotation(node, REWRITE)) {
			// If we are a total rewrite, then visit the overriding class
			node.accept(cv);

			// And prevent writing the normal one
			cv = null;
		} else {
			// Merge both interfaces
			Set<String> overrideInterfaces = new HashSet<String>();
			for (String inter : node.interfaces) {
				overrideInterfaces.add(context.mapType(inter));
			}
			Collections.addAll(overrideInterfaces, interfaces);
			interfaces = overrideInterfaces.toArray(new String[overrideInterfaces.size()]);

			writingOverride = true;
			superClass = superName;

			super.visit(node.version, node.access, name, node.signature, superName, interfaces);

			// Visit fields
			for (FieldNode field : node.fields) {
				if (field.name.equals(ANNOTATION)) continue;

				if (hasAnnotation(field.invisibleAnnotations, STUB)) {
					this.access.put(field.name, field.access);
				} else if (hasAnnotation(field.invisibleAnnotations, REMOVE) && Config.config.strictMode) {
					visited.add(getMap(this.memberNames, field.name, field.name));
				} else {
					List<String> renameTo = getAnnotationValue(getAnnotation(field.invisibleAnnotations, RENAME), "to");
					if (renameTo == null) {
						field.accept(this);
					} else {
						for (String to : renameTo) {
							field.name = to;
							field.accept(this);
						}
					}
				}
			}

			// Prepare field renames
			for (FieldNode field : node.fields) {
				List<String> renameFrom = getAnnotationValue(getAnnotation(field.invisibleAnnotations, RENAME), "from");
				if (renameFrom != null) {
					for (String from : renameFrom) {
						memberNames.put(from, field.name);
					}
				}
			}

			// Visit methods
			for (MethodNode method : node.methods) {
				if (method.name.equals("<init>") || method.name.equals("<cinit>")) continue;
				;

				if (hasAnnotation(method.invisibleAnnotations, STUB)) {
					this.access.put(method.name + "(" + context.mapMethodDesc(method.desc) + ")", method.access);
				} else if (hasAnnotation(method.invisibleAnnotations, REMOVE) && Config.config.strictMode) {
					String description = "(" + context.mapMethodDesc(method.desc) + ")";
					visited.add(getMap(this.memberNames, method.name + description, method.name) + description);
				} else {
					List<String> renameFrom = getAnnotationValue(getAnnotation(method.invisibleAnnotations, RENAME), "to");
					if (renameFrom == null) {
						method.accept(this);
					} else {
						for (String to : renameFrom) {
							method.name = to;
							method.accept(this);
						}
					}
				}
			}

			// Prepare method renames
			for (MethodNode method : node.methods) {
				List<String> renameTo = getAnnotationValue(getAnnotation(method.invisibleAnnotations, RENAME), "from");
				if (renameTo != null) {
					for (String from : renameTo) {
						memberNames.put(from + "(" + context.mapMethodDesc(method.desc) + ")", method.name);
					}
				}
			}

			writingOverride = false;
		}
	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		// Allows overriding access types
		access = getMap(this.access, name, access);
		name = getMap(this.memberNames, name, name);

		if (visited.add(name)) {
			return super.visitField(access, name, desc, signature, value);
		}

		return null;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		String description = "(" + context.mapMethodDesc(desc) + ")";
		String wholeName = name + description;

		// Allows overriding access types
		access = getMap(this.access, wholeName, access);
		name = getMap(memberNames, wholeName, name);

		if (visited.add(name + description)) {
			MethodVisitor visitor = super.visitMethod(access, name, desc, signature, exceptions);

			// We remap super methods if the method is not static and we are writing the override methods
			if (visitor != null && !Modifier.isStatic(access) && writingOverride && superClass != null) {
				return new SuperMethodVisitor(api, visitor);
			}

			return visitor;
		}

		return null;
	}

	/**
	 * Adds to the rename context from the {@link MergeVisitor.Rename} annotation
	 */
	public void populateRename() {
		Map<String, Object> annotation = getAnnotation(node, RENAME);
		List<String> from = getAnnotationValue(annotation, "from");
		List<String> to = getAnnotationValue(annotation, "to");

		if (from != null && to != null && from.size() == to.size()) {
			for (int i = 0; i < from.size(); i++) {
				context.renames.put(from.get(i), to.get(i));
			}
		}
	}

	public static <T> T getMap(Map<String, T> map, String key, T def) {
		T result = map.get(key);
		return result == null ? def : result;
	}

	/**
	 * Visitor that remaps super calls
	 */
	public class SuperMethodVisitor extends MethodVisitor {
		public SuperMethodVisitor(int api, MethodVisitor mv) {
			super(api, mv);
		}

		@Override
		public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
			// If it is a constructor, or it is in the current class (private method)
			// we shouldn't remap to the base class
			// Reference: http://stackoverflow.com/questions/20382652/detect-super-word-in-java-code-using-bytecode
			if (opcode == INVOKESPECIAL && !name.equals("<init>") && owner.equals(node.superName)) {
				owner = superClass;
			}
			super.visitMethodInsn(opcode, owner, name, desc, itf);
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
	 * or rename method
	 */
	@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
	@Retention(RetentionPolicy.CLASS)
	public @interface Rename {
		/**
		 * List of types to map from from or method to rename from
		 */
		String[] from() default "";

		/**
		 * List of types to map to from or method this to
		 */
		String[] to() default "";
	}

	/**
	 * Strip this method whilst running in strict mode
	 *
	 * @see org.squiddev.cctweaks.core.Config.ConfigData#strictMode
	 */
	@Target({ElementType.FIELD, ElementType.METHOD})
	@Retention(RetentionPolicy.CLASS)
	public @interface Remove {
	}
}
