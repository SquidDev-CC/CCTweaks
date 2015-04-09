package org.squiddev.cctweaks.core.asm.patch.classes;

import org.squiddev.cctweaks.core.asm.patch.Visitors;

public class BaseClass_Patch extends BaseClass {
	@Override
	public String getMessage() {
		return message + "_Patch";
	}

	@Override
	public String getInternal() {
		return new Internal().get() + "_Patch";
	}

	@Visitors.Stub
	private class Internal {
		public String get() {
			return null;
		}
	}

	@Visitors.Rewrite
	public class PublicInternal {
		public void get(String a) {
		}
	}
}
