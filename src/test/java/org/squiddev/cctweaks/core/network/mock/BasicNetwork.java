package org.squiddev.cctweaks.core.network.mock;

import codechicken.lib.vec.BlockCoord;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A basic network
 */
public class BasicNetwork implements IBlockAccess, Iterable<Map.Entry<BlockCoord, KeyedNetworkNode>> {
	protected final Map<BlockCoord, TileEntity> world = new HashMap<BlockCoord, TileEntity>();
	protected final Map<BlockCoord, KeyedNetworkNode> nodes = new HashMap<BlockCoord, KeyedNetworkNode>();
	public final Map<String, Integer> count;

	public BasicNetwork(TestData network) {
		count = network.counts;

		for (int z = 0; z < network.map.length; z++) {
			String row = network.map[z];
			for (int x = 0; x < row.length(); x++) {
				KeyedNetworkNode node = parse(row.charAt(x));
				if (node != null) {
					BlockCoord coord = new BlockCoord(x, 0, z);
					world.put(coord, new NodeTile(node, x, z));
					nodes.put(coord, node);
				}
			}
		}
	}

	public KeyedNetworkNode parse(char character) {
		switch (character) {
			case '=':
			case '+':
			case '-':
			case '|':
				character = '-';
				break;
			case '>':
				return new KeyedNetworkNode(Character.toString(character), new boolean[]{true, true, false, true, true});
			case '<':
				return new KeyedNetworkNode(Character.toString(character), new boolean[]{true, true, true, false, true});
			case ' ':
				return null;
		}
		return new KeyedNetworkNode(Character.toString(character));
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
