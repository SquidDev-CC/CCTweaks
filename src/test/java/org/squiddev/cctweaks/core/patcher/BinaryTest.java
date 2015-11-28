package org.squiddev.cctweaks.core.patcher;

import org.junit.BeforeClass;
import org.junit.Test;
import org.squiddev.cctweaks.core.asm.binary.BinaryUtils;
import org.squiddev.cctweaks.core.patcher.utils.TryCatch;
import org.squiddev.patcher.transformer.ClassMerger;
import org.squiddev.patcher.transformer.TransformationChain;

public class BinaryTest {
	private static RewriteClassLoader loader;

	@BeforeClass
	public static void beforeClass() {
		TransformationChain chain = BinaryUtils.inject(new TransformationChain());

		// Peripheral API fixes aren't part of the main binary toolchain
		chain.add(new ClassMerger(
			"dan200.computercraft.core.apis.PeripheralAPI",
			"org.squiddev.cctweaks.core.patch.PeripheralAPI_Patch"
		));
		chain.add(new TryCatch("dan200.computercraft.core.lua.LuaJLuaMachine"));
		loader = new RewriteClassLoader(chain)
			.addPrefixes("org.squiddev.cctweaks.core.lua.")
			.addPrefixes("org.squiddev.cctweaks.api.");
	}

	@Test
	public void queueEvent() throws Throwable {
		loader.runFile("binaryEvent");
	}

	@Test
	public void fileSystem() throws Throwable {
		loader.runFile("binaryFS");
	}

	@Test
	public void fileSystemDirect() throws Throwable {
		loader.runClass("runner.WrappedTests", "fsAPI");
	}
}
