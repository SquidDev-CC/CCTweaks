package org.squiddev.cctweaks.core.patcher;

import org.junit.Test;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.core.asm.WhitelistGlobals;

import java.util.HashSet;

public class WhitelistGlobalsTest {

	@Test
	public void assertWorks() throws Throwable {
		RewriteClassLoader loader = new RewriteClassLoader(new WhitelistGlobals());
		Config.globalWhitelist = new HashSet<String>();
		Config.globalWhitelist.add("debug");

		loader.run("assert.assert(debug, 'Expected debug API')");
	}
}
