package org.squiddev.cctweaks.core.patch.binfs;

import dan200.computercraft.core.filesystem.IMountedFile;

import java.io.IOException;

/**
 * Override of {@link dan200.computercraft.core.filesystem.IMountedFileNormal}, with {@code byte[]} methods instead.
 */
public interface INormalFile extends IMountedFile {
	byte[] readLine() throws IOException;

	byte[] readAll() throws IOException;

	void write(byte[] data, int start, int length, boolean newLine) throws IOException;

	void flush() throws IOException;
}
