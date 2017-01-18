package org.squiddev.cctweaks.core.network;

import com.google.common.base.Preconditions;
import mcmultipart.multipart.IMultipart;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.api.network.*;
import org.squiddev.cctweaks.core.McEvents;
import org.squiddev.cctweaks.core.network.controller.NetworkController;
import org.squiddev.cctweaks.integration.multipart.MultipartIntegration;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Helper methods on networks
 */
public final class NetworkHelpers implements INetworkHelpers {
	@Override
	public boolean canConnect(@Nonnull IBlockAccess world, @Nonnull BlockPos position, @Nonnull EnumFacing direction) {
		IWorldNetworkNode node = NetworkAPI.registry().getNode(world, position.offset(direction));
		return node != null && node.canConnect(direction.getOpposite());
	}

	@Override
	public boolean canConnect(@Nonnull IWorldPosition pos, @Nonnull EnumFacing direction) {
		return canConnect(pos.getBlockAccess(), pos.getPosition(), direction);
	}

	@Nonnull
	@Override
	public Set<INetworkNode> getAdjacentNodes(@Nonnull IWorldNetworkNode node) {
		return getAdjacentNodes(node, true);
	}

	@Nonnull
	@Override
	public Set<INetworkNode> getAdjacentNodes(@Nonnull IWorldNetworkNode node, boolean checkExists) {
		IWorldPosition position = node.getPosition();
		IBlockAccess access = position.getBlockAccess();

		// It might happen
		if (access == null) return Collections.emptySet();

		Set<INetworkNode> nodes = new HashSet<INetworkNode>();
		World world = checkExists && access instanceof World ? (World) access : null;
		net.minecraft.util.math.BlockPos blockPos = position.getPosition();

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
	public void joinOrCreateNetwork(@Nonnull IWorldNetworkNode node) {
		joinOrCreateNetwork(node, getAdjacentNodes(node));
	}

	@Override
	public void joinOrCreateNetwork(@Nonnull INetworkNode node, @Nonnull Set<? extends INetworkNode> connections) {
		for (INetworkNode neighbour : connections) {
			INetworkController network = neighbour.getAttachedNetwork();
			if (network != null) {
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
	public void joinNewNetwork(@Nonnull INetworkNode node) {
		if (node.getAttachedNetwork() != null) {
			node.getAttachedNetwork().removeNode(node);
		}
		new NetworkController(node);
	}

	@Override
	public void scheduleJoin(@Nonnull final IWorldNetworkNode node) {
		Preconditions.checkNotNull(node, "node cannot be null");
		McEvents.schedule(new Runnable() {
			@Override
			public void run() {
				joinOrCreateNetwork(node);
			}
		});
	}

	@Override
	public void scheduleJoin(@Nonnull final IWorldNetworkNode node, @Nonnull final TileEntity tile) {
		Preconditions.checkNotNull(node, "node cannot be null");
		Preconditions.checkNotNull(tile, "tile cannot be null");
		McEvents.schedule(new Runnable() {
			@Override
			public void run() {
				World world = tile.getWorld();
				if (world != null && world.getTileEntity(tile.getPos()) == tile) {
					joinOrCreateNetwork(node);
				}
			}
		});
	}

	public static void scheduleConnect(final AbstractWorldNode node) {
		Preconditions.checkNotNull(node, "node cannot be null");
		McEvents.schedule(new Runnable() {
			@Override
			public void run() {
				node.connect();
			}
		});
	}

	public static void scheduleConnect(final AbstractWorldNode node, final TileEntity tile) {
		Preconditions.checkNotNull(node, "node cannot be null");
		Preconditions.checkNotNull(tile, "tile cannot be null");
		McEvents.schedule(new Runnable() {
			@Override
			public void run() {
				World world = tile.getWorld();
				if (world != null && world.getTileEntity(tile.getPos()) == tile) {
					node.connect();
				}
			}
		});
	}

	@Optional.Method(modid = MultipartIntegration.MOD_NAME)
	public static void scheduleConnect(final AbstractWorldNode node, final IMultipart part) {
		Preconditions.checkNotNull(node, "node cannot be null");
		Preconditions.checkNotNull(part, "part cannot be null");
		McEvents.schedule(new Runnable() {
			@Override
			public void run() {
				if (part.getWorld() != null) {
					node.connect();
				}
			}
		});
	}
}
