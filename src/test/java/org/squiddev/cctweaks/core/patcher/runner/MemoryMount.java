package org.squiddev.cctweaks.core.patcher.runner;

import dan200.computercraft.api.filesystem.IWritableMount;

import java.io.*;
import java.util.*;

/**
 * Mounts in memory
 */
public class MemoryMount implements IWritableMount {
	private final Map<String, byte[]> files = new HashMap<String, byte[]>();
	private final Set<String> directories = new HashSet<String>();

	public MemoryMount() {
		directories.add("");
	}


	@Override
	public void makeDirectory(String s) throws IOException {
		File file = new File(s);
		while (file != null) {
			directories.add(file.getPath());
			file = file.getParentFile();
		}
	}

	@Override
	public void delete(String path) throws IOException {
		if (files.containsKey(path)) {
			files.remove(path);
		} else {
			directories.remove(path);
			for (String file : files.keySet().toArray(new String[files.size()])) {
				if (file.startsWith(path)) {
					files.remove(file);
				}
			}

			File parent = new File(path).getParentFile();
			if (parent != null) delete(parent.getPath());
		}
	}

	@Override
	public OutputStream openForWrite(final String s) throws IOException {
		return new ByteArrayOutputStream() {
			@Override
			public void close() throws IOException {
				super.close();
				files.put(s, toByteArray());
			}
		};
	}

	@Override
	public OutputStream openForAppend(final String s) throws IOException {
		ByteArrayOutputStream stream = new ByteArrayOutputStream() {
			@Override
			public void close() throws IOException {
				super.close();
				files.put(s, toByteArray());
			}
		};

		byte[] current = files.get(s);
		if (current != null) stream.write(current);

		return stream;
	}

	@Override
	public long getRemainingSpace() throws IOException {
		return 1000000L;
	}

	@Override
	public boolean exists(String s) throws IOException {
		return files.containsKey(s) || directories.contains(s);
	}

	@Override
	public boolean isDirectory(String s) throws IOException {
		return directories.contains(s);
	}

	@Override
	public void list(String s, List<String> list) throws IOException {
		for (String file : files.keySet()) {
			if (file.startsWith(s)) list.add(file);
		}
	}

	@Override
	public long getSize(String s) throws IOException {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public InputStream openForRead(String s) throws IOException {
		return new ByteArrayInputStream(files.get(s));
	}

	public MemoryMount addFile(String file, String contents) {
		files.put(file, contents.getBytes());
		return this;
	}
}
