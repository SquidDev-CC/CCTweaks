package org.squiddev.cctweaks.integration.multipart.network;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
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
import org.squiddev.cctweaks.api.CCTweaksAPI;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.api.SingleTypeUnorderedPair;
import org.squiddev.cctweaks.api.network.*;
import org.squiddev.cctweaks.core.network.NetworkHelpers;
import org.squiddev.cctweaks.core.utils.DebugLogger;
import org.squiddev.cctweaks.integration.multipart.PartBase;

import java.lang.reflect.Field;
import java.util.*;

public class PartCable extends PartBase implements IWorldNetworkNode, TSlottedPart, ISidedHollowConnect {
	public static final String NAME = CCTweaks.NAME + ":networkCable";
	private static IIcon[] icons;

	public static final double MIN = 0.375;
	public static final double MAX = 1 - MIN;

	/**
	 * Side we are testing the connection on
	 *
	 * @see #canCableExtendInDirection(ForgeDirection)
	 */
	private ForgeDirection connectionTestSide = ForgeDirection.UNKNOWN;

	/**
	 * Caches list of connections that occur internally
	 * - nodes that are inside the multipart.
	 *
	 * Each byte is represented with {@code 1 << side}
	 */
	private int internalConnection = 0;

	/**
	 * Caches list of where the cable can travel to
	 * 0 if the route is blocked
	 *
	 * Each byte is represented with {@code 1 << side}
	 */
	private int cableConnection = 0;

	/**
	 * Attached network
	 */
	private INetworkController networkController;

	@SideOnly(Side.CLIENT)
	private CableRenderer render;

