package org.squiddev.cctweaks.core.visualiser;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import org.squiddev.cctweaks.api.UnorderedPair;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class NetworkChange {
	private static final int HAS_POSITION = 1;
	private static final int HAS_LABEL = 2;
	public static final int MAX_NODES = 128;

	public final boolean clear;
	public final Collection<NetworkNode> addedNodes;
	public final Collection<Integer> removedNodes;
	public final Collection<UnorderedPair<Integer>> addedConnections;
	public final Collection<UnorderedPair<Integer>> removedConnections;

	public NetworkChange(boolean clear, Collection<NetworkNode> addedNodes, Collection<Integer> removedNodes, Collection<UnorderedPair<Integer>> addedConnections, Collection<UnorderedPair<Integer>> removedConnections) {
		this.clear = clear;
		this.addedNodes = Collections.unmodifiableCollection(addedNodes);
		this.removedNodes = Collections.unmodifiableCollection(removedNodes);
		this.addedConnections = Collections.unmodifiableCollection(addedConnections);
		this.removedConnections = Collections.unmodifiableCollection(removedConnections);
	}

	public void write(ByteBuf buffer) {
		buffer.writeBoolean(clear);
		buffer.writeInt(addedNodes.size());
		for (NetworkNode node : addedNodes) {
			buffer.writeInt(node.id);
			buffer.writeByte((node.position != null ? HAS_POSITION : 0) | (node.name != null ? HAS_LABEL : 0));

			if (node.position != null) {
				buffer.writeLong(node.position.toLong());
			}

			if (node.name != null) {
				ByteBufUtils.writeUTF8String(buffer, node.name);
			}
		}

		buffer.writeInt(removedNodes.size());
		for (Integer removed : removedNodes) {
			buffer.writeInt(removed);
		}

		buffer.writeInt((addedConnections.size()));
		for (UnorderedPair<Integer> connection : addedConnections) {
			buffer.writeInt(connection.x);
			buffer.writeInt(connection.y);
		}

		buffer.writeInt((removedConnections.size()));
		for (UnorderedPair<Integer> connection : removedConnections) {
			buffer.writeInt(connection.x);
			buffer.writeInt(connection.y);
		}
	}

	public boolean isEmpty() {
		return !clear && addedNodes.isEmpty() && removedNodes.isEmpty() && addedConnections.isEmpty() && removedNodes.isEmpty();
	}

	public static NetworkChange read(ByteBuf buffer) {
		boolean clear = buffer.readBoolean();

		int addedNodeCount = buffer.readInt();
		List<NetworkNode> addedNodes = Lists.newArrayListWithCapacity(addedNodeCount);
		for (int i = 0; i < addedNodeCount; i++) {
			int id = buffer.readInt();
			int state = buffer.readByte();

			BlockPos position = null;
			if ((state & HAS_POSITION) != 0) {
				position = BlockPos.fromLong(buffer.readLong());
			}

			String name = null;
			if ((state & HAS_LABEL) != 0) {
				name = ByteBufUtils.readUTF8String(buffer);
			}

			addedNodes.add(new NetworkNode(id, name, position));
		}

		int removedNodeCount = buffer.readInt();
		List<Integer> removedNodes = Lists.newArrayList();
		for (int i = 0; i < removedNodeCount; i++) {
			removedNodes.add(buffer.readInt());
		}

		int addedConnectionCount = buffer.readInt();
		List<UnorderedPair<Integer>> addedConnections = Lists.newArrayListWithCapacity(addedConnectionCount);
		for (int i = 0; i < addedConnectionCount; i++) {
			addedConnections.add(new UnorderedPair<Integer>(buffer.readInt(), buffer.readInt()));
		}

		int removedConnectionCount = buffer.readInt();
		List<UnorderedPair<Integer>> removedConnections = Lists.newArrayListWithCapacity(removedConnectionCount);
		for (int i = 0; i < removedConnectionCount; i++) {
			removedConnections.add(new UnorderedPair<Integer>(buffer.readInt(), buffer.readInt()));
		}

		return new NetworkChange(clear, addedNodes, removedNodes, addedConnections, removedConnections);
	}
}
