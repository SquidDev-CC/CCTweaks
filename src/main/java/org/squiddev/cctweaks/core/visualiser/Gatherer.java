package org.squiddev.cctweaks.core.visualiser;

import net.minecraft.world.IBlockAccess;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.api.SingleTypeUnorderedPair;
import org.squiddev.cctweaks.api.network.INetworkController;
import org.squiddev.cctweaks.api.network.INetworkNode;
import org.squiddev.cctweaks.api.network.IWorldNetworkNode;
import org.squiddev.cctweaks.core.network.controller.NetworkController;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.squiddev.cctweaks.core.visualiser.VisualisationData.*;

/**
 * Converts a network into visualisation data.
 */
public final class Gatherer {
	public static VisualisationData gather(INetworkController controller, IBlockAccess world) {
		if (controller == null) return new VisualisationData(new Node[0], new Connection[0]);

		NetworkController network = controller instanceof NetworkController ? (NetworkController) controller : null;

		Set<INetworkNode> nodes = controller.getNodesOnNetwork();
		Map<INetworkNode, Node> lookup = new HashMap<INetworkNode, Node>();

		Node[] resultNodes = new Node[nodes.size()];
		int index = 0;
		for (INetworkNode node : nodes) {
			IWorldPosition position = node instanceof IWorldNetworkNode ? ((IWorldNetworkNode) node).getPosition() : null;

			// We only get peripherals if we know we can get them without side effects
			String[] peripherals;
			if (network == null) {
				peripherals = new String[0];
			} else {
				Set<String> peripheralNames = network.getPoint(node).peripherals.keySet();
				peripherals = new String[peripheralNames.size()];
				peripheralNames.toArray(peripherals);
			}

			// We only care about the position if we are in the current world
			Position resultPosition = position != null && position.getWorld() == world ? new Position(position.getX(), position.getY(), position.getZ()) : null;

			Node result = resultNodes[index] = new Node(node.toString(), peripherals, resultPosition);
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
