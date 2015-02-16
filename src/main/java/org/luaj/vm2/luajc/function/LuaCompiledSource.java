package org.luaj.vm2.luajc.function;

import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.luajc.IGetSource;

/**
 * A wrapper for functions to handle call stacks
 */
public class LuaCompiledSource extends LuaFunction implements IGetSource {
	protected final String source;
	public int line;
	protected final LuaFunction parent;

	public LuaCompiledSource(String source, int startLine, LuaFunction parent) {
		this.source = source;
		line = startLine;
		this.parent = parent;
	}

	@Override
	public String getSource() {
		return source;
	}

	@Override
	public int getLine() {
		return line;
	}

	public LuaValue getfenv() {
		return parent.getfenv();
	}

	public void setfenv(LuaValue env) {
		parent.setfenv(env);
	}
}
