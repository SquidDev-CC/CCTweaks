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

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

/**
 * Abstract base class for compiled function implementations that take no arguments and
 * return one value.
 * <p/>
 * Subclasses need only implement {@link org.luaj.vm2.LuaValue#call()} to complete this class,
 * simplifying development.
 * All other uses of {@link #call(org.luaj.vm2.LuaValue)}, {@link #invoke(org.luaj.vm2.Varargs)},etc,
 * are routed through this method by this class.
 * <p/>
 * If one or more arguments are required, or variable argument or variable return values,
 * then use one of the related function
 * {@link org.luaj.vm2.luajc.function.OneArgFunction}, {@link org.luaj.vm2.luajc.function.TwoArgFunction}, {@link ThreeArgFunction}, or {@link org.luaj.vm2.luajc.function.VarArgFunction}.
 * <p/>
 * See {@link LuaCompiledFunction} for more information on implementation libraries and library functions.
 *
 * @see #call()
 * @see LuaCompiledFunction
 * @see org.luaj.vm2.luajc.function.OneArgFunction
 * @see org.luaj.vm2.luajc.function.TwoArgFunction
 * @see ThreeArgFunction
 * @see org.luaj.vm2.luajc.function.VarArgFunction
 */
abstract public class ZeroArgFunction extends LuaCompiledFunction {

	abstract public LuaValue call();

	public LuaValue call(LuaValue arg) {
		return call();
	}

	public LuaValue call(LuaValue arg1, LuaValue arg2) {
		return call();
	}

	public LuaValue call(LuaValue arg1, LuaValue arg2, LuaValue arg3) {
		return call();
	}

	public Varargs invoke(Varargs varargs) {
		return call();
	}
}
