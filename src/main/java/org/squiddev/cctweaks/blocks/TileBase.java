package org.squiddev.cctweaks.blocks;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import org.squiddev.cctweaks.api.IWorldPosition;

import javax.annotation.Nonnull;

/**
 * Base tile for all TileEntities
 */
public abstract class TileBase extends TileEntity implements IWorldPosition {
	@Nonnull
	@Override
	public BlockPos getPosition() {
		return getPos();
	}

	/**
	 * Called when the TileEntity is validated
	 */
	public void create() {
	}

	/**
	 * Called when the TileEntity is destroyed
	 */
	public void destroy() {
	}

	@Override
	public void validate() {
		if (worldObj == null || !worldObj.isRemote) {
			create();
		}
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		if (worldObj == null || !worldObj.isRemote) {
			destroy();
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		if (worldObj == null || !worldObj.isRemote) {
			destroy();
		}
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

	@Override
	public final Packet<?> getDescriptionPacket() {
		NBTTagCompound tag = new NBTTagCompound();
		return writeDescription(tag) ? new S35PacketUpdateTileEntity(pos, 0, tag) : null;
	}

	@Override
	public final void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) {
		readDescription(packet.getNbtCompound());
	}

	/**
	 * Improvement over {@link #markDirty()}
	 */
	public void markForUpdate() {
		markDirty();
		worldObj.markBlockForUpdate(pos);
	}

	/**
	 * Called when the block is activated
	 *
	 * @param player The player who triggered this
	 * @param side   The side the block is activated on
	 * @return If the event succeeded
	 */
	public boolean onActivated(EntityPlayer player, EnumFacing side) {
		return false;
	}

	/**
	 * Called when a neighbor tile/block changed
	 */
	public void onNeighborChanged() {
	}

	@Nonnull
	@Override
	public IBlockAccess getBlockAccess() {
		return worldObj;
	}
}
