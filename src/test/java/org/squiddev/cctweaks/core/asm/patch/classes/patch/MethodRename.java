package org.squiddev.cctweaks.core.asm.patch.classes.patch;

import org.squiddev.cctweaks.core.asm.patch.MergeVisitor;

/**
 * Used to test {@link org.squiddev.cctweaks.core.asm.patch.MergeVisitor.Rename} on methods
 */
public class MethodRename extends Base {
	@MergeVisitor.Rename(to = "getName")
	public String anotherGetName() {
		return parentGetName() + "Bar";
	}

	@MergeVisitor.Rename(from = "getName")
	@MergeVisitor.Stub
	public String parentGetName() {
		return "Foo";
	}
}
