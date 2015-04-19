package org.squiddev.cctweaks.core.integration.multipart;

import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.render.TextureUtils;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TSlottedPart;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.peripheral.IPeripheral;
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
import org.squiddev.cctweaks.api.network.*;
import org.squiddev.cctweaks.core.utils.DebugLogger;

import java.lang.reflect.Field;
import java.util.*;

public class CablePart extends AbstractPart implements INetworkNode, TSlottedPart {
	public static final String NAME = CCTweaks.NAME + ":networkCable";
	private static IIcon[] icons;

	public static final double MIN = 0.375;
	public static final double MAX = 1 - MIN;

	private ForgeDirection connectionTestSide = ForgeDirection.UNKNOWN;

	@SideOnly(Side.CLIENT)
	private CableRenderer render;

	@SideOnly(Side.CLIENT)
	public CableRenderer getRender() {
		CableRenderer draw = render;
		if (draw == null) {
			draw = render = new CableRenderer();
		}
		return draw;
	}

	private final Object lock = new Object();

	private boolean active = true;

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
			if (canConnect(ForgeDirection.WEST)) xMin = 0.0D;
			if (canConnect(ForgeDirection.EAST)) xMax = 1.0D;
			if (canConnect(ForgeDirection.DOWN)) yMin = 0.0D;
			if (canConnect(ForgeDirection.UP)) yMax = 1.0D;
			if (canConnect(ForgeDirection.NORTH)) zMin = 0.0D;
			if (canConnect(ForgeDirection.SOUTH)) zMax = 1.0D;
		}

		return new Cuboid6(xMin, yMin, zMin, xMax, yMax, zMax);
	}

	@Override
	public Iterable<IndexedCuboid6> getSubParts() {
		List<IndexedCuboid6> parts = new ArrayList<IndexedCuboid6>();
		parts.add(new IndexedCuboid6(ForgeDirection.UNKNOWN, new Cuboid6(MIN, MIN, MIN, MAX, MAX, MAX)));

		if (tile() != null) {
			if (canConnect(ForgeDirection.WEST)) {
				parts.add(new IndexedCuboid6(ForgeDirection.WEST, new Cuboid6(0, MIN, MIN, MIN, MAX, MAX)));
			}
			if (canConnect(ForgeDirection.EAST)) {
				parts.add(new IndexedCuboid6(ForgeDirection.EAST, new Cuboid6(MAX, MIN, MIN, 1, MAX, MAX)));
			}
			if (canConnect(ForgeDirection.DOWN)) {
				parts.add(new IndexedCuboid6(ForgeDirection.DOWN, new Cuboid6(MIN, 0, MIN, MAX, MIN, MAX)));
			}
			if (canConnect(ForgeDirection.UP)) {
				parts.add(new IndexedCuboid6(ForgeDirection.UP, new Cuboid6(MIN, MAX, MIN, MAX, 1, MAX)));
			}
			if (canConnect(ForgeDirection.NORTH)) {
				parts.add(new IndexedCuboid6(ForgeDirection.NORTH, new Cuboid6(MIN, MIN, 0, MAX, MAX, MIN)));
			}
			if (canConnect(ForgeDirection.SOUTH)) {
				parts.add(new IndexedCuboid6(ForgeDirection.SOUTH, new Cuboid6(MIN, MIN, MAX, MAX, MAX, 1)));
			}
		}

		return parts;
	}

	@Override
	public void harvest(MovingObjectPosition hit, EntityPlayer player) {
		World world = world();
		int x = x(), y = y(), z = z();

		super.harvest(hit, player);

		// Prevent visiting the node
		active = false;

		if (!world.isRemote) {
			NetworkHelpers.fireNetworkInvalidateAdjacent(world, x, y, z);
		}
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
	public ItemStack pickItem(MovingObjectPosition hit) {
		return PeripheralItemFactory.create(PeripheralType.Cable, null, 1);
	}

	@Override
	public boolean canBeVisited(ForgeDirection from) {
		return active && canCableExtendInDirection(from);
	}

	@Override
	public boolean canVisitTo(ForgeDirection to) {
		return active && canConnect(to);
	}

	public boolean canConnect(ForgeDirection dir) {
		if (!canCableExtendInDirection(dir)) {
			return false;
		}

		INetworkNode node = NetworkRegistry.getNode(
			world(),
			x() + dir.offsetX,
			y() + dir.offsetY,
			z() + dir.offsetZ
		);

		if (node == null) return false;

		return node.canBeVisited(dir.getOpposite());
	}

	/**
	 * Tests if the cable can pass through a side.
	 * Uses TileMultipart.canReplacePart to see if a version of this cable with
	 * with a certain side's occlusion extended to the full length
	 * can be placed in the multipart.
	 * If not, there's a cover or something in the way.
	 * Else, there's no cover, or something like a hollow cover.
	 *
	 * @param dir The direction to test in
	 * @return whether the cable can extend in that direction.
	 */
	public boolean canCableExtendInDirection(ForgeDirection dir) {
		connectionTestSide = dir;
		boolean occludes = tile().canReplacePart(this, this);
		connectionTestSide = ForgeDirection.UNKNOWN;
		return occludes;
	}

	/**
	 * Checks if this connects to a node inside this multipart
	 *
	 * @param side The side to check
	 * @return If there is a multipart on that sides
	 */
	public boolean canConnectInternally(ForgeDirection side) {
		TMultiPart part = tile().partMap(side.ordinal());
		return part != null && part instanceof INetworkNode;
	}

	@Override
	public Map<String, IPeripheral> getConnectedPeripherals() {
		Map<String, IPeripheral> peripherals = new HashMap<String, IPeripheral>();

		for (TMultiPart part : tile().jPartList()) {
			if (part instanceof INetworkNode && part != this) {
				Map<String, IPeripheral> nodePeripherals = ((INetworkNode) part).getConnectedPeripherals();
				if (nodePeripherals != null) peripherals.putAll(nodePeripherals);
			}
		}

		return peripherals;
	}

	@Override
	public void receivePacket(Packet packet, int distanceTravelled) {
		for (TMultiPart part : tile().jPartList()) {
			if (part instanceof INetworkNode && part != this) {
				((INetworkNode) part).receivePacket(packet, distanceTravelled);
			}
		}
	}

	@Override
	public void networkInvalidated() {
		for (TMultiPart part : tile().jPartList()) {
			if (part instanceof INetworkNode && part != this) {
				((INetworkNode) part).networkInvalidated();
			}
		}
	}

	@Override
	public Iterable<NetworkVisitor.SearchLoc> getExtraNodes() {
		Set<NetworkVisitor.SearchLoc> nodes = new HashSet<NetworkVisitor.SearchLoc>();

		for (TMultiPart part : tile().jPartList()) {
			if (part instanceof INetworkNode && part != this) {
				Iterable<NetworkVisitor.SearchLoc> extras = ((INetworkNode) part).getExtraNodes();
				if (extras != null) {
					for (NetworkVisitor.SearchLoc extra : extras) {
						nodes.add(extra);
					}
				}
			}
		}

		return nodes;
	}

	@Override
	public void onPartChanged(TMultiPart part) {
		// Fire a network changed event when the entire part is modified.
		// This is because it may block a connection or release a new one
		if (tile() != null && !world().isRemote) {
			NetworkHelpers.fireNetworkInvalidateAdjacent(world(), x(), y(), z());
		}
	}

	@Override
	public Object lock() {
		return lock;
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
			if ((icons = CablePart.icons) == null) {

				try {
					Field field = TileCable.class.getDeclaredField("s_cableIcons");
					field.setAccessible(true);
					icons = (IIcon[]) field.get(null);
				} catch (ReflectiveOperationException e) {
					DebugLogger.error("Cannot find TileCable texture", e);
					icons = new IIcon[2];
				}
				CablePart.icons = icons;
			}

			return icons;
		}

		@Override
		public IIcon getBlockIcon(Block block, IBlockAccess world, int x, int y, int z, int side) {
			int dir = -1;

			if (canVisuallyConnect(ForgeDirection.WEST) || canVisuallyConnect(ForgeDirection.EAST)) {
				dir = dir == -1 ? 4 : -2;
			}
			if (canVisuallyConnect(ForgeDirection.UP) || canVisuallyConnect(ForgeDirection.DOWN)) {
				dir = dir == -1 ? 0 : -2;
			}
			if (canVisuallyConnect(ForgeDirection.NORTH) || canVisuallyConnect(ForgeDirection.SOUTH)) {
				dir = dir == -1 ? 2 : -2;
			}
			if (dir == -1) dir = 2;

			if ((dir >= 0) && ((side == dir) || (side == Facing.oppositeSide[dir]))) {
				return getIcons()[1];
			}

			return getIcons()[0];
		}

		public void drawTile(IBlockAccess world, int x, int y, int z) {
			setWorld(world);

			Block block = ComputerCraft.Blocks.cable;
			setRenderBounds(MIN, MIN, MIN, MAX, MAX, MAX);
			renderStandardBlock(block, x, y, z);

			if (canConnect(ForgeDirection.DOWN)) {
				setRenderBounds(MIN, 0, MIN, MAX, MIN, MAX);
				renderStandardBlock(block, x, y, z);
			} else if (canConnectInternally(ForgeDirection.DOWN)) {
				setRenderBounds(MIN, 0 + RENDER_PADDING, MIN, MAX, MIN, MAX);
				renderStandardBlock(block, x, y, z);
			}

			if (canConnect(ForgeDirection.UP)) {
				setRenderBounds(MIN, MAX, MIN, MAX, 1, MAX);
				renderStandardBlock(block, x, y, z);
			} else if (canConnectInternally(ForgeDirection.UP)) {
				setRenderBounds(MIN, MAX, MIN, MAX, 1 - RENDER_PADDING, MAX);
				renderStandardBlock(block, x, y, z);
			}

			if (canConnect(ForgeDirection.NORTH)) {
				setRenderBounds(MIN, MIN, 0, MAX, MAX, MIN);
				renderStandardBlock(block, x, y, z);
			} else if (canConnectInternally(ForgeDirection.NORTH)) {
				setRenderBounds(MIN, MIN, 0 + RENDER_PADDING, MAX, MAX, MIN);
				renderStandardBlock(block, x, y, z);
			}

			if (canConnect(ForgeDirection.SOUTH)) {
				setRenderBounds(MIN, MIN, MAX, MAX, MAX, 1);
				renderStandardBlock(block, x, y, z);
			} else if (canConnectInternally(ForgeDirection.SOUTH)) {
				setRenderBounds(MIN, MIN, MAX, MAX, MAX, 1 - RENDER_PADDING);
				renderStandardBlock(block, x, y, z);
			}

			if (canConnect(ForgeDirection.WEST)) {
				setRenderBounds(0, MIN, MIN, MIN, MAX, MAX);
				renderStandardBlock(block, x, y, z);
			} else if (canConnectInternally(ForgeDirection.WEST)) {
				setRenderBounds(0 + RENDER_PADDING, MIN, MIN, MIN, MAX, MAX);
				renderStandardBlock(block, x, y, z);
			}

			if (canConnect(ForgeDirection.EAST)) {
				setRenderBounds(MAX, MIN, MIN, 1, MAX, MAX);
				renderStandardBlock(block, x, y, z);
			} else if (canConnectInternally(ForgeDirection.EAST)) {
				setRenderBounds(MAX, MIN, MIN, 1 - RENDER_PADDING, MAX, MAX);
				renderStandardBlock(block, x, y, z);
			}

			block.setBlockBoundsBasedOnState(world, x, y, z);
		}

		/**
		 * Tests to see if there is something to connect to, either in the
		 * same block space or using {@link #canConnect(ForgeDirection)}
		 *
		 * This is a render exclusive method so lives in
		 *
		 * @param side The side to check
		 * @return If we should appear to connect on that side.
		 */
		public boolean canVisuallyConnect(ForgeDirection side) {
			return canConnect(side) || canConnectInternally(side);
		}
	}
}
