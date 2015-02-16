/*******************************************************************************
 * Copyright (c) 2009-2011 Luaj.org. All rights reserved.
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
package org.luaj.vm2.luajc.function;

import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;

/**
 * Subclass of {@link org.luaj.vm2.LuaFunction} common to LuaJC compiled functions.
 * <p/>
 * Since lua functions can be called with too few or too many arguments,
 * and there are overloaded {@link org.luaj.vm2.LuaValue#call()} functions with varying
 * number of arguments, a compiled function exposed needs to handle the
 * argument fixup when a function is called with a number of arguments
 * differs from that expected.
 * <p/>
 * To simplify the creation of library functions,
 * there are 5 direct subclasses to handle common cases based on number of
 * argument values and number of return return values.
 * <ul>
 * <li>{@link org.luaj.vm2.luajc.function.ZeroArgFunction}</li>
 * <li>{@link org.luaj.vm2.luajc.function.OneArgFunction}</li>
 * <li>{@link org.luaj.vm2.luajc.function.TwoArgFunction}</li>
 * <li>{@link org.luaj.vm2.luajc.function.ThreeArgFunction}</li>
 * <li>{@link org.luaj.vm2.luajc.function.VarArgFunction}</li>
 * </ul>
 */
abstract public class LuaCompiledFunction extends LuaFunction {
	/**
	 * Java code generation utility to allocate storage for upvalue, leave it empty
	 */
	public static LuaValue[] newupe() {
		return new LuaValue[1];
	}

	/**
	 * Java code generation utility to allocate storage for upvalue, initialize with nil
	 */
	public static LuaValue[] newupn() {
		return new LuaValue[]{NIL};
	}

	/**
	 * Java code generation utility to allocate storage for upvalue, initialize with value
	 */
	public static LuaValue[] newupl(LuaValue v) {
		return new LuaValue[]{v};
	}
}
