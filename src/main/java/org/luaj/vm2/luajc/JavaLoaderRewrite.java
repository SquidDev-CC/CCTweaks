package org.luaj.vm2.luajc;

import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Prototype;
import squiddev.cctweaks.core.utils.DebugLogger;

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
	private final ClassLoader parent;

	private Map<String, byte[]> unloaded = new HashMap<String, byte[]>();

	public JavaLoaderRewrite(LuaValue env) {
		this.env = env;
		parent = getClass().getClassLoader();
	}

	public LuaFunction load(Prototype p, String className, String fileName) {
		JavaGenRewrite jg = new JavaGenRewrite(p, className, fileName);
		return load(jg);
	}

	public LuaFunction load(JavaGenRewrite jg) {
		include(jg);
		return load(jg.classname);
	}

	public LuaFunction load(String className) {
		try {
			Class c = loadClass(className);

			Object o = c.newInstance();
			DebugLogger.debug(o.toString());
			Class parent = o.getClass();
			do {
				DebugLogger.debug("\tExtends " + parent.getName());
				DebugLogger.debug("\t  Same class: " + (parent.equals(LuaFunction.class) ? "Yes" : "No"));
				DebugLogger.debug("\t  Assignable: " + (LuaFunction.class.isAssignableFrom(parent) ? "Yes" : "No"));
				DebugLogger.debug("\t  From Assignable: " + (parent.isAssignableFrom(o.getClass()) ? "Yes" : "No"));
				try {
					DebugLogger.debug("\t  Can cast: " + parent.cast(o).toString());
				} catch(Exception e) {
					DebugLogger.debug("\t  Cannot cast " + e.toString());
				}
			} while((parent = parent.getSuperclass())!= null);

			try {
				Object t = LuaFunction.class.cast(o);
				LuaFunction f = (LuaFunction) t;
				LuaValue v = (LuaValue) o;
				LuaFunction func = (LuaFunction) v;
			} catch(Exception e) {
				DebugLogger.error(e.getMessage());
			}

			LuaFunction v = (LuaFunction) o;
			v.setfenv(env);
			return v;
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException("bad class gen: " + e);
		}
	}

	public void include(JavaGenRewrite jg) {
		unloaded.put(jg.classname, jg.bytecode);
		for (int i = 0, n = jg.inners != null ? jg.inners.length : 0; i < n; i++)
			include(jg.inners[i]);
	}

	public Class findClass(String classname) throws ClassNotFoundException {
		byte[] bytes = unloaded.get(classname);
		if (bytes != null) return defineClass(classname, bytes, 0, bytes.length);
		return super.findClass(classname);
	}

}
