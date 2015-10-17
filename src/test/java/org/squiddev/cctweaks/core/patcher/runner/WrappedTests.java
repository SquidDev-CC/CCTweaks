package org.squiddev.cctweaks.core.patcher.runner;

import dan200.computercraft.api.lua.ILuaObject;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.core.apis.FSAPI;
import dan200.computercraft.core.filesystem.FileSystem;
import org.junit.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * These exist simply as tests which aim to be easier to debug
 * than executing Lua code
 */
public class WrappedTests {
	public static void fsAPI() throws Throwable {
		BasicAPIEnvironment env = new BasicAPIEnvironment();
		FSAPI api = new FSAPI(env);

		FileSystem fs = env.getFileSystem();
		fs.list("");
		api.startup();

		Callable fsAPI = new Callable(api);

		{
			Callable handle = fsAPI.callAsCallable("open", "foo", "w");
			handle.call("write", new Object[]{"foo".getBytes()});
			handle.call("close");
		}

		{
			Callable handle = fsAPI.callAsCallable("open", "foo", "r");
			byte[] result = handle.callAs("readAll");
			handle.call("close");

			Assert.assertArrayEquals("foo".getBytes(), result);
		}
	}

	public static class Callable {
		private final Map<String, Integer> methodNames;
		private final ILuaObject object;

		public Callable(ILuaObject object) {
			this.object = object;
			String[] methods = object.getMethodNames();
			methodNames = new HashMap<String, Integer>(methods.length);
			for (int i = 0; i < methods.length; i++) {
				methodNames.put(methods[i], i);
			}
		}

		public Object[] call(String method, Object... args) throws LuaException, InterruptedException {
			return object.callMethod(null, methodNames.get(method), args);
		}

		@SuppressWarnings("unchecked")
		public <T> T callAs(String method, Object... args) throws LuaException, InterruptedException {
			return (T) call(method, args)[0];
		}

		public Callable callAsCallable(String method, Object... args) throws LuaException, InterruptedException {
			return new Callable(this.<ILuaObject>callAs(method, args));
		}
	}

}
