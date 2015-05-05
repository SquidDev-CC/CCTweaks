package org.squiddev.cctweaks.blocks;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import org.squiddev.cctweaks.api.IWorldPosition;

/**
 * Base tile for all TileEntities
 */
public abstract class TileBase extends TileEntity implements IWorldPosition {
	@Override
	public int getX() {
		return xCoord;
	}

	@Override
	public int getY() {
		return yCoord;
	}

	@Override
	public int getZ() {
		return zCoord;
	}

	@Override
	public IBlockAccess getWorld() {
		return worldObj;
	}

	/**
	 * Called before the block is removed or on chunk unload
	 */
	public void preRemove() {
	}

	/**
	 * Called after being removed
	 */
	public void postRemove() {
	}

	@Override
	public void onChunkUnload() {
		preRemove();
	}

	/**
	 * Called to save data for the client
	 *
	 * @param tag The data to send
	 * @return If data needs to be sent
	 */
	protected boolean writeDescription(NBTTagCompound tag) {
		return false;
	}

	/**
	 * Read data from the server
	 *
	 * @param tag The data to read
	 */
	protected void readDescription(NBTTagCompound tag) {
	}

	public final Packet getDescriptionPacket() {
		NBTTagCompound tag = new NBTTagCompound();
		return writeDescription(tag) ? new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, tag) : null;
	}

	public final void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) {
		switch (packet.func_148853_f()) {
			case 0:
				readDescription(packet.func_148857_g());
				break;
		}
	}

	/**
	 * Improvement over {@link #markDirty()}
	 */
	public void markForUpdate() {
		markDirty();
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}
}
