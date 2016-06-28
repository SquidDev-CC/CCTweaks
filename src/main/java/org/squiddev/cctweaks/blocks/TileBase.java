package org.squiddev.cctweaks.blocks;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.squiddev.cctweaks.api.IWorldPosition;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Base tile for all TileEntities
 */
public abstract class TileBase extends TileEntity implements IWorldPosition {
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

	@Nullable
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		NBTTagCompound tag = new NBTTagCompound();
		return writeDescription(tag) ? new SPacketUpdateTileEntity(pos, 0, tag) : null;
	}

	@Nonnull
	@Override
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound tag = super.getUpdateTag();
		writeDescription(tag);
		return tag;
	}

	@Override
	public final void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
		readDescription(packet.getNbtCompound());
	}

	@Override
	public void handleUpdateTag(@Nonnull NBTTagCompound tag) {
		super.handleUpdateTag(tag);
		readDescription(tag);
	}

	/**
	 * Improvement over {@link #markDirty()}
	 */
	public void markForUpdate() {
		markDirty();
		// TODO: worldObj.markBlockForUpdate(pos);
	}

	/**
	 * Called when the block is activated
	 *
	 * @param player The player who triggered this
	 * @param side   The side the block is activated on
	 * @return If the event succeeded
	 */
	public boolean onActivated(EntityPlayer player, EnumFacing side, EnumHand hand, @Nullable ItemStack stack) {
		return false;
	}

	/**
	 * Called when a neighbor tile/block changed
	 */
	public void onNeighborChanged() {
	}

	@Override
	public IBlockAccess getBlockAccess() {
		return worldObj;
	}
}
