package org.squiddev.cctweaks.core.visualiser;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.api.network.INetworkController;
import org.squiddev.cctweaks.client.render.RenderNetworkOverlay;
import org.squiddev.cctweaks.core.packet.AbstractPacketHandler;
import org.squiddev.cctweaks.core.utils.DebugLogger;

/**
 * Handles transmitting/receiving the network
 */
public class VisualisationPacket implements AbstractPacketHandler.IPacket {
	private NetworkChange change;

	public VisualisationPacket() {
	}

	public VisualisationPacket(NetworkChange change) {
		this.change = change;
	}

	public static void send(NetworkState state, INetworkController controller) {
		NetworkChange change = state.calculateChange(controller);
		if (!change.isEmpty()) {
			CCTweaks.network.sendTo(new VisualisationPacket(change), state.player);
		}
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		byte version = buf.readByte();
		if (version == 1) {
			change = NetworkChange.read(buf);
		} else {
			DebugLogger.error("Unexpected version " + version + " for network visualiser");
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeByte(1);
		change.write(buf);
	}

	@Override
	public IMessage handle(MessageContext cxt) {
		if (change != null) {
			RenderNetworkOverlay.apply(change);
		}
		return null;
	}
}
