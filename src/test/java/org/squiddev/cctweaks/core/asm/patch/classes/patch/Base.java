package org.squiddev.cctweaks.core.asm.patch.classes.patch;

/**
 * Base class for all rewrites
 */
@SuppressWarnings("unused")
public class Base extends VeryBase {
	private static class Foo {
		public String getName() {
			return "Foo";
		}
	}

	private static class Bar {
		public String getName() {
			return "Bar";
		}
	}

	@Override
	public String getName() {
		return new Foo().getName();
	}

	private void onlyExistsToMakeSureBarHasSameConstructorAsFoo() {
		new Bar().getName();
	}

	private String getPrivateName() {
		return "Private";
	}
}
