package org.squiddev.cctweaks.core.lua;

import org.luaj.vm2.*;
import org.luaj.vm2.compiler.LuaC;
import org.squiddev.cctweaks.core.utils.DebugLogger;
import org.squiddev.luaj.luajc.JavaLoader;
import org.squiddev.luaj.luajc.LuaJC;

import java.io.IOException;
import java.io.InputStream;

/**
 * A version of LuaJC that falls
 */
public class FallbackLuaJC extends LuaJC {
	protected static FallbackLuaJC instance;

	public static FallbackLuaJC getInstance() {
		if (instance == null) {
			instance = new FallbackLuaJC();
		}

		return instance;
	}

	public static void install() {
		LoadState.compiler = getInstance();
	}

	public LuaFunction load(InputStream stream, String name, LuaValue env) throws IOException {
		Prototype p = LuaC.compile(stream, name);
		String className = toStandardJavaClassName(name);
		JavaLoader loader = new JavaLoader(env);
		try {
			return loader.load(p, className, name);
		} catch (RuntimeException e) {
			DebugLogger.error(
				"Could not compile " + name + ". Falling back to normal Lua.\n"
					+ "Please report this at https://github.com/SquidDev/luaj.luajc/issues along with the following exception",
				e
			);
			return new LuaClosure(p, env);
		}
	}

	private static String toStandardJavaClassName(String chunkName) {
		String stub = chunkName.endsWith(".lua") ? chunkName.substring(0, chunkName.length() - 4) : chunkName;
		String className = stub.replace('/', '.').replaceAll("[^a-zA-Z0-9_]", "_");
		char c = className.charAt(0);
		if (c != 95 && !Character.isJavaIdentifierStart(c)) {
			className = "_" + className;
		}

		return className + "_LuaCompiled";
	}
}
