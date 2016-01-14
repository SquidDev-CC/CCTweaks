package org.squiddev.cctweaks.integration.multipart;

import mcmultipart.MCMultiPartMod;
import mcmultipart.multipart.ISlottedPart;
import mcmultipart.multipart.PartSlot;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;

import java.util.EnumSet;

public abstract class PartSided extends PartBase implements ISlottedPart {
	public static final PropertyDirection SIDE = PropertyDirection.create("side");

	private EnumFacing side;
	private EnumSet<PartSlot> slot;

	@Override
	public EnumFacing[] getValidRotations() {
		return EnumFacing.VALUES;
	}

	public void setSide(EnumFacing direction) {
		side = direction;
		slot = EnumSet.of(PartSlot.getFaceSlot(direction));
	}

	public final EnumFacing getSide() {
		return side;
	}

	@Override
	public boolean rotatePart(EnumFacing axis) {
		setSide(side.rotateAround(axis.getAxis()));
		return true;
	}

	@Override
	public EnumSet<PartSlot> getSlotMask() {
		return slot;
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		tag.setByte("side", (byte) side.ordinal());
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		setSide(EnumFacing.getFront(tag.getByte("side")));
	}

	@Override
	public void writeUpdatePacket(PacketBuffer buf) {
		super.writeUpdatePacket(buf);
		buf.writeByte(side.ordinal());
	}

	@Override
	public void readUpdatePacket(PacketBuffer buf) {
		super.readUpdatePacket(buf);
		setSide(EnumFacing.getFront(buf.readByte()));
	}

	@Override
	public BlockState createBlockState() {
		return new BlockState(MCMultiPartMod.multipart, SIDE);
	}

	@Override
	public IBlockState getExtendedState(IBlockState state) {
		return state.withProperty(SIDE, side);
	}
}
