package org.squiddev.cctweaks.core.network.mock;

import codechicken.lib.vec.BlockCoord;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.ForgeDirection;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.api.network.INetworkController;
import org.squiddev.cctweaks.core.network.PacketTest;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A basic network
 */
public class BasicNetwork implements IBlockAccess, Iterable<Map.Entry<BlockCoord, KeyedNetworkNode>> {
	protected final Map<BlockCoord, TileEntity> world = new HashMap<BlockCoord, TileEntity>();
	protected final Map<BlockCoord, KeyedNetworkNode> nodes = new HashMap<BlockCoord, KeyedNetworkNode>();

	public final int width;
	public final int height;

	public BasicNetwork(PacketTest.TestData network) {
		int width = 0;
		for (int z = 0; z < network.map.length; z++) {
			String row = network.map[z];
			width = Math.max(width, row.length());

			for (int x = 0; x < row.length(); x++) {
				NodeTile tile = new NodeTile(this, x, z);
				KeyedNetworkNode node = parse(tile, row.charAt(x));
				if (node != null) {
					tile.node = node;
					BlockCoord coord = new BlockCoord(x, 0, z);
					world.put(coord, tile);
					nodes.put(coord, node);
				}
			}
		}

		this.width = width;
		height = network.map.length;

		for (CountingNetworkNode node : nodes.values()) {
			node.connect();
		}
	}

	public KeyedNetworkNode parse(IWorldPosition position, char character) {
		switch (Character.toLowerCase(character)) {
			case '=':
			case '+':
			case '-':
			case '|':
				character = '-';
				break;
			case '>':
				return new KeyedNetworkNode(position, Character.toString(character), new boolean[]{true, true, false, true, true});
			case '<':
				return new KeyedNetworkNode(position, Character.toString(character), new boolean[]{true, true, true, false, true});
			case 'b':
				return new BoundNetworkNode(position, Character.toString(character));
			case ' ':
				return null;
		}
		return new KeyedNetworkNode(position, Character.toString(character));
	}

	public void reset() {
		for (CountingNetworkNode node : nodes.values()) {
			node.reset();
		}
	}

	public void dump() {
		SetMultimap<INetworkController, BlockCoord> networks = MultimapBuilder.hashKeys().hashSetValues().build();

		for (Map.Entry<BlockCoord, KeyedNetworkNode> location : this) {
			networks.put(location.getValue().getAttachedNetwork(), location.getKey());
		}

		for (Map.Entry<INetworkController, Collection<BlockCoord>> entry : networks.asMap().entrySet()) {
			System.out.println(entry.getKey());
			for (BlockCoord pos : entry.getValue()) {
				System.out.println(" - " + pos);
			}
		}
	}

	@Override
	public TileEntity getTileEntity(int x, int y, int z) {
		return world.get(new BlockCoord(x, y, z));
	}

	@Override
	public Iterator<Map.Entry<BlockCoord, KeyedNetworkNode>> iterator() {
		return nodes.entrySet().iterator();
	}

	@Override
	public int getHeight() {
		return 1;
	}

	@Override
	public int getLightBrightnessForSkyBlocks(int x, int y, int z, int side) {
		return 0;
	}

	@Override
	public int getBlockMetadata(int x, int y, int z) {
		return 0;
	}

	@Override
	public int isBlockProvidingPowerTo(int x, int y, int z, int side) {
		return 0;
	}

	@Override
	public boolean isAirBlock(int x, int y, int z) {
		return getTileEntity(x, y, z) != null;
	}

	@Override
	public BiomeGenBase getBiomeGenForCoords(int x, int z) {
		return null;
	}


	@Override
	public boolean extendedLevelsInChunkCache() {
		return false;
	}

	@Override
	public boolean isSideSolid(int x, int y, int z, ForgeDirection side, boolean _default) {
		return true;
	}

	@Override
	public Block getBlock(int x, int y, int z) {
		return null;
	}
}
