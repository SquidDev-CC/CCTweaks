package org.squiddev.cctweaks.core.network.mock;

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
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
public class BasicNetwork implements IBlockAccess, Iterable<Map.Entry<BlockPos, KeyedNetworkNode>> {
	protected final Map<BlockPos, TileEntity> world = new HashMap<BlockPos, TileEntity>();
	protected final Map<BlockPos, KeyedNetworkNode> nodes = new HashMap<BlockPos, KeyedNetworkNode>();

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
					world.put(tile.getPos(), tile);
					nodes.put(tile.getPos(), node);
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
		SetMultimap<INetworkController, BlockPos> networks = MultimapBuilder.hashKeys().hashSetValues().build();

		for (Map.Entry<BlockPos, KeyedNetworkNode> location : this) {
			networks.put(location.getValue().getAttachedNetwork(), location.getKey());
		}

		for (Map.Entry<INetworkController, Collection<BlockPos>> entry : networks.asMap().entrySet()) {
			System.out.println(entry.getKey());
			for (BlockPos pos : entry.getValue()) {
				System.out.println(" - " + pos);
			}
		}
	}

	@Override
	public TileEntity getTileEntity(BlockPos pos) {
		return world.get(pos);
	}

	public TileEntity getTileEntity(int x, int y, int z) {
		return getTileEntity(new BlockPos(x, y, z));
	}

	@Override
	public int getCombinedLight(BlockPos pos, int p_175626_2_) {
		return 0;
	}

	@Override
	public IBlockState getBlockState(BlockPos pos) {
		return null;
	}

	@Override
	public boolean isAirBlock(BlockPos pos) {
		return false;
	}

	@Override
	public Biome getBiomeGenForCoords(BlockPos pos) {
		return null;
	}

	@Override
	public Iterator<Map.Entry<BlockPos, KeyedNetworkNode>> iterator() {
		return nodes.entrySet().iterator();
	}

	@Override
	public int getStrongPower(BlockPos pos, EnumFacing direction) {
		return 0;
	}

	@Override
	public WorldType getWorldType() {
		return WorldType.DEBUG_WORLD;
	}

	@Override
	public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
		return false;
	}
}
