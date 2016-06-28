package org.squiddev.cctweaks.core.visualiser;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.api.SingleTypeUnorderedPair;
import org.squiddev.cctweaks.api.network.INetworkController;
import org.squiddev.cctweaks.api.network.INetworkNode;
import org.squiddev.cctweaks.api.network.IWorldNetworkNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.squiddev.cctweaks.core.visualiser.VisualisationData.Connection;
import static org.squiddev.cctweaks.core.visualiser.VisualisationData.Node;

/**
 * Converts a network into visualisation data.
 */
public final class Gatherer {
	public static VisualisationData gather(INetworkController controller, IBlockAccess world) {
		if (controller == null) return new VisualisationData(new Node[0], new Connection[0]);

		Set<INetworkNode> nodes = controller.getNodesOnNetwork();
		Map<INetworkNode, Node> lookup = new HashMap<INetworkNode, Node>();

		Node[] resultNodes = new Node[nodes.size()];
		int index = 0;
		for (INetworkNode node : nodes) {
			IWorldPosition position = node instanceof IWorldNetworkNode ? ((IWorldNetworkNode) node).getPosition() : null;

			// We only care about the position if we are in the current world
			BlockPos resultPosition = position != null && position.getBlockAccess() == world ? position.getPosition() : null;

			Node result = resultNodes[index] = new Node(node.toString(), resultPosition);
			lookup.put(node, result);
			index++;
		}

		Set<SingleTypeUnorderedPair<INetworkNode>> connections = controller.getNodeConnections();
		Connection[] resultConnections = new Connection[connections.size()];
		index = 0;
		for (SingleTypeUnorderedPair<INetworkNode> connection : connections) {
			resultConnections[index] = new Connection(lookup.get(connection.x), lookup.get(connection.y));
			index++;
		}

		return new VisualisationData(resultNodes, resultConnections);
	}
}
