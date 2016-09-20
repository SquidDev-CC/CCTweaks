package org.squiddev.cctweaks.integration.multipart.network;

import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.render.TextureUtils;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Vector3;
import codechicken.microblock.ISidedHollowConnect;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TSlottedPart;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dan200.computercraft.ComputerCraft;
import dan200.computercraft.client.render.FixedRenderBlocks;
import dan200.computercraft.shared.peripheral.PeripheralType;
import dan200.computercraft.shared.peripheral.common.PeripheralItemFactory;
import dan200.computercraft.shared.peripheral.modem.TileCable;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Facing;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.api.network.INetworkNode;
import org.squiddev.cctweaks.api.network.IWorldNetworkNode;
import org.squiddev.cctweaks.api.network.IWorldNetworkNodeHost;
import org.squiddev.cctweaks.core.network.NetworkHelpers;
import org.squiddev.cctweaks.core.network.cable.CableWithInternalSidedParts;
import org.squiddev.cctweaks.core.utils.DebugLogger;
import org.squiddev.cctweaks.integration.multipart.PartBase;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class PartCable extends PartBase implements IWorldNetworkNodeHost, TSlottedPart, ISidedHollowConnect {
	public static final String NAME = CCTweaks.NAME + ":networkCable";
	private static IIcon[] icons;

	public static final double MIN = 0.375;
	public static final double MAX = 1 - MIN;

	/**
	 * Side we are testing the connection on
	 *
	 * @see #rebuildCanConnectMap()
	 */
	private ForgeDirection connectionTestSide = ForgeDirection.UNKNOWN;

	protected CableImpl cable = new CableImpl();
	protected int canConnectMap;

	@SideOnly(Side.CLIENT)
	private CableRenderer render;

	@SideOnly(Side.CLIENT)
	public CableRenderer getRender() {
		if (render == null) return render = new CableRenderer();
		return render;
	}

	@Override
	public String getType() {
		return NAME;
	}

	@Override
	public int getSlotMask() {
		return 1 << 6;
	}

	@Override
	public Iterable<Cuboid6> getOcclusionBoxes() {
		if (connectionTestSide == ForgeDirection.UNKNOWN) {
			return Collections.singletonList(new Cuboid6(MIN, MIN, MIN, MAX, MAX, MAX));
		}

		// In order to determine if this cable can pass in a certain direction,
		// through things like covers and hollow covers,
		// add an occlusion box in that direction, test if occlusion collisions occur,
		// and only make the connection if no collision occurs.
		// Then remove the added occlusion box.
		List<Cuboid6> parts = new ArrayList<Cuboid6>();

		if (tile() != null) {
			if (connectionTestSide == ForgeDirection.WEST) {
				parts.add(new Cuboid6(0, MIN, MIN, MIN, MAX, MAX));
			}
			if (connectionTestSide == ForgeDirection.EAST) {
				parts.add(new Cuboid6(MAX, MIN, MIN, 1, MAX, MAX));
			}
			if (connectionTestSide == ForgeDirection.DOWN) {
				parts.add(new Cuboid6(MIN, 0, MIN, MAX, MIN, MAX));
			}
			if (connectionTestSide == ForgeDirection.UP) {
				parts.add(new Cuboid6(MIN, MAX, MIN, MAX, 1, MAX));
			}
			if (connectionTestSide == ForgeDirection.NORTH) {
				parts.add(new Cuboid6(MIN, MIN, 0, MAX, MAX, MIN));
			}
			if (connectionTestSide == ForgeDirection.SOUTH) {
				parts.add(new Cuboid6(MIN, MIN, MAX, MAX, MAX, 1));
			}
		}

		return parts;
	}

	@Override
	public Cuboid6 getBounds() {
		double xMin = MIN;
		double yMin = MIN;
		double zMin = MIN;
		double xMax = MAX;
		double yMax = MAX;
		double zMax = MAX;

		if (tile() != null) {
			if (cable.doesConnect(ForgeDirection.WEST)) xMin = 0.0D;
			if (cable.doesConnect(ForgeDirection.EAST)) xMax = 1.0D;
			if (cable.doesConnect(ForgeDirection.DOWN)) yMin = 0.0D;
			if (cable.doesConnect(ForgeDirection.UP)) yMax = 1.0D;
			if (cable.doesConnect(ForgeDirection.NORTH)) zMin = 0.0D;
			if (cable.doesConnect(ForgeDirection.SOUTH)) zMax = 1.0D;
		}

		return new Cuboid6(xMin, yMin, zMin, xMax, yMax, zMax);
	}

	@Override
	public Iterable<IndexedCuboid6> getSubParts() {
		List<IndexedCuboid6> parts = new ArrayList<IndexedCuboid6>();
		parts.add(new IndexedCuboid6(ForgeDirection.UNKNOWN, new Cuboid6(MIN, MIN, MIN, MAX, MAX, MAX)));

		if (tile() != null) {
			if (cable.doesConnect(ForgeDirection.WEST)) {
				parts.add(new IndexedCuboid6(ForgeDirection.WEST, new Cuboid6(0, MIN, MIN, MIN, MAX, MAX)));
			}
			if (cable.doesConnect(ForgeDirection.EAST)) {
				parts.add(new IndexedCuboid6(ForgeDirection.EAST, new Cuboid6(MAX, MIN, MIN, 1, MAX, MAX)));
			}
			if (cable.doesConnect(ForgeDirection.DOWN)) {
				parts.add(new IndexedCuboid6(ForgeDirection.DOWN, new Cuboid6(MIN, 0, MIN, MAX, MIN, MAX)));
			}
			if (cable.doesConnect(ForgeDirection.UP)) {
				parts.add(new IndexedCuboid6(ForgeDirection.UP, new Cuboid6(MIN, MAX, MIN, MAX, 1, MAX)));
			}
			if (cable.doesConnect(ForgeDirection.NORTH)) {
				parts.add(new IndexedCuboid6(ForgeDirection.NORTH, new Cuboid6(MIN, MIN, 0, MAX, MAX, MIN)));
			}
			if (cable.doesConnect(ForgeDirection.SOUTH)) {
				parts.add(new IndexedCuboid6(ForgeDirection.SOUTH, new Cuboid6(MIN, MIN, MAX, MAX, MAX, 1)));
			}
		}

		return parts;
	}

	@Override
	public void harvest(MovingObjectPosition hit, EntityPlayer player) {
		World world = world();
		super.harvest(hit, player);

		if (!world.isRemote) cable.destroy();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getBrokenIcon(int side) {
		return ComputerCraft.Blocks.cable.getIcon(0, 0);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean renderStatic(Vector3 pos, int pass) {
		if (pass == 0) {
			TextureUtils.bindAtlas(0);
			getRender().drawTile(world(), x(), y(), z());
			return true;
		}
		return false;
	}

	@Override
	public ItemStack getItem() {
		return PeripheralItemFactory.create(PeripheralType.Cable, null, 1);
	}

	@Override
	public void onPartChanged(TMultiPart part) {
		rebuildCanConnectMap();
		if (!world().isRemote) cable.updateConnections();
	}

	@Override
	public void onNeighborChanged() {
		if (!world().isRemote) cable.updateConnections();
	}

	@Override
	public void onWorldJoin() {
		rebuildCanConnectMap();
		if (!world().isRemote) {
			NetworkHelpers.scheduleConnect(cable, this);
		}
	}

	@Override
	public int getHollowSize(int i) {
		return 4;
	}

	@Override
	public boolean doesTick() {
		return false;
	}

	/**
	 * Rebuild the cache of occluded sides
	 *
	 * Uses TileMultipart.canReplacePart to see if a version of this cable with
	 * with a certain side's occlusion extended to the full length
	 * can be placed in the multipart.
	 * If not, there's a cover or something in the way.
	 * Else, there's no cover, or something like a hollow cover.
	 */
	protected void rebuildCanConnectMap() {
		int map = 0;
		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			connectionTestSide = direction;
			if (tile().canReplacePart(this, this)) map |= 1 << direction.ordinal();
		}
		connectionTestSide = ForgeDirection.UNKNOWN;

		canConnectMap = map;
	}

	@Override
	public IWorldNetworkNode getNode() {
		return cable;
	}

	protected class CableImpl extends CableWithInternalSidedParts {
		@Override
		public Set<INetworkNode> getConnectedNodes() {
			Set<INetworkNode> nodes = super.getConnectedNodes();

			for (TMultiPart part : tile().jPartList()) {
				if (part != PartCable.this) {
					if (part instanceof INetworkNode) {
						nodes.add((INetworkNode) part);
					} else if (part instanceof IWorldNetworkNodeHost) {
						nodes.add(((IWorldNetworkNodeHost) part).getNode());
					}
				}
			}

			return nodes;
		}

		@Override
		public boolean canConnectInternally(ForgeDirection direction) {
			TMultiPart part = tile().partMap(direction.ordinal());
			INetworkNode node = part instanceof INetworkNode ? (INetworkNode) part
				: part instanceof IWorldNetworkNodeHost ? ((IWorldNetworkNodeHost) part).getNode()
				: null;
			return node != null;
		}

		@Override
		public IWorldPosition getPosition() {
			return PartCable.this;
		}

		@Override
		public boolean canConnect(ForgeDirection direction) {
			int flag = 1 << direction.ordinal();
			return (canConnectMap & flag) == flag;
		}
	}

	public class CableRenderer extends FixedRenderBlocks {
		/**
		 * When rendering with other nodes on the multipart, rendering overlaps,
		 * resulting in flickering between the two nodes.
		 *
		 * If we detect a node on one side, we add some padding so they don't overlap
		 * as much.
		 *
		 * There are probably better ways of doing this using {@link TMultiPart#getRenderBounds()}
		 */
		public static final double RENDER_PADDING = 0.1;

		public IIcon[] getIcons() {
			IIcon[] icons;
			if ((icons = PartCable.icons) == null) {

				try {
					Field field = TileCable.class.getDeclaredField("s_cableIcons");
					field.setAccessible(true);
					icons = (IIcon[]) field.get(null);
				} catch (IllegalAccessException e) {
					DebugLogger.error("Cannot find TileCable texture", e);
					icons = new IIcon[2];
				} catch (NoSuchFieldException e) {
					DebugLogger.error("Cannot find TileCable texture", e);
					icons = new IIcon[2];
				}
				PartCable.icons = icons;
			}

			return icons;
		}

		@Override
		public IIcon getBlockIcon(Block block, IBlockAccess world, int x, int y, int z, int side) {
			int dir = -1;

			if (cable.doesConnectVisually(ForgeDirection.WEST) || cable.doesConnectVisually(ForgeDirection.EAST)) {
				dir = 4;
			}
			if (cable.doesConnectVisually(ForgeDirection.UP) || cable.doesConnectVisually(ForgeDirection.DOWN)) {
				dir = dir == -1 ? 0 : -2;
			}
			if (cable.doesConnectVisually(ForgeDirection.NORTH) || cable.doesConnectVisually(ForgeDirection.SOUTH)) {
				dir = dir == -1 ? 2 : -2;
			}
			if (dir == -1) dir = 2;

			if (dir >= 0 && (side == dir || side == Facing.oppositeSide[dir])) {
				return getIcons()[1];
			}

			return getIcons()[0];
		}

		public void drawTile(IBlockAccess world, int x, int y, int z) {
			/*
				Caching cable connections is quite hard, so instead we update
				the connection list every tick.
				This means that we gain some performance (not looking up connections for icons as well)
				but we don't have render derpyness.
			 */
			cable.updateConnections();
			setWorld(world);

			Block block = ComputerCraft.Blocks.cable;
			setRenderBounds(MIN, MIN, MIN, MAX, MAX, MAX);
			renderStandardBlock(block, x, y, z);

			if (cable.doesConnect(ForgeDirection.DOWN)) {
				setRenderBounds(MIN, 0, MIN, MAX, MIN, MAX);
				renderStandardBlock(block, x, y, z);
			} else if (cable.doesConnectInternally(ForgeDirection.DOWN)) {
				setRenderBounds(MIN, 0 + RENDER_PADDING, MIN, MAX, MIN, MAX);
				renderStandardBlock(block, x, y, z);
			}

			if (cable.doesConnect(ForgeDirection.UP)) {
				setRenderBounds(MIN, MAX, MIN, MAX, 1, MAX);
				renderStandardBlock(block, x, y, z);
			} else if (cable.doesConnectInternally(ForgeDirection.UP)) {
				setRenderBounds(MIN, MAX, MIN, MAX, 1 - RENDER_PADDING, MAX);
				renderStandardBlock(block, x, y, z);
			}

			if (cable.doesConnect(ForgeDirection.NORTH)) {
				setRenderBounds(MIN, MIN, 0, MAX, MAX, MIN);
				renderStandardBlock(block, x, y, z);
			} else if (cable.doesConnectInternally(ForgeDirection.NORTH)) {
				setRenderBounds(MIN, MIN, 0 + RENDER_PADDING, MAX, MAX, MIN);
				renderStandardBlock(block, x, y, z);
			}

			if (cable.doesConnect(ForgeDirection.SOUTH)) {
				setRenderBounds(MIN, MIN, MAX, MAX, MAX, 1);
				renderStandardBlock(block, x, y, z);
			} else if (cable.doesConnectInternally(ForgeDirection.SOUTH)) {
				setRenderBounds(MIN, MIN, MAX, MAX, MAX, 1 - RENDER_PADDING);
				renderStandardBlock(block, x, y, z);
			}

			if (cable.doesConnect(ForgeDirection.WEST)) {
				setRenderBounds(0, MIN, MIN, MIN, MAX, MAX);
				renderStandardBlock(block, x, y, z);
			} else if (cable.doesConnectInternally(ForgeDirection.WEST)) {
				setRenderBounds(0 + RENDER_PADDING, MIN, MIN, MIN, MAX, MAX);
				renderStandardBlock(block, x, y, z);
			}

			if (cable.doesConnect(ForgeDirection.EAST)) {
				setRenderBounds(MAX, MIN, MIN, 1, MAX, MAX);
				renderStandardBlock(block, x, y, z);
			} else if (cable.doesConnectInternally(ForgeDirection.EAST)) {
				setRenderBounds(MAX, MIN, MIN, 1 - RENDER_PADDING, MAX, MAX);
				renderStandardBlock(block, x, y, z);
			}
		}
	}
}
