package org.squiddev.cctweaks.core.visualiser;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.IBlockAccess;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.api.SingleTypeUnorderedPair;
import org.squiddev.cctweaks.api.network.INetworkController;
import org.squiddev.cctweaks.api.network.INetworkNode;
import org.squiddev.cctweaks.api.network.IWorldNetworkNode;
import org.squiddev.cctweaks.client.render.RenderNetworkOverlay;
import org.squiddev.cctweaks.core.network.controller.NetworkController;
import org.squiddev.cctweaks.core.packet.AbstractPacketHandler;
import org.squiddev.cctweaks.core.utils.DebugLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Handles transmitting/receiving the network
 */
public class VisualisationPacket implements AbstractPacketHandler.IPacket {
	public VisualisationData data;
	public INetworkController controller;
	public IBlockAccess world;

	public static VisualisationPacket create(INetworkController controller, IBlockAccess world) {
		VisualisationPacket packet = new VisualisationPacket();
		packet.controller = controller;
		packet.world = world;
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
		version0.write(buf, controller, world);
	}

	@Override
	public IMessage handle(MessageContext cxt) {
		RenderNetworkOverlay.data = data;
		return null;
	}

	public interface Encoder {
		VisualisationData read(ByteBuf buffer);

		void write(ByteBuf buffer, INetworkController data, IBlockAccess world);
	}

	/**
	 * Writes in the form:
	 *
	 * nodes length: int
	 * nodes:
	 * | kind: byte
	 * | name: string
	 * | peripherals length: short
	 * | | name: string
	 * | x: int ? kind == 1
	 * | y: int ? kind == 1
	 * | z: int ? kind == 1
	 * connections length: int
	 * connections:
	 * | x: int
	 * | y: int
	 */
	private static final Encoder version0 = new Encoder() {
		@Override
		public VisualisationData read(ByteBuf buffer) {
			int nodeSize = buffer.readInt();
			VisualisationData.Node[] nodes = new VisualisationData.Node[nodeSize];
			for (int i = 0; i < nodeSize; i++) {
				int kind = buffer.readByte();

				String name = ByteBufUtils.readUTF8String(buffer);

				int peripheralSize = buffer.readShort();
				String[] peripherals = new String[peripheralSize];
				for (int pIndex = 0; pIndex < peripheralSize; pIndex++) {
					peripherals[pIndex] = ByteBufUtils.readUTF8String(buffer);
				}

				switch (kind) {
					case 0:
						nodes[i] = new VisualisationData.Node(name, peripherals);
						break;
					case 1:
						nodes[i] = new VisualisationData.PositionedNode(name, peripherals, buffer.readInt(), buffer.readInt(), buffer.readInt());
						break;
					default:
						DebugLogger.error("Unknown node kind " + kind);
						return null;
				}
			}

			int connectionSize = buffer.readInt();
			VisualisationData.Connection[] connections = new VisualisationData.Connection[connectionSize];
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

				connections[i] = new VisualisationData.Connection(nodes[x], nodes[y]);
			}

			return new VisualisationData(nodes, connections);
		}

		@Override
		public void write(ByteBuf buffer, INetworkController data, IBlockAccess world) {
			if (data == null) {
				buffer.writeInt(0);
				buffer.writeInt(0);
				return;
			}

			NetworkController controller = data instanceof NetworkController ? (NetworkController) data : null;

			Set<INetworkNode> nodes = data.getNodesOnNetwork();
			Map<INetworkNode, Integer> lookup = new HashMap<INetworkNode, Integer>();

			buffer.writeInt(nodes.size());
			int index = 0;
			for (INetworkNode node : nodes) {
				lookup.put(node, index);

				IWorldPosition position = node instanceof IWorldNetworkNode ? ((IWorldNetworkNode) node).getPosition() : null;
				boolean validPosition = position != null && position.getWorld() == world;

				buffer.writeByte(validPosition ? 1 : 0);
				ByteBufUtils.writeUTF8String(buffer, node.toString());

				if (controller == null) {
					buffer.writeInt(0);
				} else {
					Set<String> peripherals = controller.getPoint(node).peripherals.keySet();
					buffer.writeShort(peripherals.size());
					for (String peripheral : peripherals) {
						ByteBufUtils.writeUTF8String(buffer, peripheral);
					}
				}

				if (validPosition) {
					buffer.writeInt(position.getX());
					buffer.writeInt(position.getY());
					buffer.writeInt(position.getZ());
				}

				index++;
			}

			Set<SingleTypeUnorderedPair<INetworkNode>> connections = data.getNodeConnections();
			buffer.writeInt(connections.size());

			for (SingleTypeUnorderedPair<INetworkNode> connection : connections) {
				buffer.writeInt(lookup.get(connection.x));
				buffer.writeInt(lookup.get(connection.y));
			}
		}
	};
}
