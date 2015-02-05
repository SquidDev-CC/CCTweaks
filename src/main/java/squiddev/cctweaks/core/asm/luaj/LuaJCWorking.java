/*******************************************************************************
 * Copyright (c) 2010 Luaj.org. All rights reserved.
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
 ******************************************************************************/

package squiddev.cctweaks.core.asm.luaj;

import org.luaj.vm2.LoadState;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Prototype;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.luajc.JavaLoader;
import squiddev.cctweaks.core.utils.DebugLogger;

import java.io.IOException;
import java.io.InputStream;

/**
 * Alternative version of LuaJC which fixes class names properly.
 * For instance chunk-name breaks in LuaJC
 */
public class LuaJCWorking implements LoadState.LuaCompiler {
	protected static final String NON_IDENTIFIER = "[^a-zA-Z0-9_]";

	protected static LuaJCWorking instance;

	public static LuaJCWorking getInstance() {
		if (instance == null) instance = new LuaJCWorking();
		return instance;
	}

	/**
	 * Install the compiler as the main compiler to use.
	 */
	public static void install() {
		DebugLogger.debug("Installing LuaJCWorking");
		LoadState.compiler = getInstance();
	}


	public LuaJCWorking() {
	}

	public LuaFunction load(InputStream stream, String name, LuaValue env) throws IOException {
		Prototype p = LuaC.compile(stream, name);
		String className = toStandardJavaClassName(name);
		String luaName = toStandardLuaFileName(name);

		JavaLoader loader = new JavaLoader(env);
		return loader.load(p, className, luaName);
	}


	private static String toStandardJavaClassName(String chunkName) {
		String stub = toStub(chunkName);
		String className = stub.replaceAll(NON_IDENTIFIER, "_");

		int c = className.charAt(0);
		if (c != '_' && !Character.isJavaIdentifierStart(c)) className = "_" + className;

		return className + "_LuaCompiled";
	}

	private static String toStandardLuaFileName(String chunkName) {
		return toStub(chunkName).replace('.', '/') + ".lua";
	}

	private static String toStub(String s) {
		return s.endsWith(".lua") ? s.substring(0, s.length() - 4) : s;
	}
}
