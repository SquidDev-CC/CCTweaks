package org.squiddev.cctweaks.core.lua;

import com.google.common.base.Preconditions;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.ILuaTask;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import org.squiddev.cctweaks.api.lua.IExtendedLuaTask;

/**
 * Delaying version of {@link dan200.computercraft.core.computer.MainThread}
 */
public class DelayedTasks {
	private static final int MAX_TASKS_TOTAL = 50000;
	private static final int MAX_TASKS_TICK = 1000;

	private static int taskCount = 0;
	private static LuaTask first;
	private static LuaTask last;

	private static final Object lock = new Object();
	private static long lastTask = 0;

	private DelayedTasks() {
	}

	public static long getNextId() {
		synchronized (lock) {
			return ++lastTask;
		}
	}

	private static boolean addTask(LuaTask task) {
		synchronized (lock) {
			if (taskCount < MAX_TASKS_TOTAL) {
				last.next = task;
				last = task;
				taskCount++;
				return true;
			} else {
				return false;
			}
		}
	}

	public static boolean addTask(IComputerAccess access, ILuaContext context, ILuaTask task, int delay, long id) {
		Preconditions.checkNotNull(access, "access cannot be null");
		Preconditions.checkNotNull(context, "context cannot be null");
		Preconditions.checkNotNull(task, "task cannot be null");
		if (delay < 0) throw new IllegalArgumentException("delay must be >= 0");

		return addTask(new LuaTask(access, context, task, delay, id));
	}

	public static void update() {
		LuaTask previous = null;
		LuaTask task = first;
		int i = 0;

		while (task != null && i < MAX_TASKS_TICK) {
			if (task.update()) {
				synchronized (lock) {
					taskCount--;
					(previous == null ? first : previous).next = task.next;
					if (task == last) last = null;
				}
			} else {
				previous = task;
			}

			synchronized (lock) {
				task = task.next;
			}

			i++;
		}
	}

	private static final class LuaTask {
		public LuaTask next;

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
			} else {
				remaining--;

				if (extendedTask != null) {
					try {
						extendedTask.update();
					} catch (LuaException e) {
						yieldFailure(e.getMessage());
					} catch (Throwable e) {
						yieldFailure("Java Exception Thrown: " + e.toString());
					}
				}
			}

			return false;
		}
	}
}
