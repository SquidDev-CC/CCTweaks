package org.squiddev.cctweaks.core.patch.binfs;

import dan200.computercraft.api.filesystem.IWritableMount;
import dan200.computercraft.core.filesystem.FileSystem;
import dan200.computercraft.core.filesystem.FileSystemException;
import dan200.computercraft.core.filesystem.IMountedFile;
import org.squiddev.patcher.visitors.MergeVisitor;

import java.io.*;
import java.util.Set;

@SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "SynchronizeOnNonFinalField"})
@MergeVisitor.Rename(
	from = "org/squiddev/cctweaks/core/patch/binfs/INormalFile",
	to = "dan200/computercraft/core/filesystem/IMountedFileNormal"
)
public class FileSystem_Patch extends FileSystem {
	@MergeVisitor.Stub
	private Set<IMountedFile> m_openFiles;

	public FileSystem_Patch(String rootLabel, IWritableMount rootMount) throws FileSystemException {
		super(rootLabel, rootMount);
	}

	@MergeVisitor.Stub
	private static String sanitizePath(String path) {
		return path;
	}

	@MergeVisitor.Stub
	private MountWrapper getMount(String path) throws FileSystemException {
		return new MountWrapper();
	}

	@MergeVisitor.Stub
	private class MountWrapper {
		public InputStream openForRead(String path) throws FileSystemException {
			return null;
		}

		public OutputStream openForWrite(String path) throws FileSystemException {
			return null;
		}

		public OutputStream openForAppend(String path) throws FileSystemException {
			return null;
		}
	}

	@MergeVisitor.Rename(to = "openForRead")
	public synchronized INormalFile openForRead_P(String path) throws FileSystemException {
		path = sanitizePath(path);
		MountWrapper mount = getMount(path);
		InputStream stream = mount.openForRead(path);
		if (stream != null) {
			final BufferedInputStream reader = new BufferedInputStream(stream);
			INormalFile file = new INormalFile() {
				@MergeVisitor.Rewrite
				protected boolean ANNOTATION;

				@Override
				public byte[] readLine() throws IOException {
					// FIXME: Is this the most efficient way?
					ByteArrayOutputStream buffer = new ByteArrayOutputStream(1024);
					int val;
					while ((val = reader.read()) != -1) {
						buffer.write(val);
					}

					return buffer.size() > 0 ? buffer.toByteArray() : null;
				}

				@Override
				public byte[] readAll() throws IOException {
					ByteArrayOutputStream buffer = new ByteArrayOutputStream(1024);
					int nRead;
					byte[] data = new byte[1024];
					while ((nRead = reader.read(data, 0, data.length)) != -1) {
						buffer.write(data, 0, nRead);
					}

					return buffer.toByteArray();
				}

				@Override
				public void write(byte[] data, int start, int length, boolean newLine) throws IOException {
					throw new UnsupportedOperationException();
				}

				@Override
				public void close() throws IOException {
					synchronized (m_openFiles) {
						m_openFiles.remove(this);
						reader.close();
					}
				}

				@Override
				public void flush() throws IOException {
					throw new UnsupportedOperationException();
				}
			};
			synchronized (m_openFiles) {
				this.m_openFiles.add(file);
			}
			return file;
		}
		return null;
	}

	@MergeVisitor.Rename(to = "openForWrite")
	public synchronized INormalFile openForWrite_P(String path, boolean append) throws FileSystemException {
		path = sanitizePath(path);
		MountWrapper mount = getMount(path);
		OutputStream stream = append ? mount.openForAppend(path) : mount.openForWrite(path);
		if (stream != null) {
			final BufferedOutputStream writer = new BufferedOutputStream(stream);
			INormalFile file = new INormalFile() {
				@MergeVisitor.Rewrite
				protected boolean ANNOTATION;

				@Override
				public byte[] readLine() throws IOException {
					throw new UnsupportedOperationException();
				}

				@Override
				public byte[] readAll() throws IOException {
					throw new UnsupportedOperationException();
				}

				@Override
				public void write(byte[] data, int start, int length, boolean newLine) throws IOException {
					writer.write(data, start, length);
					if (newLine) writer.write('\n');
				}

				@Override
				public void close() throws IOException {
					synchronized (m_openFiles) {
						m_openFiles.remove(this);
						writer.close();
					}
				}

				@Override
				public void flush() throws IOException {
					writer.flush();
				}
			};
			synchronized (m_openFiles) {
				m_openFiles.add(file);
			}
			return file;
		}
		return null;
	}
}
