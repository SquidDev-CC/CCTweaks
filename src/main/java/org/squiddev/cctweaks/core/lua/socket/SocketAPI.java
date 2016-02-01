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
import org.squiddev.cctweaks.api.lua.ILuaAPI;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.core.utils.DebugLogger;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;

public class SocketAPI implements ILuaAPI {
	protected final HashSet<SocketConnection> connections = new HashSet<SocketConnection>();

	@Override
	public void startup() {
	}

	@Override
	public void shutdown() {
		for (SocketConnection connection : connections) {
			try {
				connection.close(false);
			} catch (IOException e) {
				DebugLogger.error("Error closing socket", e);
			}
		}

		connections.clear();
	}

	@Override
	public void advance(double timestep) {
	}

	@Override
	public String[] getMethodNames() {
		return new String[]{"connect"};
	}

	@Override
	public Object[] callMethod(ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException {
		switch (method) {
			case 0: {
				if (arguments.length == 0 || !(arguments[0] instanceof String)) {
					throw new LuaException("Expected string");
				}
				String address = (String) arguments[0];
				int port = -1;

				if (arguments.length >= 2) {
					Object val = arguments[1];
					if (val instanceof Number) {
						port = ((Number) val).intValue();
					} else {
						throw new LuaException("Expected string, number");
					}
				}

				if (!Config.APIs.Socket.enabled) throw new LuaException("TCP connections are disabled");
				if (connections.size() >= Config.APIs.Socket.maxTcpConnections) {
					throw new LuaException("Too many open connections");
				}

				try {
					SocketConnection connection = new SocketConnection(this, checkUri(address, port), port);
					connections.add(connection);
					return new Object[]{connection};
				} catch (IOException e) {
					throw new LuaException(e.getMessage());
				}
			}
			default:
				return null;
		}
	}

	private URI checkUri(String address, int port) throws LuaException {
		try {
			URI parsed = new URI(address);
			if (parsed.getHost() != null && (parsed.getPort() > 0 || port > 0)) {
				return parsed;
			}
		} catch (URISyntaxException ignored) {
		}

		try {
			URI simple = new URI("oc://" + address);
			if (simple.getHost() != null) {
				if (simple.getPort() > 0) {
					return simple;
				} else if (port > 0) {
					return new URI(simple.toString() + ":" + port);
				}
			}
		} catch (URISyntaxException ignored) {
		}

		throw new LuaException("Address could not be parsed or no valid port given");
	}
}
