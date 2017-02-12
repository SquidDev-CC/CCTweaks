package org.squiddev.cctweaks.core.visualiser;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import org.squiddev.cctweaks.api.network.INetworkController;
import org.squiddev.cctweaks.api.network.INetworkNode;
import org.squiddev.cctweaks.api.network.NetworkAPI;
import org.squiddev.cctweaks.core.packet.AbstractPacketHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Static class for watching network changes
 */
public class NetworkPlayerWatcher extends AbstractPacketHandler<VisualisationPacket> {
	public NetworkPlayerWatcher() {
		super(0, VisualisationPacket.class);
	}

	private static final Map<EntityPlayerMP, NetworkState> watchers = new HashMap<EntityPlayerMP, NetworkState>();

	public static void reset() {
		watchers.clear();
	}

	public static void update(EntityPlayerMP player, BlockPos pos) {
		INetworkNode node = pos == null ? null : NetworkAPI.registry().getNode(player.getEntityWorld(), pos);
		NetworkState state = watchers.get(player);

		INetworkController controller = node == null ? null : node.getAttachedNetwork();
		if (state == null && (controller == null || controller.getNodesOnNetwork().isEmpty())) {
			return;
		}

		if (controller == null) controller = state.controller();

		if (state == null) {
			state = new NetworkState(player);
			watchers.put(player, state);
		}

		VisualisationPacket.send(state, controller);

		if (controller.getNodesOnNetwork().isEmpty()) {
			watchers.remove(player);
		}
	}

	@Override
	public void preInit() {
		super.preInit();
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void handlePlayerLogout(PlayerEvent.PlayerLoggedOutEvent ev) {
		if (ev.player instanceof EntityPlayerMP) {
			watchers.remove(ev.player);
		}
	}
}
