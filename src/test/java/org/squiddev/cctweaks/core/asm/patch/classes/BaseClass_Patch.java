package org.squiddev.cctweaks.core.asm.patch.classes;

public class BaseClass_Patch extends BaseClass {
	@Override
	public String getMessage() {
		return message + "_Patch";
	}
}
