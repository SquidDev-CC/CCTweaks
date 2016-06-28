package org.squiddev.cctweaks.core.visualiser;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import org.squiddev.cctweaks.api.network.INetworkController;
import org.squiddev.cctweaks.api.network.INetworkNode;
import org.squiddev.cctweaks.api.network.NetworkAPI;
import org.squiddev.cctweaks.core.packet.AbstractPacketHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Static class for watching network changes
 */
public class NetworkPlayerWatcher extends AbstractPacketHandler<VisualisationPacket> {
	public NetworkPlayerWatcher() {
		super(0, VisualisationPacket.class);
	}

	public static class Watcher {
		private World world;
		private final EntityPlayer player;
		public INetworkController controller;

		private int nodeHash = -1;
		private int connHash = -1;
		private boolean fresh = true;

		public Watcher(EntityPlayer player, INetworkController controller) {
			this.player = player;
			this.world = player.worldObj;
			this.controller = controller;
		}

		public boolean changed() {
			INetworkController controller = this.controller;

			// If we are empty then clear everything
			Set<INetworkNode> nodes = controller.getNodesOnNetwork();
			if (nodes.size() == 0) {
				this.controller = null;
				return true;
			}

			boolean changed = false;

			// If we are a new instance then we need to send & calculate everything
			if (fresh) {
				changed = true;
				fresh = false;
			}

			// If we've changed world then we need to resend data
			if (world != player.worldObj) {
				world = player.worldObj;
				changed = true;
			}

			// If the hashes have changed then we should update them.
			int nodeHash = controller.getNodesOnNetwork().hashCode();
			int connHash = controller.getNodeConnections().hashCode();
			if (nodeHash != this.nodeHash || connHash != this.connHash) {
				this.nodeHash = nodeHash;
				this.connHash = connHash;
				changed = true;
			}

			return changed;
		}
	}

	private static final Map<EntityPlayer, Watcher> watchers = new HashMap<EntityPlayer, Watcher>();

	public static Watcher get(EntityPlayer player) {
		return watchers.get(player);
	}

	public static Watcher remove(EntityPlayer player) {
		return watchers.remove(player);
	}

	public static void reset() {
		watchers.clear();
	}

	public static Watcher update(EntityPlayer player, BlockPos pos) {
		INetworkNode node = NetworkAPI.registry().getNode(player.worldObj, pos);
		Watcher old = watchers.get(player);

		if (node == null) return old;

		INetworkController controller = node.getAttachedNetwork();
		if (controller == null || (old != null && old.controller == controller)) return old;

		Watcher watcher = new Watcher(player, controller);
		watchers.put(player, watcher);
		return watcher;
	}

	@Override
	public void preInit() {
		super.preInit();
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void handlePlayerLogout(PlayerEvent.PlayerLoggedOutEvent ev) {
		remove(ev.player);
	}
}
