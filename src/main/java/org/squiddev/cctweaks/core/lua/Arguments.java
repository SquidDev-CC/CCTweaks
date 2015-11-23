package org.squiddev.cctweaks.core.lua;

import dan200.computercraft.api.lua.LuaException;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.squiddev.cctweaks.api.lua.IArguments;

public class Arguments implements IArguments {
	private final Varargs args;

	public Arguments(Varargs args) {
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
			throw new LuaException("Expected number for argument " + (index + 1));
		}
	}

	@Override
	public boolean getBoolean(int index) throws LuaException {
		LuaValue value = args.arg(index + 1);
		if (value.isboolean()) {
			return value.toboolean();
		} else {
			throw new LuaException("Expected boolean for argument " + (index + 1));
		}
	}

	@Override
	public String getString(int index) throws LuaException {
		LuaValue value = args.arg(index + 1);
		if (value instanceof LuaString) {
			return value.toString();
		} else {
			throw new LuaException("Expected string for argument " + (index + 1));
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
			throw new LuaException("Expected string for argument " + (index + 1));
		}
	}

	@Override
	public Object getArgument(int index, boolean binary) {
		return LuaConverter.toObject(args.arg(index + 1), binary);
	}

	@Override
	public Object[] asArguments() {
		return LuaConverter.toObjects(args, 0, false);
	}

	@Override
	public Object[] asBinary() {
		return LuaConverter.toObjects(args, 0, true);
	}

	@Override
	public IArguments subArgs(int offset) {
		return new Arguments(args.subargs(offset));
	}
}
