package org.luaj.vm2.luajc;

import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Prototype;
import squiddev.cctweaks.core.reference.Config;

import java.util.HashMap;
import java.util.Map;

/**
 * ****************************************************************************
 * Copyright (c) 2010 Luaj.org. All rights reserved.
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * ****************************************************************************
 */
public class JavaLoaderRewrite extends ClassLoader {

	private final LuaValue env;

	private Map<String, byte[]> unloaded = new HashMap<String, byte[]>();

	public JavaLoaderRewrite(LuaValue env) {
		super(JavaLoaderRewrite.class.getClassLoader());
		this.env = env;
	}

	public LuaFunction load(Prototype p, String className, String filename) {
		JavaGenRewrite jg = new JavaGenRewrite(p, className, filename);
		return load(jg);
	}

	public LuaFunction load(JavaGenRewrite jg) {
		include(jg);
		return load(jg.className);
	}

	public LuaFunction load(String className) {
		try {
			Class c = loadClass(className);
			LuaFunction v = (LuaFunction) c.newInstance();
			v.setfenv(env);
			return v;
		} catch (Exception e) {
			if (Config.config.debug) e.printStackTrace();
			throw new IllegalStateException("bad class gen: " + e.getMessage(), e);
		}
	}

	public void include(JavaGenRewrite jg) {
		unloaded.put(jg.className, jg.bytecode);
		for (int i = 0, n = jg.inners != null ? jg.inners.length : 0; i < n; i++) {
			include(jg.inners[i]);
		}

		if (Config.config.luaJCVerify) {
			jg.validate(this);
		}
	}

	public Class findClass(String className) throws ClassNotFoundException {
		byte[] bytes = unloaded.get(className);
		if (bytes != null) {
			return defineClass(className, bytes, 0, bytes.length);
		}
		return super.findClass(className);
	}
}
