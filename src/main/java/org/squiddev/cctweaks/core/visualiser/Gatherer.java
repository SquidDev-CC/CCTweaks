package org.squiddev.cctweaks.core.visualiser;

import com.google.common.collect.Lists;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.api.UnorderedPair;
import org.squiddev.cctweaks.api.network.INetworkController;
import org.squiddev.cctweaks.api.network.INetworkNode;
import org.squiddev.cctweaks.api.network.IWorldNetworkNode;

import java.util.*;

import static org.squiddev.cctweaks.core.visualiser.VisualisationData.Node;

/**
 * Converts a network into visualisation data.
 */
public final class Gatherer {
	public static VisualisationData gather(INetworkController controller, IBlockAccess world) {
		if (controller == null) {
			return new VisualisationData(Collections.<Node>emptyList(), Collections.<UnorderedPair<Node>>emptyList());
		}

		Set<INetworkNode> nodes = controller.getNodesOnNetwork();
		Map<INetworkNode, Node> lookup = new HashMap<INetworkNode, Node>();

		List<Node> resultNodes = Lists.newArrayListWithCapacity(nodes.size());
		int i = 0;
		for (INetworkNode node : nodes) {
			IWorldPosition position = node instanceof IWorldNetworkNode ? ((IWorldNetworkNode) node).getPosition() : null;

			// We only care about the position if we are in the current world
			BlockPos resultPosition = position != null && position.getBlockAccess() == world ? position.getPosition() : null;

			Node result = new Node(i++, node.toString(), resultPosition);
			resultNodes.add(result);
			lookup.put(node, result);
		}

		Set<UnorderedPair<INetworkNode>> connections = controller.getNodeConnections();
		List<UnorderedPair<Node>> resultConnections = Lists.newArrayListWithCapacity(connections.size());
		for (UnorderedPair<INetworkNode> connection : connections) {
			resultConnections.add(new UnorderedPair<Node>(lookup.get(connection.x), lookup.get(connection.y)));
		}

		return new VisualisationData(resultNodes, resultConnections);
	}
}
