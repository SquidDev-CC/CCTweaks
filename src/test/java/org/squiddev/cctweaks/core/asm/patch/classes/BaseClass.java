package org.squiddev.cctweaks.core.asm.patch.classes;

public class BaseClass {
	public String message = "Foo";

	public String getMessage() {
		return message;
	}

	public String getInternal() {
		return new Internal().get();
	}

	private class Internal {
		public String get() {
			return "Foo";
		}
	}

	public class PublicInternal {
		public void get(int a) {
		}
	}
}
