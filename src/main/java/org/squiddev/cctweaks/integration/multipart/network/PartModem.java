package org.squiddev.cctweaks.integration.multipart.network;

import mcmultipart.multipart.ISlottedPart;
import mcmultipart.multipart.PartSlot;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import org.squiddev.cctweaks.api.network.IWorldNetworkNode;
import org.squiddev.cctweaks.api.network.IWorldNetworkNodeHost;
import org.squiddev.cctweaks.integration.multipart.PartBase;

import java.util.EnumSet;
import java.util.List;

public class PartModem extends PartBase implements IWorldNetworkNodeHost, ISlottedPart {
	private EnumFacing facing;
	private EnumSet<PartSlot> slot;

	public PartModem() {

	}

	@Override
	public EnumSet<PartSlot> getSlotMask() {
		return null;
	}

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

	}

	@Override
	public void addCollisionBoxes(AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity) {

	}

	@Override
	public void addOcclusionBoxes(List<AxisAlignedBB> list) {

	}
}
