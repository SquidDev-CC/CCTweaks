package org.squiddev.cctweaks.core.lua.luaj;

import dan200.computercraft.api.lua.LuaException;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.squiddev.cctweaks.api.lua.IArguments;

public class LuaJArguments implements IArguments {
	private final Varargs args;

	public LuaJArguments(Varargs args) {
		this.args = args;
	}

	@Override
	public int size() {
		return args.narg();
	}

	@Override
	public double getNumber(int index) throws LuaException {
		LuaValue value = args.arg(index + 1);
		if (value.isnumber()) {
			return value.todouble();
		} else {
			throw new LuaException("Expected number");
		}
	}

	@Override
	public boolean getBoolean(int index) throws LuaException {
		LuaValue value = args.arg(index + 1);
		if (value.isboolean()) {
			return value.toboolean();
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
			byte[] result = new byte[string.m_length];
			System.arraycopy(string.m_bytes, string.m_offset, result, 0, string.m_length);
			return result;
		} else {
			throw new LuaException("Expected string");
		}
	}

	@Override
	public Object getArgumentBinary(int index) {
		return LuaJConverter.toObject(args.arg(index + 1), true);
	}

	@Override
	public Object getArgument(int index) {
		return LuaJConverter.toObject(args.arg(index + 1), false);
	}

	@Override
	public Object[] asArguments() {
		return LuaJConverter.toObjects(args, 1, false);
	}

	@Override
	public Object[] asBinary() {
		return LuaJConverter.toObjects(args, 1, true);
	}

	@Override
	public IArguments subArgs(int offset) {
		return new LuaJArguments(args.subargs(offset + 1));
	}
}
