package org.squiddev.cctweaks.integration.multipart.network;

import dan200.computercraft.ComputerCraft;
import mcmultipart.microblock.ISideHollowConnect;
import mcmultipart.multipart.ISlottedPart;
import mcmultipart.multipart.PartSlot;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import org.squiddev.cctweaks.integration.multipart.PartBase;

import java.util.EnumSet;
import java.util.List;

public class PartCable extends PartBase implements ISideHollowConnect, ISlottedPart {
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
		return new ItemStack(getBlock());
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
