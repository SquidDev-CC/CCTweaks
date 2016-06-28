package org.squiddev.cctweaks.core.network;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.api.network.*;
import org.squiddev.cctweaks.core.McEvents;
import org.squiddev.cctweaks.core.network.controller.NetworkController;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Helper methods on networks
 */
public final class NetworkHelpers implements INetworkHelpers {
	@Override
	public boolean canConnect(IBlockAccess world, BlockPos position, EnumFacing direction) {
		IWorldNetworkNode node = NetworkAPI.registry().getNode(world, position.offset(direction));
		return node != null && node.canConnect(direction.getOpposite());
	}

	@Override
	public boolean canConnect(IWorldPosition pos, EnumFacing direction) {
		return canConnect(pos.getBlockAccess(), pos.getPosition(), direction);
	}

	@Override
	public Set<INetworkNode> getAdjacentNodes(IWorldNetworkNode node) {
		return getAdjacentNodes(node, true);
	}

	@Override
	public Set<INetworkNode> getAdjacentNodes(IWorldNetworkNode node, boolean checkExists) {
		IWorldPosition position = node.getPosition();
		IBlockAccess access = position.getBlockAccess();

		// It might happen
		if(access == null) return Collections.emptySet();

		Set<INetworkNode> nodes = new HashSet<INetworkNode>();
		World world = checkExists && access instanceof World ? (World) access : null;
		BlockPos blockPos = position.getPosition();

		for (EnumFacing direction : EnumFacing.VALUES) {
			if (node.canConnect(direction)) {
				BlockPos pos = blockPos.offset(direction);
				if (world == null || world.isBlockLoaded(pos)) {
					IWorldNetworkNode neighbour = NetworkAPI.registry().getNode(access, pos);

					if (neighbour != null && neighbour.canConnect(direction.getOpposite())) {
						nodes.add(neighbour);
					}
				}
			}
		}

		return nodes;
	}

	@Override
	public void joinOrCreateNetwork(IWorldNetworkNode node) {
		joinOrCreateNetwork(node, getAdjacentNodes(node));
	}

	@Override
	public void joinOrCreateNetwork(INetworkNode node, Set<? extends INetworkNode> connections) {
		for (INetworkNode neighbour : connections) {
			if (neighbour.getAttachedNetwork() != null) {
				INetworkController network = neighbour.getAttachedNetwork();
				network.formConnection(neighbour, node);
			}
		}

		if (node.getAttachedNetwork() == null) {
			joinNewNetwork(node);
			for (INetworkNode neighbour : connections) {
				node.getAttachedNetwork().formConnection(node, neighbour);
			}
		}
	}

	@Override
	public void joinNewNetwork(INetworkNode node) {
		if (node.getAttachedNetwork() != null) {
			node.getAttachedNetwork().removeNode(node);
		}
		new NetworkController(node);
	}

	@Override
	public void scheduleJoin(final IWorldNetworkNode node) {
		if (node == null) throw new IllegalArgumentException("node cannot be null");
		McEvents.schedule(new Runnable() {
			@Override
			public void run() {
				joinOrCreateNetwork(node);
			}
		});
	}

	public static void scheduleConnect(final AbstractWorldNode node) {
		if (node == null) throw new IllegalArgumentException("node cannot be null");
		McEvents.schedule(new Runnable() {
			@Override
			public void run() {
				node.connect();
			}
		});
	}
}
