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

	private final Queue<Runnable> serverQueue = new LinkedList<Runnable>();
	private final Queue<Runnable> clientQueue = new LinkedList<Runnable>();

	public static void schedule(Runnable runnable) {
		synchronized (instance.serverQueue) {
			instance.serverQueue.add(runnable);
		}
	}

	public static void scheduleClient(Runnable runnable) {
		synchronized (instance.clientQueue) {
			instance.clientQueue.add(runnable);
		}
	}

	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			synchronized (serverQueue) {
				Runnable scheduled;
				while ((scheduled = serverQueue.poll()) != null) {
					scheduled.run();
				}
			}
		}
	}

	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			synchronized (clientQueue) {
				Runnable scheduled;
				while ((scheduled = clientQueue.poll()) != null) {
					scheduled.run();
				}
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
