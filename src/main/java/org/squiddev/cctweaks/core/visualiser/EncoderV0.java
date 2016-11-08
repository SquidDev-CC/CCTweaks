package org.squiddev.cctweaks.core.visualiser;

import com.google.common.collect.Maps;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import org.squiddev.cctweaks.core.utils.DebugLogger;

import java.util.Map;

import static org.squiddev.cctweaks.core.visualiser.VisualisationData.Connection;
import static org.squiddev.cctweaks.core.visualiser.VisualisationData.Node;

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
public final class EncoderV0 implements VisualisationPacket.Encoder {
	public static final EncoderV0 INSTANCE = new EncoderV0();

	private EncoderV0() {
	}

	@Override
	public VisualisationData read(ByteBuf buffer) {
		int nodeSize = buffer.readInt();
		Node[] nodes = new Node[nodeSize];
		DebugLogger.debug("Reading " + nodeSize + " nodes");
		for (int i = 0; i < nodeSize; i++) {
			String name = ByteBufUtils.readUTF8String(buffer);
			net.minecraft.util.math.BlockPos position = buffer.readByte() == 1 ? new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt()) : null;

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

		Map<Node, Integer> lookup = Maps.newHashMap();

		buffer.writeInt(data.nodes.length);
		int index = 0;
		for (Node node : data.nodes) {
			lookup.put(node, index);

			ByteBufUtils.writeUTF8String(buffer, node.name);

			net.minecraft.util.math.BlockPos position = node.position;
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
}
