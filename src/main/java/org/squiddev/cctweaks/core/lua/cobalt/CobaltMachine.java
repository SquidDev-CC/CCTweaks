package org.squiddev.cctweaks.core.lua.cobalt;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.ILuaObject;
import dan200.computercraft.api.lua.ILuaTask;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.core.apis.ILuaAPI;
import dan200.computercraft.core.computer.Computer;
import dan200.computercraft.core.computer.ITask;
import dan200.computercraft.core.computer.MainThread;
import dan200.computercraft.core.lua.ILuaMachine;
import org.squiddev.cctweaks.api.lua.ArgumentDelegator;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cobalt.*;
import org.squiddev.cobalt.debug.DebugHandler;
import org.squiddev.cobalt.debug.DebugInfo;
import org.squiddev.cobalt.debug.DebugState;
import org.squiddev.cobalt.function.LuaFunction;
import org.squiddev.cobalt.function.VarArgFunction;
import org.squiddev.cobalt.lib.*;
import org.squiddev.cobalt.lib.platform.AbstractResourceManipulator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.IdentityHashMap;
import java.util.Map;

import static org.squiddev.cobalt.Constants.NIL;
import static org.squiddev.cobalt.Constants.NONE;
import static org.squiddev.cobalt.ValueFactory.valueOf;
import static org.squiddev.cobalt.ValueFactory.varargsOf;

/**
 * An rewrite of the Lua machine using cobalt
 *
 * @see dan200.computercraft.core.lua.LuaJLuaMachine
 */
public class CobaltMachine implements ILuaMachine, ILuaContext {
	private static final String[] ILLEGAL_NAMES = new String[]{
		"collectgarbage",
		"dofile",
		"loadfile",
		"print"
	};

	private final Computer computer;
	private final LuaState state;
	private final LuaTable globals;
	private LuaThread mainThread;

	private String eventFilter = null;
	private String hardAbort = null;
	private String softAbort = null;

	public CobaltMachine(Computer computer) {
		this.computer = computer;

		final LuaState state = this.state = new LuaState(new AbstractResourceManipulator() {
			@Override
			public InputStream findResource(String s) {
				throw new IllegalStateException("Cannot open files");
			}
		});

		state.debug = new DebugHandler(state) {
			int count = 0;

			@Override
			public void onInstruction(DebugState ds, DebugInfo di, int pc, Varargs extras, int top) {
				if (++count > 100000) {
					if (hardAbort != null) LuaThread.yield(state, NONE);
					count = 0;
				}
				super.onInstruction(ds, di, pc, extras, top);
			}
		};

		LuaTable globals = this.globals = new LuaTable();
		state.setupThread(globals);

		// Add basic libraries
		globals.load(state, new BaseLib());
		globals.load(state, new TableLib());
		globals.load(state, new StringLib());
		globals.load(state, new MathLib());
		globals.load(state, new CoroutineLib());

		if (!Config.globalWhitelist.contains("debug")) {
			globals.load(state, new DebugLib());
		}

		for (String global : ILLEGAL_NAMES) {
			if (!Config.globalWhitelist.contains(global)) globals.rawset(global, Constants.NIL);
		}

		// TODO: Move to Cobalt
		globals.rawset("_VERSION", valueOf("Lua 5.1"));

		globals.rawset("_CC_VERSION", valueOf(ComputerCraft.getVersion()));
		globals.rawset("_MC_VERSION", valueOf("${mc_version}"));
		globals.rawset("_LUAJ_VERSION", valueOf("Cobalt 0.1"));
		if (ComputerCraft.disable_lua51_features) {
			globals.rawset("_CC_DISABLE_LUA51_FEATURES", Constants.TRUE);
		}
	}

	@Override
	public void addAPI(ILuaAPI api) {
		LuaValue table = wrapLuaObject(api);
		for (String name : api.getNames()) {
			globals.rawset(name, table);
		}
	}

	@Override
	public void loadBios(InputStream bios) {
		if (mainThread != null) return;
		try {
			LuaFunction value = LoadState.load(state, bios, "bios", globals);
			mainThread = new LuaThread(state, value, globals);
		} catch (LuaError e) {
			if (mainThread != null) {
				state.abandon();
				mainThread = null;
			}
		} catch (IOException e) {
			if (mainThread != null) {
				state.abandon();
				mainThread = null;
			}
		}
	}

	@Override
	public void handleEvent(String eventName, Object[] arguments) {
		if (mainThread == null) return;

		if (eventFilter == null || eventName == null || eventName.equals(eventFilter) || eventName.equals("terminate")) {
			try {
				Varargs args = Constants.NONE;
				if (eventName != null) {
					Varargs params = toValues(arguments);
					if (params.count() == 0) {
						args = valueOf(eventName);
					} else {
						args = varargsOf(valueOf(eventName), params);
					}
				}

				Varargs results = mainThread.resume(args);
				if (hardAbort != null) {
					throw new LuaError(hardAbort);
				}

				if (!results.first().checkBoolean()) {
					throw new LuaError(results.arg(2).checkString());
				}

				LuaValue filter = results.arg(2);
				if (filter.isString()) {
					eventFilter = filter.toString();
				} else {
					eventFilter = null;
				}

				if (mainThread.getStatus().equals("dead")) {
					mainThread = null;
				}
			} catch (LuaError e) {
				state.abandon();
				mainThread = null;
			} finally {
				softAbort = null;
				hardAbort = null;
			}

		}
	}

	@Override
	public void softAbort(String message) {
		softAbort = message;
	}

	@Override
	public void hardAbort(String message) {
		softAbort = message;
		hardAbort = message;
	}

