package org.squiddev.cctweaks.core.asm.patch.classes.patch;

import org.squiddev.cctweaks.core.asm.patch.MergeVisitor;

/**
 * Tests {@link org.squiddev.cctweaks.core.asm.patch.MergeVisitor.Stub}
 */
public class MethodStub extends Base {
	@Override
	@MergeVisitor.Stub
	public String getName() {
		return "Called a Stub";
	}

	@MergeVisitor.Stub
	public String getPrivateName() {
		return "Called the private stub";
	}
}
