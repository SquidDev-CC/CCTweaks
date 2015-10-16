package org.squiddev.cctweaks.core.patcher.utils;

import dan200.computercraft.api.filesystem.IMount;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

public class MountHelpers {
	public static final File mainJar;

	static {
		URL url = IMount.class.getProtectionDomain().getCodeSource().getLocation();
		File jar;
		try {
			jar = new File(url.toURI());
		} catch (URISyntaxException ignored) {
			jar = new File(url.getPath());
		}

		mainJar = jar;
	}
}
