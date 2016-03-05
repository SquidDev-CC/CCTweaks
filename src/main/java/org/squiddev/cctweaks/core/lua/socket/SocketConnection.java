/**
 * Copyright (c) 2013-2015 Florian "Sangar" Nücke
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.squiddev.cctweaks.core.lua.socket;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import org.squiddev.cctweaks.api.lua.IArguments;
import org.squiddev.cctweaks.api.lua.ILuaObjectWithArguments;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.core.lua.BinaryConverter;
import org.squiddev.cctweaks.core.lua.LuaHelpers;
import org.squiddev.cctweaks.core.utils.Helpers;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

public class SocketConnection implements ILuaObjectWithArguments {
	private static final ScheduledExecutorService threads = Helpers.createThread("Socket", Config.APIs.Socket.threads);

	private final SocketAPI owner;

	private Future<InetAddress> address;
	private SocketChannel channel;
	private boolean isResolved = false;

	public SocketConnection(SocketAPI owner, final URI uri, final int port) throws IOException {
		this.owner = owner;

		channel = SocketChannel.open();
		channel.configureBlocking(false);
		address = threads.submit(new Callable<InetAddress>() {

			@Override
			public InetAddress call() throws Exception {
				InetAddress resolved = InetAddress.getByName(uri.getHost());
				if (Config.socketBlacklist.active && Config.socketBlacklist.matches(resolved, uri.getHost())) {
					throw new LuaException("Address is blacklisted");
				}

				if (Config.socketWhitelist.active && !Config.socketWhitelist.matches(resolved, uri.getHost())) {
					throw new LuaException("Address is not whitelisted");
				}

				InetSocketAddress address = new InetSocketAddress(resolved, uri.getPort() == -1 ? port : uri.getPort());
				channel.connect(address);
				return resolved;
			}
		});
	}

	public void close(boolean remove) throws IOException {
		if (remove) owner.connections.remove(this);
		if (channel == null || address == null) throw new IOException("Already closed");

		address.cancel(true);
		address = null;

		channel.close();
		channel = null;
	}

	private boolean checkConnected() throws LuaException, InterruptedException {
		if (channel == null || address == null) throw new LuaException("Connection lost");
		try {
			if (isResolved) return channel.finishConnect();

			if (address.isCancelled()) {
				channel.close();
				throw new LuaException("Bad connection descriptor");
			}

			if (address.isDone()) {
				try {
					address.get();
				} catch (ExecutionException e) {
					throw LuaHelpers.rewriteException(e.getCause(), "Socket error");
				}
				isResolved = true;
				return channel.finishConnect();
			}

			return false;
		} catch (IOException e) {
			throw LuaHelpers.rewriteException(e, "Socket error");
		}
	}

	@Override
	public String[] getMethodNames() {
		return new String[]{"checkConnected", "close", "read", "write"};
	}

	private int write(byte[] contents) throws LuaException, InterruptedException {
		if (checkConnected()) {
			try {
				return channel.write(ByteBuffer.wrap(contents));
			} catch (IOException e) {
				throw LuaHelpers.rewriteException(e, "Socket error");
			}
		} else {
			return 0;
		}
	}

	private byte[] read(int count) throws LuaException, InterruptedException {
		count = Math.min(count, Config.APIs.Socket.maxRead);

		if (checkConnected()) {
			ByteBuffer buffer = ByteBuffer.allocate(count);
			try {
				int read = channel.read(buffer);
				if (read == -1) return null;

				return Arrays.copyOf(buffer.array(), read);
			} catch (IOException e) {
				throw LuaHelpers.rewriteException(e, "Socket error");
			}

		} else {
			return new byte[0];
		}
	}

	@Override
	public Object[] callMethod(ILuaContext context, int method, IArguments arguments) throws LuaException, InterruptedException {
		switch (method) {
			case 0:
				return new Object[]{checkConnected()};
			case 1:
				try {
					close(true);
				} catch (IOException e) {
					throw LuaHelpers.rewriteException(e, "Socket error");
				}
				return null;
			case 2: {
				int count = Integer.MAX_VALUE;
				Object argument = arguments.getArgument(0);
				if (argument instanceof Number) count = Math.max(0, ((Number) argument).intValue());

				byte[] contents = read(count);
				return new Object[]{contents};
			}
			case 3: {
				int written = write(arguments.getStringBytes(0));
				return new Object[]{written};
			}
			default:
				return null;
		}
	}

	@Override
	public Object[] callMethod(ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException {
		switch (method) {
			case 0:
				return new Object[]{checkConnected()};
			case 1:
				try {
					close(true);
				} catch (IOException e) {
					throw LuaHelpers.rewriteException(e, "Socket error");
				}
				return null;
			case 2: {
				int count = Integer.MAX_VALUE;
				if (arguments.length >= 1) {
					if (arguments[0] instanceof Number) {
						count = Math.max(0, ((Number) arguments[0]).intValue());
					} else {
						throw new LuaException("Expected number");
					}
				}

				byte[] contents = read(count);
				return new Object[]{contents};
			}
			case 3: {
				byte[] stream;
				if (arguments.length == 0) throw new LuaException("Expected string");

				Object argument = arguments[0];
				if (argument instanceof byte[]) {
					stream = (byte[]) argument;
				} else if (argument instanceof String) {
					stream = BinaryConverter.toBytes((String) argument);
				} else {
					throw new LuaException("Expected string");
				}

				int written = write(stream);
				return new Object[]{written};
			}
			default:
				return null;
		}
	}
}
