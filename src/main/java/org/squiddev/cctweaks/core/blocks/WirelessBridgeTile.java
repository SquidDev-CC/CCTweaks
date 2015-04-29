package org.squiddev.cctweaks.core.blocks;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.squiddev.cctweaks.api.IDataCard;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.core.network.NetworkBinding;

/**
 * Create a new networked item
 */
public class WirelessBridgeTile extends NetworkedTile {
	protected final NetworkBinding binding = new NetworkBinding(this);

	@Override
	public Iterable<IWorldPosition> getExtraNodes() {
		return binding.getPositions();
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		binding.load(tag);
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		binding.save(tag);
	}

	@Override
	public void onRemove() {
		binding.remove();
	}

	@Override
	public void setWorldObj(World world) {
		super.setWorldObj(world);
		binding.add();
	}

	public boolean onActivated(ItemStack stack, IDataCard card, EntityPlayer player) {
		if (player.isSneaking()) {
			binding.save(stack, card);
			card.notifyPlayer(player, IDataCard.Messages.Stored);
			return true;
		} else if (binding.load(stack, card)) {
			card.notifyPlayer(player, IDataCard.Messages.Loaded);
			return true;
		}

		return false;
	}

}