	@Override
	public boolean saveState(OutputStream outputStream) {
		return false;
	}

	@Override
	public boolean restoreState(InputStream inputStream) {
		return false;
	}

	@Override
	public boolean isFinished() {
		return mainThread == null;
	}

	@Override
	public void unload() {
		if (this.mainThread == null) return;
		state.abandon();
		mainThread = null;
	}

	private LuaValue wrapLuaObject(final ILuaObject object) {
		String[] methods = object.getMethodNames();
		LuaTable result = new LuaTable(0, methods.length);

		for (int i = 0; i < methods.length; i++) {
			final int method = i;
			result.rawset(methods[i], new VarArgFunction() {
				@Override
				public Varargs invoke(LuaState state, Varargs args) {
					String message = softAbort;
					if (message != null) {
						softAbort = null;
						hardAbort = null;
						throw new LuaError(message);
					}

					try {
						Object[] results = ArgumentDelegator.delegateLuaObject(object, CobaltMachine.this, method, new CobaltArguments(args));
						return toValues(results);
					} catch (LuaException e) {
						throw new LuaError(e.getMessage(), e.getLevel());
					} catch (InterruptedException e) {
						throw new OrphanedThread();
					} catch (Throwable e) {
						throw new LuaError("Java Exception Thrown: " + e.toString(), 0);
					}
				}
			});
		}

		return result;
	}

	//region Conversion
	public LuaValue toValue(Object object, Map<Object, LuaValue> tables) {
		if (object == null) {
			return NIL;
		} else if (object instanceof Number) {
			return valueOf(((Number) object).doubleValue());
		} else if (object instanceof Boolean) {
			return valueOf((Boolean) object);
		} else if (object instanceof String) {
			return valueOf(object.toString());
		} else if (object instanceof byte[]) {
			return valueOf((byte[]) object);
		} else if (object instanceof Map) {
			if (tables == null) {
				tables = new IdentityHashMap<Object, LuaValue>();
			} else {
				LuaValue value = tables.get(object);
				if (value != null) return value;
			}

			LuaTable table = new LuaTable();
			tables.put(object, table);

			for (Map.Entry<?, ?> pair : ((Map<?, ?>) object).entrySet()) {
				LuaValue key = toValue(pair.getKey(), tables);
				LuaValue value = toValue(pair.getValue(), tables);
				if (!key.isNil() && !value.isNil()) {
					table.rawset(key, value);
				}
			}

			return table;
		} else if (object instanceof ILuaObject) {
			return wrapLuaObject((ILuaObject) object);
		} else {
			return NIL;
		}
	}

	public Varargs toValues(Object[] objects) {
		if (objects != null && objects.length != 0) {
			LuaValue[] values = new LuaValue[objects.length];

			for (int i = 0; i < objects.length; ++i) {
				Object object = objects[i];
				values[i] = toValue(object, null);
			}

			return varargsOf(values);
		} else {
			return NONE;
		}
	}
	//endregion

	@Override
	public Object[] pullEvent(String filter) throws LuaException, InterruptedException {
		Object[] results = pullEventRaw(filter);
		if (results.length >= 1 && results[0].equals("terminate")) {
			throw new LuaException("Terminated", 0);
		} else {
			return results;
		}
	}

	@Override
	public Object[] pullEventRaw(String filter) throws InterruptedException {
		return yield(new Object[]{filter});
	}

	@Override
	public Object[] yield(Object[] objects) throws InterruptedException {
		try {
			Varargs results = LuaThread.yield(state, toValues(objects));
			return CobaltConverter.toObjects(results, 0, false);
		} catch (OrphanedThread e) {
			throw new InterruptedException();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Object[] executeMainThreadTask(final ILuaTask task) throws LuaException, InterruptedException {
		long taskID = issueMainThreadTask(task);

		Object[] response;
		do {
			do {
				response = this.pullEvent("task_complete");
			} while (response.length < 3);
		}
		while (!(response[1] instanceof Number) || !(response[2] instanceof Boolean) || (long) ((Number) response[1]).intValue() != taskID);

		if (!(Boolean) response[2]) {
			if (response.length >= 4 && response[3] instanceof String) {
				throw new LuaException((String) response[3]);
			} else {
				throw new LuaException();
			}
		} else {
			Object[] returnValues = new Object[response.length - 3];
			System.arraycopy(response, 3, returnValues, 0, returnValues.length);
			return returnValues;
		}
	}

	@Override
	public long issueMainThreadTask(final ILuaTask task) throws LuaException {
		final long taskID = MainThread.getUniqueTaskID();
		ITask generatedTask = new ITask() {
			@Override
			public Computer getOwner() {
				return computer;
			}

			@Override
			public void execute() {
				try {
					Object[] t = task.execute();
					if (t != null) {
						Object[] eventArguments = new Object[t.length + 2];
						eventArguments[0] = taskID;
						eventArguments[1] = true;
						System.arraycopy(t, 0, eventArguments, 2, t.length);

						computer.queueEvent("task_complete", eventArguments);
					} else {
						computer.queueEvent("task_complete", new Object[]{taskID, true});
					}
				} catch (LuaException e) {
					computer.queueEvent("task_complete", new Object[]{taskID, false, e.getMessage()});
				} catch (Throwable e) {
					computer.queueEvent("task_complete", new Object[]{taskID, false, "Java Exception Thrown: " + e.toString()});
				}
			}
		};
		if (MainThread.queueTask(generatedTask)) {
			return taskID;
		} else {
			throw new LuaException("Task limit exceeded");
		}
	}
}
