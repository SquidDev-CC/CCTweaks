package org.squiddev.cctweaks.core;

import dan200.computercraft.shared.computer.core.IComputer;
import dan200.computercraft.shared.computer.core.IContainerComputer;
import dan200.computercraft.shared.computer.core.ServerComputer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.core.utils.WorldPosition;
import org.squiddev.cctweaks.lua.lib.DelayedTasks;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Main event handler
 */
public class McEvents {
	public interface IDropConsumer {
		void consumeDrop(ItemStack stack);
	}

	private static final Map<IWorldPosition, IDropConsumer> blockConsumers = new HashMap<IWorldPosition, IDropConsumer>();
	private static final Map<Entity, IDropConsumer> entityConsumers = new HashMap<Entity, IDropConsumer>();
	private static final Queue<Runnable> serverQueue = new LinkedList<Runnable>();
	private static final Queue<Runnable> clientQueue = new LinkedList<Runnable>();

	/**
	 * Add a consumer for entity drops
	 *
	 * @param entity   The entity whose drops to consume
	 * @param consumer The drop consumer
	 */
	public static void addEntityConsumer(Entity entity, IDropConsumer consumer) {
		if (!entity.captureDrops && (entity.capturedDrops == null || entity.capturedDrops.size() == 0)) {
			entity.captureDrops = true;
			entityConsumers.put(entity, consumer);
		}
	}

	/**
	 * Remove an entity drop consumer
	 *
	 * @param entity The entity who's drops to consume
	 */
	public static void removeEntityConsumer(Entity entity) {
		IDropConsumer consumer = entityConsumers.remove(entity);

		if (consumer != null && entity.captureDrops) {
			entity.captureDrops = false;

			if (entity.capturedDrops != null && entity.capturedDrops.size() > 0) {
				for (EntityItem item : entity.capturedDrops) {
					consumer.consumeDrop(item.getEntityItem());
				}
				entity.capturedDrops.clear();
			}
		}
	}

	public static void addBlockConsumer(IWorldPosition position, IDropConsumer consumer) {
		blockConsumers.put(position, consumer);
	}

	public static void removeBlockConsumer(IWorldPosition position) {
		blockConsumers.remove(position);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onHarvestDrops(BlockEvent.HarvestDropsEvent event) {
		if (blockConsumers.size() > 0) {
			IDropConsumer consumer = blockConsumers.get(new WorldPosition(event.getWorld(), event.getPos()));
			if (consumer != null) {
				for (ItemStack item : event.getDrops()) {
					if (event.getWorld().rand.nextFloat() < event.getDropChance()) {
						consumer.consumeDrop(item);
					}
				}
				event.getDrops().clear();

			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onEntityLivingDrops(LivingDropsEvent event) {
		IDropConsumer consumer = entityConsumers.get(event.getEntity());
		if (consumer != null) {
			for (EntityItem item : event.getDrops()) {
				consumer.consumeDrop(item.getEntityItem());
			}
			event.getDrops().clear();
		}
	}

	public static void schedule(Runnable runnable) {
		synchronized (serverQueue) {
			serverQueue.add(runnable);
		}
	}

	public static void scheduleClient(Runnable runnable) {
		synchronized (clientQueue) {
			clientQueue.add(runnable);
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
	public void onContainerOpen(PlayerContainerEvent.Open open) {
		if (open.getEntityPlayer().getEntityWorld().isRemote) return;

		Container container = open.getContainer();
		if (container instanceof IContainerComputer) {
			IComputer computer = ((IContainerComputer) container).getComputer();
			if (computer instanceof ServerComputer) {
				((ServerComputer) computer).sendState(open.getEntityPlayer());
			}
		}
	}

	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			DelayedTasks.update();
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
		if (eventArgs.getModID().equals(CCTweaks.ID)) {
			Config.sync();
		}
	}
}
