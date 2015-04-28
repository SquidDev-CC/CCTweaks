package org.squiddev.cctweaks.core.blocks;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.squiddev.cctweaks.api.IDataCard;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.core.network.NetworkBindings;

import java.util.UUID;

/**
 * Create a new networked item
 */
public class WirelessBridgeTile extends NetworkedTile {
	protected static final String MSB = "bound_id_msb";
	protected static final String LSB = "bound_id_lsb";
	protected UUID id = UUID.randomUUID();

	@Override
	public Iterable<IWorldPosition> getExtraNodes() {
		return NetworkBindings.getPositions(id);
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		readNBT(tag);
	}

	public boolean readNBT(NBTTagCompound tag) {
		if (tag.hasKey(MSB) && tag.hasKey(LSB)) {
			UUID newId = new UUID(tag.getLong(MSB), tag.getLong(LSB));
			if (!newId.equals(id)) setId(newId);
			return true;
		}

		return false;
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		writeNBT(tag);
	}

	public NBTTagCompound writeNBT(NBTTagCompound tag) {
		tag.setLong(MSB, id.getMostSignificantBits());
		tag.setLong(LSB, id.getLeastSignificantBits());
		return tag;
	}

	@Override
	public void onChunkUnload() {
		NetworkBindings.removePosition(id, this);
	}

	@Override
	public void setWorldObj(World world) {
		super.setWorldObj(world);
		if (worldObj != null) NetworkBindings.addPosition(id, this);
	}

	protected void setId(UUID newId) {
		if (worldObj != null) {
			NetworkBindings.removePosition(id, this);
			id = newId;
			NetworkBindings.addPosition(newId, this);
		} else {
			id = newId;
		}
	}

	public boolean onActivated(ItemStack stack, IDataCard card, EntityPlayer player) {
		if (player.isSneaking()) {
			NBTTagCompound data = writeNBT(new NBTTagCompound());
			// Write the UUID to the details tag so we can debug easily
			data.setString("details", id.toString());

			card.setSettings(stack, NetworkBindings.BINDING_NAME, data);
			card.notifyPlayer(player, IDataCard.Messages.Stored);
			return true;
		} else if (card.getType(stack).equals(NetworkBindings.BINDING_NAME) && readNBT(card.getData(stack))) {
			card.notifyPlayer(player, IDataCard.Messages.Loaded);
			return true;
		}

		return false;
	}

}
