package org.squiddev.cctweaks.core.network;

import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.api.network.*;
import org.squiddev.cctweaks.core.FmlEvents;
import org.squiddev.cctweaks.core.network.controller.NetworkController;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Helper methods on networks
 */
public final class NetworkHelpers implements INetworkHelpers {
	@Override
	public boolean canConnect(IBlockAccess world, int x, int y, int z, ForgeDirection direction) {
		x += direction.offsetX;
		y += direction.offsetY;
		z += direction.offsetZ;

		IWorldNetworkNode node = NetworkAPI.registry().getNode(world, x, y, z);
		return node != null && node.canConnect(direction.getOpposite());
	}

	@Override
	public boolean canConnect(IWorldPosition pos, ForgeDirection direction) {
		return canConnect(pos.getWorld(), pos.getX(), pos.getY(), pos.getZ(), direction);
	}

	@Override
	public Set<INetworkNode> getAdjacentNodes(IWorldNetworkNode node) {
		return getAdjacentNodes(node, true);
	}

	@Override
	public Set<INetworkNode> getAdjacentNodes(IWorldNetworkNode node, boolean checkExists) {
		IWorldPosition position = node.getPosition();
		IBlockAccess access = position.getWorld();

		// It might happen
		if(access == null) return Collections.emptySet();

		Set<INetworkNode> nodes = new HashSet<INetworkNode>();
		World world = checkExists && access instanceof World ? (World) access : null;

		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			if (node.canConnect(direction)) {
				int x = position.getX() + direction.offsetX, y = position.getY() + direction.offsetY, z = position.getZ() + direction.offsetZ;
				if (world == null || world.blockExists(x, y, z)) {
					IWorldNetworkNode neighbour = NetworkAPI.registry().getNode(access, x, y, z);

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
		FmlEvents.schedule(new Runnable() {
			@Override
			public void run() {
				joinOrCreateNetwork(node);
			}
		});
	}

	public static void scheduleConnect(final AbstractWorldNode node) {
		if (node == null) throw new IllegalArgumentException("node cannot be null");
		FmlEvents.schedule(new Runnable() {
			@Override
			public void run() {
				node.connect();
			}
		});
	}
}
