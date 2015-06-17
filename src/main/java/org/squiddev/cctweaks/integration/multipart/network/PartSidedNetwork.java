package org.squiddev.cctweaks.integration.multipart.network;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.vec.Cuboid6;
import codechicken.multipart.TSlottedPart;
import net.minecraft.nbt.NBTTagCompound;
import org.squiddev.cctweaks.api.network.IWorldNetworkNodeHost;
import org.squiddev.cctweaks.integration.multipart.MultipartHelpers;
import org.squiddev.cctweaks.integration.multipart.PartBase;

import java.util.Collections;

/**
 * An abstract network to help with creating network nodes.
 * This takes the size/drawing style of a modem
 */
public abstract class PartSidedNetwork extends PartBase implements IWorldNetworkNodeHost, TSlottedPart {
	protected byte direction;

	@Override
	public Iterable<Cuboid6> getOcclusionBoxes() {
		return Collections.singletonList(MultipartHelpers.peripheralOcclusion(direction));
	}

	@Override
	public Cuboid6 getBounds() {
		return MultipartHelpers.peripheralBounds(direction);
	}

	@Override
	public Iterable<IndexedCuboid6> getSubParts() {
		return Collections.singletonList(new IndexedCuboid6(direction, getBounds()));
	}

	@Override
	public int getSlotMask() {
		return 1 << direction;
	}

	@Override
	public void writeDesc(MCDataOutput packet) {
		super.writeDesc(packet);
		packet.writeByte(direction);
	}

	@Override
	public void readDesc(MCDataInput packet) {
		super.readDesc(packet);
		direction = packet.readByte();
	}

	@Override
	public void save(NBTTagCompound tag) {
		super.save(tag);
		tag.setByte("node_direction", direction);
	}

	@Override
	public void load(NBTTagCompound tag) {
		super.load(tag);
		direction = tag.getByte("node_direction");
	}

	@Override
	public boolean doesTick() {
		return false;
	}
}