	@SideOnly(Side.CLIENT)
	public CableRenderer getRender() {
		if (render == null) {
			render = new CableRenderer();
		}
		return render;
	}

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
			if (shouldConnect(ForgeDirection.WEST)) xMin = 0.0D;
			if (shouldConnect(ForgeDirection.EAST)) xMax = 1.0D;
			if (shouldConnect(ForgeDirection.DOWN)) yMin = 0.0D;
			if (shouldConnect(ForgeDirection.UP)) yMax = 1.0D;
			if (shouldConnect(ForgeDirection.NORTH)) zMin = 0.0D;
			if (shouldConnect(ForgeDirection.SOUTH)) zMax = 1.0D;
		}

		return new Cuboid6(xMin, yMin, zMin, xMax, yMax, zMax);
	}

	@Override
	public Iterable<IndexedCuboid6> getSubParts() {
		List<IndexedCuboid6> parts = new ArrayList<IndexedCuboid6>();
		parts.add(new IndexedCuboid6(ForgeDirection.UNKNOWN, new Cuboid6(MIN, MIN, MIN, MAX, MAX, MAX)));

		if (tile() != null) {
			if (shouldConnect(ForgeDirection.WEST)) {
				parts.add(new IndexedCuboid6(ForgeDirection.WEST, new Cuboid6(0, MIN, MIN, MIN, MAX, MAX)));
			}
			if (shouldConnect(ForgeDirection.EAST)) {
				parts.add(new IndexedCuboid6(ForgeDirection.EAST, new Cuboid6(MAX, MIN, MIN, 1, MAX, MAX)));
			}
			if (shouldConnect(ForgeDirection.DOWN)) {
				parts.add(new IndexedCuboid6(ForgeDirection.DOWN, new Cuboid6(MIN, 0, MIN, MAX, MIN, MAX)));
			}
			if (shouldConnect(ForgeDirection.UP)) {
				parts.add(new IndexedCuboid6(ForgeDirection.UP, new Cuboid6(MIN, MAX, MIN, MAX, 1, MAX)));
			}
			if (shouldConnect(ForgeDirection.NORTH)) {
				parts.add(new IndexedCuboid6(ForgeDirection.NORTH, new Cuboid6(MIN, MIN, 0, MAX, MAX, MIN)));
			}
			if (shouldConnect(ForgeDirection.SOUTH)) {
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
			networkController.removeNode(this);
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
	public ItemStack getItem() {
		return PeripheralItemFactory.create(PeripheralType.Cable, null, 1);
	}

	@Override
	public void onPartChanged(TMultiPart part) {
		// Fire a network changed event when the entire part is modified.
		// This is because it may block a connection or release a new one
		if (tile() != null) {
			if (!world().isRemote && rebuildConnections()) {
				sendDescUpdate();
			}
		}
	}

	@Override
	public void onWorldJoin() {
		rebuildConnections();
	}

	@Override
	public int getHollowSize(int i) {
		return 4;
	}

	@Override
	public boolean doesTick() {
		return false;
	}

	@Override
	public IWorldPosition getPosition() {
		return this;
	}

	@Override
	public boolean canConnect(ForgeDirection side) {
		return active && canConnectCached(cableConnection, side);
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
	protected boolean canCableExtendInDirection(ForgeDirection dir) {
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
	protected boolean canConnectInternally(ForgeDirection side) {
		TMultiPart part = tile().partMap(side.ordinal());
		return part != null && part instanceof INetworkNode;
	}

	/**
	 * Checks if we can connect to that side
	 *
	 * @param side The side to connect to
	 * @return If a connection can occur
	 */
	protected boolean shouldConnect(ForgeDirection side) {
		return shouldConnect(side, cableConnection);
	}

	protected boolean shouldConnect(ForgeDirection side, int connection) {
		return canConnectCached(connection, side) && NetworkHelpers.canConnect(world(), x(), y(), z(), side);
	}

	/**
	 * Rebuild the cache of connections
	 *
	 * @see #internalConnection
	 * @see #cableConnection
	 */
	protected boolean rebuildConnections() {
		// Always allow from ForgeDirection.UNKNOWN
		int internal = 1 << 6, cable = 1 << 6;
		int oldInternal = 0;

		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			int flag = 1 << direction.ordinal();

			if (shouldConnect(direction)) oldInternal |= flag;
			if (canConnectInternally(direction)) internal |= flag;
			if (canCableExtendInDirection(direction)) cable |= flag;
		}

		boolean changed = internalConnection != internal || cableConnection != cable;

		internalConnection = internal;
		cableConnection = cable;

		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			boolean shouldConnect = shouldConnect(direction);
			if (shouldConnect != shouldConnect(direction, oldInternal) && networkController != null) {
				int x = x() + direction.offsetX;
				int y = y() + direction.offsetY;
				int z = z() + direction.offsetZ;
				IWorldNetworkNode node = CCTweaksAPI.instance().networkRegistry().getNode(world(), x, y, z);
				if (shouldConnect) {
					networkController.formConnection(this, node);
				} else if (node != null) {
					networkController.breakConnection(new SingleTypeUnorderedPair<INetworkNode>(this, node));
				}
			}
		}

		return changed;
	}

	/**
	 * Check if we can connect using a particular flag
	 *
	 * @param direction Direction to check in
	 * @return If a connection should occur
	 * @see #rebuildConnections()
	 */
	protected boolean canConnectCached(int flags, ForgeDirection direction) {
		int flag = 1 << direction.ordinal();
		return (flags & flag) == flag;
	}

	@Override
	public void writeDesc(MCDataOutput packet) {
		packet.writeBoolean(true);
	}

	@Override
	public void readDesc(MCDataInput packet) {
		if (packet.readBoolean() && tile() != null) rebuildConnections();
	}

	@Override
	public Map<String, IPeripheral> getConnectedPeripherals() {
		return Collections.emptyMap();
	}

	@Override
	public void receivePacket(INetworkController networkController, Packet packet, double distanceTravelled) {
	}

	@Override
	public void networkInvalidated(Map<String, IPeripheral> oldPeripherals) {
	}

	@Override
	public Set<INetworkNode> getConnectedNodes() {
		Set<INetworkNode> nodes = new HashSet<INetworkNode>();

		for (TMultiPart part : tile().jPartList()) {
			if (part != this) {
				if (part instanceof INetworkNode) {
					nodes.add((INetworkNode) part);
				} else if (part instanceof IWorldNetworkNodeHost) {
					nodes.add(((IWorldNetworkNodeHost) part).getNode());
				}
			}
		}

		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			if (shouldConnect(direction)) {
				int x = x() + direction.offsetX;
				int y = y() + direction.offsetY;
				int z = z() + direction.offsetZ;
				IWorldNetworkNode node = CCTweaksAPI.instance().networkRegistry().getNode(world(), x, y, z);
				nodes.add(node);
			}
		}

		return nodes;
	}

	@Override
	public void detachFromNetwork() {
		networkController = null;
	}

	@Override
	public void attachToNetwork(INetworkController networkController) {
		this.networkController = networkController;
	}

	@Override
	public INetworkController getAttachedNetwork() {
		return networkController;
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

		protected int externalConnection;

		public IIcon[] getIcons() {
			IIcon[] icons;
			if ((icons = PartCable.icons) == null) {

				try {
					Field field = TileCable.class.getDeclaredField("s_cableIcons");
					field.setAccessible(true);
					icons = (IIcon[]) field.get(null);
				} catch (ReflectiveOperationException e) {
					DebugLogger.error("Cannot find TileCable texture", e);
					icons = new IIcon[2];
				}
				PartCable.icons = icons;
			}

			return icons;
		}

		/**
		 * Rebuild external connections
		 *
		 * We do this once per draw instead as this means we don't have to look it up
		 * for icons, but don't have client-server sync issues
		 *
		 * @see #externalConnection
		 */
		protected void rebuildExternal() {
			int external = 0;
			for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
				if (NetworkHelpers.canConnect(world(), x(), y(), z(), direction)) external |= 1 << direction.ordinal();
			}
			externalConnection = cableConnection & external;
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
			rebuildExternal();

			Block block = ComputerCraft.Blocks.cable;
			setRenderBounds(MIN, MIN, MIN, MAX, MAX, MAX);
			renderStandardBlock(block, x, y, z);

			int internal = internalConnection;
			int external = externalConnection;

			if (canConnectCached(external, ForgeDirection.DOWN)) {
				setRenderBounds(MIN, 0, MIN, MAX, MIN, MAX);
				renderStandardBlock(block, x, y, z);
			} else if (canConnectCached(internal, ForgeDirection.DOWN)) {
				setRenderBounds(MIN, 0 + RENDER_PADDING, MIN, MAX, MIN, MAX);
				renderStandardBlock(block, x, y, z);
			}

			if (canConnectCached(external, ForgeDirection.UP)) {
				setRenderBounds(MIN, MAX, MIN, MAX, 1, MAX);
				renderStandardBlock(block, x, y, z);
			} else if (canConnectCached(internal, ForgeDirection.UP)) {
				setRenderBounds(MIN, MAX, MIN, MAX, 1 - RENDER_PADDING, MAX);
				renderStandardBlock(block, x, y, z);
			}

			if (canConnectCached(external, ForgeDirection.NORTH)) {
				setRenderBounds(MIN, MIN, 0, MAX, MAX, MIN);
				renderStandardBlock(block, x, y, z);
			} else if (canConnectCached(internal, ForgeDirection.NORTH)) {
				setRenderBounds(MIN, MIN, 0 + RENDER_PADDING, MAX, MAX, MIN);
				renderStandardBlock(block, x, y, z);
			}

			if (canConnectCached(external, ForgeDirection.SOUTH)) {
				setRenderBounds(MIN, MIN, MAX, MAX, MAX, 1);
				renderStandardBlock(block, x, y, z);
			} else if (canConnectCached(internal, ForgeDirection.SOUTH)) {
				setRenderBounds(MIN, MIN, MAX, MAX, MAX, 1 - RENDER_PADDING);
				renderStandardBlock(block, x, y, z);
			}

			if (canConnectCached(external, ForgeDirection.WEST)) {
				setRenderBounds(0, MIN, MIN, MIN, MAX, MAX);
				renderStandardBlock(block, x, y, z);
			} else if (canConnectCached(internal, ForgeDirection.WEST)) {
				setRenderBounds(0 + RENDER_PADDING, MIN, MIN, MIN, MAX, MAX);
				renderStandardBlock(block, x, y, z);
			}

			if (canConnectCached(external, ForgeDirection.EAST)) {
				setRenderBounds(MAX, MIN, MIN, 1, MAX, MAX);
				renderStandardBlock(block, x, y, z);
			} else if (canConnectCached(internal, ForgeDirection.EAST)) {
				setRenderBounds(MAX, MIN, MIN, 1 - RENDER_PADDING, MAX, MAX);
				renderStandardBlock(block, x, y, z);
			}
		}

		/**
		 * Tests to see if there is something to connect to, either in the
		 * same block space or using {@link #shouldConnect(ForgeDirection)}
		 *
		 * @param side The side to check
		 * @return If we should appear to connect on that side.
		 */
		public boolean canVisuallyConnect(ForgeDirection side) {
			return canConnectCached(internalConnection | externalConnection, side);
		}
	}
}
