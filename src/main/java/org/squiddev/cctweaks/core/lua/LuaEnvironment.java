package org.squiddev.cctweaks.core.lua;

import dan200.computercraft.api.filesystem.IMount;
import dan200.computercraft.api.filesystem.IWritableMount;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.ILuaTask;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.core.apis.IAPIEnvironment;
import dan200.computercraft.core.apis.ILuaAPI;
import dan200.computercraft.core.computer.Computer;
import dan200.computercraft.core.filesystem.FileSystem;
import dan200.computercraft.core.filesystem.FileSystemException;
import org.squiddev.cctweaks.api.lua.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LuaEnvironment implements ILuaEnvironment {
	/**
	 * The instance of the Lua environment - this exists as ASM is easier this way
	 */
	public static final LuaEnvironment instance = new LuaEnvironment();

	private final Set<ILuaAPIFactory> providers = new HashSet<ILuaAPIFactory>();

	private LuaEnvironment() {
	}

	@Override
	public void registerAPI(ILuaAPIFactory factory) {
		if (factory == null) throw new IllegalArgumentException("factory cannot be null");
		providers.add(factory);
	}

	@Override
	public long issueTask(IComputerAccess access, ILuaContext context, ILuaTask task, int delay) throws LuaException {
		long id = DelayedTasks.getNextId();
		if (!DelayedTasks.addTask(access, context, task, delay, id)) throw new LuaException("Too many tasks");

		return id;
	}

	@Override
	public Object[] executeTask(IComputerAccess access, ILuaContext context, ILuaTask task, int delay) throws LuaException, InterruptedException {
		long id = issueTask(access, context, task, delay);

		Object[] response;
		do {
			response = context.pullEvent("task_complete");
		}
		while (response.length < 3 || !(response[1] instanceof Number) || !(response[2] instanceof Boolean) || (long) ((Number) response[1]).intValue() != id);

		Object[] returnValues = new Object[response.length - 3];
		if (!(Boolean) response[2]) {
			if (response.length >= 4 && response[3] instanceof String) {
				throw new LuaException((String) response[3]);
			} else {
				throw new LuaException();
			}
		} else {
			System.arraycopy(response, 3, returnValues, 0, returnValues.length);
			return returnValues;
		}
	}

	public static void inject(Computer computer) {
		if (instance.providers.size() == 0) return;

		IComputerAccess access = new ComputerAccess(computer);
		for (ILuaAPIFactory factory : instance.providers) {
			org.squiddev.cctweaks.api.lua.ILuaAPI api = factory.create(access);
			if (api != null) {
				computer.addAPI(new LuaAPI(api, factory));
			}
		}
	}

	private static class LuaAPI implements ILuaAPI, ILuaObjectWithArguments, IExtendedLuaObject {
		private final org.squiddev.cctweaks.api.lua.ILuaAPI api;
		private final ILuaAPIFactory factory;

		private LuaAPI(org.squiddev.cctweaks.api.lua.ILuaAPI api, ILuaAPIFactory factory) {
			this.api = api;
			this.factory = factory;
		}

		@Override
		public String[] getNames() {
			return factory.getNames();
		}

		@Override
		public void startup() {
			api.startup();
		}

		@Override
		public void advance(double v) {
			api.advance(v);
		}

		@Override
		public void shutdown() {
			api.shutdown();
		}

		@Override
		public String[] getMethodNames() {
			return api.getMethodNames();
		}

		@Override
		public Object[] callMethod(ILuaContext context, int method, Object[] args) throws LuaException, InterruptedException {
			return api.callMethod(context, method, args);
		}

		@Override
		public Object[] callMethod(ILuaContext context, int method, IArguments arguments) throws LuaException, InterruptedException {
			return ArgumentDelegator.delegateLuaObject(api, context, method, arguments);
		}

		@Override
		public Map<Object, Object> getAdditionalData() {
			return api instanceof IExtendedLuaObject ? ((IExtendedLuaObject) api).getAdditionalData() : Collections.emptyMap();
		}
	}

	private static final class ComputerAccess implements IComputerAccess {
		private final Computer computer;
		private final IAPIEnvironment environment;
		private final Set<String> mounts = new HashSet<String>();
		private FileSystem fs;

		private ComputerAccess(Computer computer) {
			this.computer = computer;
			this.environment = computer.getAPIEnvironment();
		}

		private FileSystem getFs() {
			if (fs == null) fs = environment.getFileSystem();
			return fs;
		}


		private String findFreeLocation(String desiredLoc) {
			try {
				synchronized (getFs()) {
					return !fs.exists(desiredLoc) ? desiredLoc : null;
				}
			} catch (FileSystemException ignored) {
				return null;
			}
		}

		@Override
		public String mount(String desiredLoc, IMount mount) {
			return this.mount(desiredLoc, mount, desiredLoc);
		}

		@Override
		public synchronized String mount(String desiredLoc, IMount mount, String driveName) {
			synchronized (getFs()) {
				String location = findFreeLocation(desiredLoc);
				if (location != null) {
					try {
						getFs().mount(driveName, location, mount);
					} catch (FileSystemException ignored) {
					}

					mounts.add(location);
				}

				return location;
			}
		}

		@Override
		public String mountWritable(String desiredLoc, IWritableMount mount) {
			return this.mountWritable(desiredLoc, mount, desiredLoc);
		}

		@Override
		public synchronized String mountWritable(String desiredLoc, IWritableMount mount, String driveName) {
			synchronized (getFs()) {
				String location = findFreeLocation(desiredLoc);
				if (location != null) {
					try {
						getFs().mountWritable(driveName, location, mount);
					} catch (FileSystemException ignored) {
					}

					mounts.add(location);
				}

				return location;
			}
		}

		@Override
		public synchronized void unmount(String location) {
			if (location != null) {
				if (!mounts.contains(location)) {
					throw new RuntimeException("You didn\'t mount this location");
				}

				getFs().unmount(location);
				mounts.remove(location);
			}
		}

		@Override
		public int getID() {
			return environment.getComputerID();
		}

		@Override
		public void queueEvent(String s, Object[] objects) {
			environment.queueEvent(s, objects);
		}

		@Override
		public String getAttachmentName() {
			return environment.getLabel();
		}
	}

	private static final class LuaTask {
		private int remaining;

		private final IComputerAccess access;
		private final ILuaContext context;

		public final ILuaTask task;
		public final IExtendedLuaTask extendedTask;
		private final long id;

		private LuaTask(IComputerAccess access, ILuaContext context, ILuaTask task, int delay, long id) {
			this.id = id;
			this.access = access;
			this.context = context;

			this.task = task;
			this.extendedTask = task instanceof IExtendedLuaTask ? (IExtendedLuaTask) task : null;
			remaining = delay;
		}

		private void yieldSuccess(Object[] result) {
			if (result != null) {
				Object[] eventArguments = new Object[result.length + 2];
				eventArguments[0] = id;
				eventArguments[1] = true;

				System.arraycopy(result, 0, eventArguments, 2, result.length);
				access.queueEvent("task_complete", eventArguments);
			} else {
				access.queueEvent("task_complete", new Object[]{id, true});
			}
		}

		private void yieldFailure(String message) {
			access.queueEvent("task_complete", new Object[]{id, false, message});
		}

		public boolean update() {
			if (remaining == 0) {
				try {
					yieldSuccess(task.execute());
				} catch (LuaException e) {
					yieldFailure(e.getMessage());
				} catch (Throwable e) {
					yieldFailure("Java Exception Thrown: " + e.toString());
				}

				return true;
			} else if (extendedTask != null) {
				try {
					extendedTask.update();
				} catch (LuaException e) {
					yieldFailure(e.getMessage());
				} catch (Throwable e) {
					yieldFailure("Java Exception Thrown: " + e.toString());
				}
			}

			return false;
		}
	}
}
