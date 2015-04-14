package org.squiddev.cctweaks.core.asm.patch;

import org.objectweb.asm.commons.Remapper;

import java.util.HashMap;
import java.util.Map;

/**
 * The remapper for a series of classes.
 * This is to help with inner classes which may wish to rename, etc...
 */
public class RenameContext extends Remapper {
	/**
	 * List of names to rename with
	 */
	public final Map<String, String> renames = new HashMap<String, String>();

	/**
	 * List of names to rename the prefix of
	 */
	public final Map<String, String> prefixRenames = new HashMap<String, String>();

	@Override
	public String map(String typeName) {
		String newName = renames.get(typeName);
		if (newName != null) typeName = newName;

		for (Map.Entry<String, String> prefix : prefixRenames.entrySet()) {
			String name = prefix.getKey();
			if (typeName.startsWith(name)) {
				return prefix.getValue() + typeName.substring(name.length());
			}
		}

		return typeName;
	}
}
