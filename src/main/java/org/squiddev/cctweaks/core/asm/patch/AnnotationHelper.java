package org.squiddev.cctweaks.core.asm.patch;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.squiddev.cctweaks.core.asm.patch.Visitors.*;

/**
 * Helpers for reading annotations from nodes
 */
public class AnnotationHelper {
	public final static String STUB = Type.getDescriptor(Stub.class);
	public final static String REWRITE = Type.getDescriptor(Rewrite.class);
	public final static String RENAME = Type.getDescriptor(Rename.class);
	public final static String NAME = Type.getDescriptor(Name.class);

	/**
	 * We use a field called {@code ANNOTATION} to store data about the class itself
	 */
	public final static String ANNOTATION = "ANNOTATION";

	/**
	 * Gets the annotation for a list of nodes
	 *
	 * @param nodes List of annotation nodes to search
	 * @param name  Name of the annotation to find
	 * @return Map of name to value annotations
	 */
	protected static Map<String, Object> getAnnotation(List<AnnotationNode> nodes, String name) {
		if (nodes == null) return null;
		for (AnnotationNode node : nodes) {
			if (node.desc.equals(name)) {
				Map<String, Object> result = new HashMap<String, Object>();
				if (node.values != null && node.values.size() > 0) {
					for (int i = 0; i < node.values.size(); i += 2) {
						result.put((String) node.values.get(i), node.values.get(1));
					}
				}

				return result;
			}
		}
		return null;
	}

	/**
	 * Gets the annotation for a class node
	 * If will also search in the ANNOTATION field
	 *
	 * @param node The class node
	 * @param name The name of the annotation
	 * @return Map of name to value annotations
	 */
	protected static Map<String, Object> getAnnotation(ClassNode node, String name) {
		Map<String, Object> annotation = getAnnotation(node.invisibleAnnotations, name);
		if (annotation != null) return annotation;

		for (FieldNode field : node.fields) {
			if (field.name.equals(ANNOTATION)) {
				return getAnnotation(field.invisibleAnnotations, name);
			}
		}

		return null;
	}

	/**
	 * Checks if a annotation is in a list of annotations
	 *
	 * @param nodes The list of annotations
	 * @param name  The name of the annotation
	 * @return If this item has the annotation
	 */
	protected static boolean hasAnnotation(List<AnnotationNode> nodes, String name) {
		return getAnnotation(nodes, name) != null;
	}

	/**
	 * Checks if a class node has a specific annotation.
	 * If will also search in the ANNOTATION field
	 *
	 * @param node The class node
	 * @param name The name of the annotation
	 * @return If this class has the annotation
	 */
	protected static boolean hasAnnotation(ClassNode node, String name) {
		return getAnnotation(node, name) != null;
	}
}
