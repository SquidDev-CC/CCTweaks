package org.squiddev.cctweaks.core.asm.patch.classes.patch;

import org.squiddev.cctweaks.core.asm.patch.MergeVisitor;

/**
 * Tests {@link org.squiddev.cctweaks.core.asm.patch.MergeVisitor.Stub} on classes
 */
public class ClassStub extends Base {
	@MergeVisitor.Stub
	private static class Foo {
		public String getName() {
			return "Created a Stub";
		}
	}

	@Override
	public String getName() {
		return new Foo().getName() + " Stub";
	}
}
