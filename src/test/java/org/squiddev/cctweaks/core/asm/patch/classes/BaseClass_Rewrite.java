package org.squiddev.cctweaks.core.asm.patch.classes;

public class BaseClass_Rewrite {
	public String renamedMessage = "Bar";

	public String getMessage() {
		return renamedMessage + "_Rewrite";
	}
}
