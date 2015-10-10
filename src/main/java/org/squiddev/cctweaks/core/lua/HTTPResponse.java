package org.squiddev.cctweaks.core.lua;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.ILuaObject;
import dan200.computercraft.api.lua.LuaException;

import java.util.Arrays;

public class HTTPResponse implements ILuaObject {
	private final int responseCode;
	private final byte[] result;
	private int index = 0;
	private boolean closed = false;

	public HTTPResponse(int responseCode, byte[] result) {
		this.responseCode = responseCode;
		this.result = result;
	}

	@Override
	public String[] getMethodNames() {
		return new String[]{"readLine", "readAll", "read", "close", "getResponseCode"};
	}

	@Override
	public Object[] callMethod(ILuaContext context, int method, Object[] args) throws LuaException, InterruptedException {
		switch (method) {
			case 0: {
				// We have these as separate methods to ensure select('#', ...) works
				if (closed) return null;
				if (index >= result.length) return new Object[1];

				int start = index, end = -1, newIndex = -1;

				for (int i = start; i < result.length; i++) {
					// Yeah. Kinda ugly
					if (result[i] == '\r') {
						if (i + 1 < result.length && result[i + 1] == '\n') {
							end = i;
							newIndex = i + 2;
							break;
						} else {
							end = i;
							newIndex = i + 1;
							break;
						}
					} else if (result[i] == '\n') {
						end = i;
						newIndex = i + 1;
						break;
					}
				}

				if (end == -1) {
					// If we read until the end
					end = index = result.length;
				} else {
					index = newIndex;
				}

				// If we were at the end of the line then return empty string
				if (end < start) return new Object[]{""};
				return new Object[]{Arrays.copyOfRange(result, start, end)};
			}
			case 1:
				if (closed) return null;
				if (index >= result.length) return new Object[1];

				return new Object[]{Arrays.copyOfRange(result, index, result.length)};
			case 2: {
				if (closed) return null;
				if (index >= result.length) return new Object[1];

				byte character = result[index];
				index++;
				return new Object[]{character};
			}
			case 3:
				closed = true;
				break;
			case 4:
				return new Object[]{responseCode};
		}

		return null;
	}
}
