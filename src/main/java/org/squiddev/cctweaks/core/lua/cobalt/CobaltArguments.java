package org.squiddev.cctweaks.core.lua.cobalt;

import dan200.computercraft.api.lua.LuaException;
import org.squiddev.cctweaks.api.lua.IArguments;
import org.squiddev.cobalt.LuaString;
import org.squiddev.cobalt.LuaValue;
import org.squiddev.cobalt.Varargs;

public class CobaltArguments implements IArguments {
	private final Varargs args;

	public CobaltArguments(Varargs args) {
		this.args = args;
	}

	@Override
	public int size() {
		return args.count();
	}

	@Override
	public double getNumber(int index) throws LuaException {
		LuaValue value = args.arg(index + 1);
		if (value.isNumber()) {
			return value.toDouble();
		} else {
			throw new LuaException("Expected number");
		}
	}

	@Override
	public boolean getBoolean(int index) throws LuaException {
		LuaValue value = args.arg(index + 1);
		if (value.isBoolean()) {
			return value.toBoolean();
		} else {
			throw new LuaException("Expected boolean");
		}
	}

	@Override
	public String getString(int index) throws LuaException {
		LuaValue value = args.arg(index + 1);
		if (value instanceof LuaString) {
			return value.toString();
		} else {
			throw new LuaException("Expected string");
		}
	}

	@Override
	public byte[] getStringBytes(int index) throws LuaException {
		LuaValue value = args.arg(index + 1);
		if (value instanceof LuaString) {
			LuaString string = (LuaString) value;
			if (string.offset == 0 && string.length == string.bytes.length) {
				return string.bytes;
			} else {
				byte[] result = new byte[string.length];
				System.arraycopy(string.bytes, string.offset, result, 0, string.length);
				return result;
			}
		} else {
			throw new LuaException("Expected string");
		}
	}

	@Override
	public Object getArgumentBinary(int index) {
		return CobaltConverter.toObject(args.arg(index + 1), true);
	}

	@Override
	public Object getArgument(int index) {
		return CobaltConverter.toObject(args.arg(index + 1), false);
	}

	@Override
	public Object[] asArguments() {
		return CobaltConverter.toObjects(args, 1, false);
	}

	@Override
	public Object[] asBinary() {
		return CobaltConverter.toObjects(args, 1, true);
	}

	@Override
	public IArguments subArgs(int offset) {
		return new CobaltArguments(args.subargs(offset + 1));
	}
}
