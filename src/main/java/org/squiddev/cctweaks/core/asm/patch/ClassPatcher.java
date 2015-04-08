package org.squiddev.cctweaks.core.asm.patch;

import org.squiddev.cctweaks.core.utils.DebugLogger;

import java.io.IOException;
import java.io.InputStream;

/**
 * Finds classes in the /patch/ directory and uses
 * them instead
 */
public class ClassPatcher implements IPatcher {
	/**
	 * The root directory we are finding patches at
	 */
	public final String patchPath;

	/**
	 * Name of the class we are replacing
	 */
	public final String targetName;

	public ClassPatcher(String targetName, String patchPath) {
		if (!patchPath.endsWith("/")) patchPath += "/";

		this.patchPath = patchPath;
		this.targetName = targetName;
	}

	public ClassPatcher(String targetName) {
		this(targetName, "/patch/");
	}

	/**
	 * Checks if the class matches
	 *
	 * @param className The name of the class
	 * @return If it should be patched
	 */
	@Override
	public boolean matches(String className) {
		return className.startsWith(this.targetName);
	}

	/**
	 * Patches a class. Replaces className with {@link #patchPath}
	 *
	 * @param className The name of the class
	 * @param bytes     The original bytes to patch
	 * @return The patched bytes
	 */
	@Override
	public byte[] patch(String className, byte[] bytes) {
		String source = patchPath + className.replace('.', '/') + ".class";
		InputStream is = ClassPatcher.class.getResourceAsStream(source);

		if (is == null) {
			DebugLogger.error("Cannot find custom rewrite for " + className + " at " + source);
			return bytes;
		}

		try {
			// Read the byte array
			byte[] result = new byte[is.available()];
			int len = 0;
			while (true) {
				int n = is.read(result, len, result.length - len);
				if (n == -1) {
					if (len < result.length) {
						byte[] c = new byte[len];
						System.arraycopy(result, 0, c, 0, len);
						result = c;
					}
					return result;
				}
				len += n;
				if (len == result.length) {
					int last = is.read();
					if (last < 0) {
						DebugLogger.debug("Injected custom " + className);
						return result;
					}
					byte[] c = new byte[result.length + 1000];
					System.arraycopy(result, 0, c, 0, len);
					c[len++] = (byte) last;
					result = c;
				}
			}
		} catch (Exception e) {
			DebugLogger.error("Cannot replace " + className + ", falling back to default");
			e.printStackTrace();
			return bytes;
		} finally {
			try {
				is.close();
			} catch (IOException ignored) {
			}
		}
	}
}
