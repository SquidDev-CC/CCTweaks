package org.squiddev.cctweaks.api.network;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;

import java.util.HashSet;
import java.util.Set;

/**
 * Registry for network components
 */
public class NetworkRegistry {
	private static Set<INetworkNodeProvider> providers = new HashSet<INetworkNodeProvider>();

	/**
	 * Get the network node for the specific TileEntity.
	 *
	 * This first checks for an instance of {@link INetworkNode} on the TE
	 * then checks if the block is an instance of {@link INetworkNodeBlock} then
	 * uses {@link INetworkNodeProvider} to find it.
	 *
	 * @param tile The tile entity
	 * @return The network node or {@code null} if not found
	 */
	public static INetworkNode getNode(TileEntity tile) {
		if(tile == null) return null;

		if (tile instanceof INetworkNode) {
			return (INetworkNode) tile;
		}

		Block block = tile.blockType;
		if (block != null && block instanceof INetworkNodeBlock) {
			return ((INetworkNodeBlock) block).getNode(
				tile.getWorldObj(),
				tile.xCoord,
				tile.yCoord,
				tile.zCoord,
				tile.blockMetadata
			);
		}

		for (INetworkNodeProvider provider : providers) {
			INetworkNode node = provider.getNode(tile);
			if (node != null) return node;
		}

		return null;
	}

	/**
	 * Get the network node for the specific block.
	 *
	 * This first checks for an instance of {@link INetworkNode} on the TE of the block
	 * then checks if the block is an instance of {@link INetworkNodeBlock} then
	 * uses {@link INetworkNodeProvider} to find it.
	 *
	 * @param world The world the block is in
	 * @param x     X coordinates of the block
	 * @param y     Y coordinates of the block
	 * @param z     Z coordinates of the block
	 * @return The network node or {@code null} if not found
	 */
	public static INetworkNode getNode(IBlockAccess world, int x, int y, int z) {
		return y > 0 && y < world.getHeight() ? getNode(world.getTileEntity(x, y, z)) : null;
	}

	/**
	 * Get the network node for the specific block.
	 *
	 * This first checks for an instance of {@link INetworkNodeBlock} on the block
	 * then follows the same process as {@link #getNode(TileEntity)}
	 *
	 * @param block The block we are checking
	 * @param world The world the block is in
	 * @param x     X coordinates of the block
	 * @param y     Y coordinates of the block
	 * @param z     Z coordinates of the block
	 * @return If this block is a network node
	 */
	public static boolean isNode(Block block, IBlockAccess world, int x, int y, int z) {
		if(block == null) return false;

		if (block instanceof INetworkNodeBlock) {
			return true;
		}

		TileEntity tile = world.getTileEntity(x, y, z);

		if (tile instanceof INetworkNode) return true;
		for (INetworkNodeProvider provider : providers) {
			if (provider.isNode(tile)) return true;
		}

		return false;
	}

	/**
	 * Get the network node for the specific block.
	 *
	 * This first checks for an instance of {@link INetworkNodeBlock} on the block
	 * then follows the same process as {@link #getNode(TileEntity)}
	 *
	 * @param world The world the block is in
	 * @param x     X coordinates of the block
	 * @param y     Y coordinates of the block
	 * @param z     Z coordinates of the block
	 * @return If this block is a network node
	 */
	public static boolean isNode(IBlockAccess world, int x, int y, int z) {
		return y > 0 && y < world.getHeight() && isNode(world.getBlock(x, y, z), world, x, y, z);
	}

	/**
	 * Add a node provider to the registry
	 *
	 * @param provider The provider to add
	 */
	public static void addNodeProvider(INetworkNodeProvider provider) {
		if (provider != null) providers.add(provider);
	}
}
