package org.squiddev.cctweaks.core.visualiser;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.api.UnorderedPair;
import org.squiddev.cctweaks.api.network.INetworkController;
import org.squiddev.cctweaks.api.network.INetworkNode;
import org.squiddev.cctweaks.api.network.IWorldNetworkNode;

import java.util.*;

/**
 * Stores the state of the network and provides methods for tracking and applying network changes
 */
public final class NetworkState {
	public final EntityPlayerMP player;
	private World world;
	private INetworkController controller;

	private int index = 0;
	private final Map<Integer, NetworkNode> intMapping = Maps.newHashMap();
	private final Map<INetworkNode, NetworkNode> nodeMapping = Maps.newHashMap();
	private final Set<UnorderedPair<NetworkNode>> connections = Sets.newHashSet();

	public NetworkState(EntityPlayerMP player) {
		this.player = player;
	}

	public NetworkState() {
		this.player = null;
	}

	public NetworkChange calculateChange(INetworkController controller) {
		boolean clear = false;
		if (controller != this.controller || player.worldObj != world) {
			clear = true;

			intMapping.clear();
			nodeMapping.clear();
			connections.clear();

			this.controller = controller;
			this.world = player.worldObj;
		}

		Set<INetworkNode> networkNodes = controller.getNodesOnNetwork();
		Set<UnorderedPair<INetworkNode>> networkConnections = controller.getNodeConnections();

		List<NetworkNode> addedNodes = Lists.newArrayList();
		List<Integer> removedNodes = Lists.newArrayList();

		for (NetworkNode node : intMapping.values()) {
			INetworkNode networkNode = node.node;

			if (!networkNodes.contains(networkNode)) {
				removedNodes.add(node.id);
			} else {
				IWorldPosition position = networkNode instanceof IWorldNetworkNode ? ((IWorldNetworkNode) networkNode).getPosition() : null;
				BlockPos newPos = position == null ? null : position.getPosition();
				String newName = networkNode.toString();

				if (position != null && position.getBlockAccess() != world) {
					// Remove intMapping in different dimensions
					removedNodes.add(node.id);
				} else if (!Objects.equal(newPos, node.position) || !Objects.equal(newName, node.name)) {
					node.position = newPos;
					node.name = newName;

					// Position has changed so reset.
					addedNodes.add(node);
				}
			}
		}

		// Remove all items we have queued for removal
		for (Integer removed : removedNodes) {
			NetworkNode node = intMapping.remove(removed);
			nodeMapping.remove(node.node);
		}

		// Add new nodes within range
		for (INetworkNode networkNode : networkNodes) {
			if (nodeMapping.containsKey(networkNode)) continue;

			BlockPos position = null;
			if (networkNode instanceof IWorldNetworkNode) {
				IWorldPosition worldPos = ((IWorldNetworkNode) networkNode).getPosition();
				if (!isWithinRange(worldPos, player)) continue;

				position = worldPos.getPosition();
			}

			// Add the node to all the mappings
			NetworkNode node = new NetworkNode(index++, networkNode.toString(), position, networkNode);
			addedNodes.add(node);
			nodeMapping.put(networkNode, node);
			intMapping.put(node.id, node);
		}

		List<UnorderedPair<Integer>> addedConnections = Lists.newArrayList();
		List<UnorderedPair<Integer>> removedConnections = Lists.newArrayList();
		List<UnorderedPair<NetworkNode>> removedConnectionsNode = Lists.newArrayList();

		for (UnorderedPair<NetworkNode> connection : connections) {
			if (!networkConnections.contains(new UnorderedPair<INetworkNode>(connection.x.node, connection.y.node))) {
				removedConnectionsNode.add(connection);
				removedConnections.add(new UnorderedPair<Integer>(connection.x.id, connection.y.id));
			}
		}

		// Remove all queued connections
		connections.removeAll(removedConnectionsNode);

		for (UnorderedPair<INetworkNode> networkConnection : networkConnections) {
			NetworkNode x = nodeMapping.get(networkConnection.x);
			if (x == null) continue;

			NetworkNode y = nodeMapping.get(networkConnection.y);
			if (y == null) continue;

			UnorderedPair<NetworkNode> connection = new UnorderedPair<NetworkNode>(x, y);
			if (!connections.contains(connection)) {
				// Add the appropriate node
				addedConnections.add(new UnorderedPair<Integer>(x.id, y.id));
				connections.add(connection);
			}
		}

		return new NetworkChange(clear, addedNodes, removedNodes, addedConnections, removedConnections);
	}

	public void applyChange(NetworkChange change) {
		if (change.clear) {
			intMapping.clear();
			connections.clear();
		}

		for (UnorderedPair<Integer> connection : change.removedConnections) {
			connections.add(new UnorderedPair<NetworkNode>(
				intMapping.get(connection.x),
				intMapping.get(connection.y)
			));
		}

		for (NetworkNode node : change.addedNodes) {
			NetworkNode previous = intMapping.get(node.id);
			if (previous == null) {
				intMapping.put(node.id, node);
			} else {
				previous.position = node.position;
				previous.name = node.name;
			}
		}
		for (Integer node : change.removedNodes) intMapping.remove(node);

		for (UnorderedPair<Integer> connection : change.addedConnections) {
			connections.add(new UnorderedPair<NetworkNode>(
				intMapping.get(connection.x),
				intMapping.get(connection.y)
			));
		}
	}

	private static boolean isWithinRange(IWorldPosition position, EntityPlayer player) {
		if (position.getBlockAccess() != player.worldObj) return false;

		double distance = player.getPosition().distanceSq(position.getPosition());
		MinecraftServer server = MinecraftServer.getServer();

		int maxDistance = (server == null ? 5 : MinecraftServer.getServer().getConfigurationManager().getViewDistance()) * 16;

		return distance <= maxDistance * maxDistance;
	}

	public INetworkController controller() {
		return controller;
	}

	public Collection<NetworkNode> nodes() {
		return intMapping.values();
	}

	public Collection<UnorderedPair<NetworkNode>> connections() {
		return Collections.unmodifiableCollection(connections);
	}

	public void reset() {
		intMapping.clear();
		nodeMapping.clear();
		connections.clear();
	}
}
