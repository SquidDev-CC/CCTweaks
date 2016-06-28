package org.squiddev.cctweaks.core.visualiser;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.api.network.INetworkController;
import org.squiddev.cctweaks.client.render.RenderNetworkOverlay;
import org.squiddev.cctweaks.core.packet.AbstractPacketHandler;
import org.squiddev.cctweaks.core.utils.DebugLogger;

import java.util.HashMap;
import java.util.Map;

import static org.squiddev.cctweaks.core.visualiser.VisualisationData.Connection;
import static org.squiddev.cctweaks.core.visualiser.VisualisationData.Node;

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
		CCTweaks.NETWORK.sendTo(create(controller, player.worldObj), player);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		byte version = buf.readByte();
		if (version == 0) {
			data = version0.read(buf);
		} else {
			DebugLogger.error("Unexpected version " + version + " for network visualiser");
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeByte(0);
		version0.write(buf, data);
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

	/**
	 * Writes in the form:
	 *
	 * nodes length: int
	 * nodes:
	 * | name: string
	 * | peripherals length: short
	 * | | name: string
	 * | position exists: boolean
	 * | position:
	 * | | x: int
	 * | | y: int
	 * | | z: int
	 * connections length: int
	 * connections:
	 * | x: int
	 * | y: int
	 */
	private static final Encoder version0 = new Encoder() {
		@Override
		public VisualisationData read(ByteBuf buffer) {
			int nodeSize = buffer.readInt();
			Node[] nodes = new Node[nodeSize];
			DebugLogger.debug("Reading " + nodeSize + " nodes");
			for (int i = 0; i < nodeSize; i++) {
				String name = ByteBufUtils.readUTF8String(buffer);
				BlockPos position = buffer.readByte() == 1 ? new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt()) : null;

				nodes[i] = new Node(name, position);
			}

			int connectionSize = buffer.readInt();
			DebugLogger.debug("Reading " + connectionSize + " connections");
			Connection[] connections = new Connection[connectionSize];
			for (int i = 0; i < connectionSize; i++) {
				int x = buffer.readInt();
				if (x < 0 || x >= nodeSize) {
					DebugLogger.error("Invalid node index: " + x + ", expected between 0 <= x < " + nodeSize);
					return null;
				}

				int y = buffer.readInt();
				if (y < 0 || y >= nodeSize) {
					DebugLogger.error("Invalid node index: " + x + ", expected between 0 <= x < " + nodeSize);
					return null;
				}

				connections[i] = new Connection(nodes[x], nodes[y]);
			}

			return new VisualisationData(nodes, connections);
		}

		@Override
		public void write(ByteBuf buffer, VisualisationData data) {
			if (data == null) {
				buffer.writeInt(0);
				buffer.writeInt(0);
				return;
			}

			Map<Node, Integer> lookup = new HashMap<Node, Integer>();

			buffer.writeInt(data.nodes.length);
			int index = 0;
			for (Node node : data.nodes) {
				lookup.put(node, index);

				ByteBufUtils.writeUTF8String(buffer, node.name);

				BlockPos position = node.position;
				if (position != null) {
					buffer.writeByte(1);
					buffer.writeInt(position.getX());
					buffer.writeInt(position.getY());
					buffer.writeInt(position.getZ());
				} else {
					buffer.writeByte(0);
				}

				index++;
			}

			buffer.writeInt(data.connections.length);
			for (Connection connection : data.connections) {
				buffer.writeInt(lookup.get(connection.x));
				buffer.writeInt(lookup.get(connection.y));
			}
		}
	};
}
