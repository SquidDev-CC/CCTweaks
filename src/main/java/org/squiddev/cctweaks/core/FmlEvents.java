package org.squiddev.cctweaks.core;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import org.squiddev.cctweaks.CCTweaks;

import java.util.LinkedList;
import java.util.Queue;

/**
 * This handles various events
 */
public final class FmlEvents {
	private static FmlEvents instance;

	public FmlEvents() {
		if (instance == null) {
			instance = this;
		} else {
			throw new IllegalStateException("Events already exists");
		}
	}

	private final Queue<Runnable> scheduledQueue = new LinkedList<Runnable>();

	private void add(Runnable runnable) {
		scheduledQueue.add(runnable);
	}

	public static void schedule(Runnable runnable) {
		instance.add(runnable);
	}

	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			for (Runnable scheduled : scheduledQueue) {
				scheduled.run();
			}
		}
	}

	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
		if (eventArgs.modID.equals(CCTweaks.ID)) {
			Config.sync();
		}
	}
}
