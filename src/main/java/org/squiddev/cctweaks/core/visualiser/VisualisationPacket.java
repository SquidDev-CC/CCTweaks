package org.squiddev.cctweaks.core.visualiser;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.IBlockAccess;
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
	public VisualisationData data;

	public static VisualisationPacket create(INetworkController controller, IBlockAccess world) {
		VisualisationPacket packet = new VisualisationPacket();
		packet.data = Gatherer.gather(controller, world);
		return packet;
	}

	public static void send(INetworkController controller, EntityPlayerMP player) {
		CCTweaks.network.sendTo(create(controller, player.worldObj), player);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		byte version = buf.readByte();
		if (version == 0) {
			data = EncoderV0.INSTANCE.read(buf);
		} else {
			DebugLogger.error("Unexpected version " + version + " for network visualiser");
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeByte(0);
		EncoderV0.INSTANCE.write(buf, data);
	}

	@Override
	public IMessage handle(MessageContext cxt) {
		RenderNetworkOverlay.data = data;
		return null;
	}

	public interface Encoder {
		VisualisationData read(ByteBuf buffer);

		void write(ByteBuf buffer, VisualisationData data);
	}
}
