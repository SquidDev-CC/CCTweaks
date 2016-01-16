package org.squiddev.cctweaks.integration.multipart.network;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.peripheral.PeripheralType;
import dan200.computercraft.shared.peripheral.common.BlockCable;
import dan200.computercraft.shared.peripheral.common.PeripheralItemFactory;
import mcmultipart.MCMultiPartMod;
import mcmultipart.microblock.ISideHollowConnect;
import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.ISlottedPart;
import mcmultipart.multipart.PartSlot;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.api.network.INetworkNode;
import org.squiddev.cctweaks.api.network.IWorldNetworkNode;
import org.squiddev.cctweaks.api.network.IWorldNetworkNodeHost;
import org.squiddev.cctweaks.core.network.NetworkHelpers;
import org.squiddev.cctweaks.core.network.cable.CableWithInternalSidedParts;
import org.squiddev.cctweaks.integration.multipart.MultipartHelpers;
import org.squiddev.cctweaks.integration.multipart.PartBase;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class PartCable extends PartBase implements IWorldNetworkNodeHost, ISlottedPart, ISideHollowConnect {
	public static final double MIN = 0.375;
	public static final double MAX = 1 - MIN;

	public static final AxisAlignedBB[] BOXES = new AxisAlignedBB[]{
		new AxisAlignedBB(MIN, 0, MIN, MAX, MIN, MAX),
		new AxisAlignedBB(MIN, MAX, MIN, MAX, 1, MAX),
		new AxisAlignedBB(MIN, MIN, 0, MAX, MAX, MIN),
		new AxisAlignedBB(MIN, MIN, MAX, MAX, MAX, 1),
		new AxisAlignedBB(0, MIN, MIN, MIN, MAX, MAX),
		new AxisAlignedBB(MAX, MIN, MIN, 1, MAX, MAX),
		new AxisAlignedBB(MIN, MIN, MIN, MAX, MAX, MAX),
	};

	private final CableImpl cable = new CableImpl();
	private int canConnectMap = 0;

	//region Basic getters
	@Override
	public int getHollowSize(EnumFacing enumFacing) {
		return 4;
	}

	@Override
	public EnumSet<PartSlot> getSlotMask() {
		return EnumSet.of(PartSlot.CENTER);
	}

	@Override
	public Block getBlock() {
		return ComputerCraft.Blocks.cable;
	}

	@Override
	public ItemStack getStack() {
		return PeripheralItemFactory.create(PeripheralType.Cable, null, 1);
	}

	@Override
	public IWorldNetworkNode getNode() {
		return cable;
	}

	@Override
	public String getModelPath() {
		return "cctweaks:cable";
	}

	@Override
	public void addSelectionBoxes(List<AxisAlignedBB> list) {
		list.add(BOXES[6]);
		for (EnumFacing facing : EnumFacing.VALUES) {
			if (cable.doesConnectVisually(facing)) list.add(BOXES[facing.ordinal()]);
		}
	}

	@Override
	public void addCollisionBoxes(AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity) {
		if (BOXES[6].intersectsWith(mask)) list.add(BOXES[6]);

		for (EnumFacing facing : EnumFacing.VALUES) {
			if (cable.doesConnectVisually(facing)) {
				AxisAlignedBB box = BOXES[facing.ordinal()];
				if (box.intersectsWith(mask)) list.add(box);
			}
		}
	}

	@Override
	public void addOcclusionBoxes(List<AxisAlignedBB> list) {
		list.add(BOXES[6]);
	}

	@Override
	public BlockState createBlockState() {
		return new BlockState(
			MCMultiPartMod.multipart,
			BlockCable.Properties.NORTH,
			BlockCable.Properties.SOUTH,
			BlockCable.Properties.EAST,
			BlockCable.Properties.WEST,
			BlockCable.Properties.UP,
			BlockCable.Properties.DOWN
		);
	}

	@Override
	public IBlockState getExtendedState(IBlockState state) {
		rebuildCanConnectMap();
		cable.updateConnectionMaps();
		return state
			.withProperty(BlockCable.Properties.NORTH, cable.doesConnectVisually(EnumFacing.NORTH))
			.withProperty(BlockCable.Properties.SOUTH, cable.doesConnectVisually(EnumFacing.SOUTH))
			.withProperty(BlockCable.Properties.EAST, cable.doesConnectVisually(EnumFacing.EAST))
			.withProperty(BlockCable.Properties.WEST, cable.doesConnectVisually(EnumFacing.WEST))
			.withProperty(BlockCable.Properties.UP, cable.doesConnectVisually(EnumFacing.UP))
			.withProperty(BlockCable.Properties.DOWN, cable.doesConnectVisually(EnumFacing.DOWN));
	}

	//endregion

	/**
	 * Rebuild the cache of occluded sides
	 */
	private void rebuildCanConnectMap() {
		int map = 0;
		for (EnumFacing side : EnumFacing.VALUES) {
			if (MultipartHelpers.extendIn(this, BOXES[side.ordinal()], side)) map |= 1 << side.ordinal();
		}

		canConnectMap = map;
	}

	@Override
	public void onLoaded() {
		super.onLoaded();

		rebuildCanConnectMap();
		if (!getWorld().isRemote) NetworkHelpers.scheduleConnect(cable);
	}

	@Override
	public void onAdded() {
		super.onAdded();

		rebuildCanConnectMap();
		if (!getWorld().isRemote) cable.connect();
		updateConnections();
	}

	@Override
	public void onNeighborTileChange(EnumFacing facing) {
		super.onNeighborTileChange(facing);
		updateConnections();
	}

	@Override
	public void onNeighborBlockChange(Block block) {
		super.onNeighborBlockChange(block);
		updateConnections();
	}

	@Override
	public void onPartChanged(IMultipart part) {
		super.onPartChanged(part);

		rebuildCanConnectMap();
		updateConnections();
	}

	@Override
	public void onUnloaded() {
		if (!getWorld().isRemote) cable.destroy();
		super.onUnloaded();
	}

	@Override
	public void onRemoved() {
		if (!getWorld().isRemote) cable.destroy();
		super.onRemoved();
	}

	private void updateConnections() {
		if (getWorld().isRemote) {
			cable.updateConnectionMaps();
		} else {
			cable.updateConnections();
		}
	}

	private class CableImpl extends CableWithInternalSidedParts {
		@Override
		public Set<INetworkNode> getConnectedNodes() {
			Set<INetworkNode> nodes = super.getConnectedNodes();

			for (IMultipart part : getContainer().getParts()) {
				if (part != PartCable.this) {
					INetworkNode node = MultipartHelpers.getNode(part);
					if (node != null) nodes.add(node);
				}
			}

			return nodes;
		}

		@Override
		public boolean canConnectInternally(EnumFacing direction) {
			IMultipart part = getContainer().getPartInSlot(PartSlot.getFaceSlot(direction));
			return MultipartHelpers.getNode(part) != null;
		}

		@Override
		public IWorldPosition getPosition() {
			return PartCable.this;
		}

		@Override
		public boolean canConnect(EnumFacing direction) {
			int flag = 1 << direction.ordinal();
			return (canConnectMap & flag) == flag;
		}

		public boolean updateConnectionMaps() {
			return updateInternalConnectionMap() | updateConnectionMap();
		}
	}
}
