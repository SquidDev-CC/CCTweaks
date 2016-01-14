package org.squiddev.cctweaks.integration.multipart.network;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import org.squiddev.cctweaks.api.network.IWorldNetworkNode;
import org.squiddev.cctweaks.api.network.IWorldNetworkNodeHost;
import org.squiddev.cctweaks.integration.multipart.PartSided;

import java.util.List;

public class PartModem extends PartSided implements IWorldNetworkNodeHost {
	/**
	 * Occlusion for collision detection
	 */
	public static final AxisAlignedBB[] OCCLUSION = new AxisAlignedBB[]{
		new AxisAlignedBB(0.125, 0.0, 0.125, 0.875, 0.125, 0.875D),
		new AxisAlignedBB(0.125, 0.875, 0.125, 0.875, 1.0, 0.875D),
		new AxisAlignedBB(0.125, 0.125, 0.0, 0.875, 0.875, 0.125D),
		new AxisAlignedBB(0.125, 0.125, 0.875, 0.875, 0.875, 1.0D),
		new AxisAlignedBB(0.0, 0.125, 0.125, 0.125, 0.875, 0.875D),
		new AxisAlignedBB(0.875, 0.125, 0.125, 1.0, 0.875, 0.875D),
	};

	/**
	 * Slightly smaller bounds
	 */
	public static final AxisAlignedBB[] BOUNDS = new AxisAlignedBB[]{
		new AxisAlignedBB(0.125, 0.0, 0.125, 0.875, 0.1875, 0.875),
		new AxisAlignedBB(0.125, 0.8125, 0.125, 0.875, 1.0, 0.875),
		new AxisAlignedBB(0.125, 0.125, 0.0, 0.875, 0.875, 0.1875),
		new AxisAlignedBB(0.125, 0.125, 0.8125, 0.875, 0.875, 1.0),
		new AxisAlignedBB(0.0, 0.125, 0.125, 0.1875, 0.875, 0.875),
		new AxisAlignedBB(0.8125, 0.125, 0.125, 1.0, 0.875, 0.875),
	};

	@Override
	public IWorldNetworkNode getNode() {
		return null;
	}

	@Override
	public Block getBlock() {
		return null;
	}

	@Override
	public void addSelectionBoxes(List<AxisAlignedBB> list) {
		list.add(OCCLUSION[getSide().ordinal()]);
	}

	@Override
	public void addCollisionBoxes(AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity) {
		AxisAlignedBB bounds = OCCLUSION[getSide().ordinal()];
		if (bounds.intersectsWith(mask)) list.add(bounds);
	}

	@Override
	public void addOcclusionBoxes(List<AxisAlignedBB> list) {
		list.add(BOUNDS[getSide().ordinal()]);
	}
}
