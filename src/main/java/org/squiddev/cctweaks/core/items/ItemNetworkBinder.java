package org.squiddev.cctweaks.core.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.squiddev.cctweaks.api.IDataCard;

/**
 * Item to bind networks together
 */
public class ItemNetworkBinder extends ItemBase implements IDataCard {
	public ItemNetworkBinder() {
		super("networkBinder", 1);
	}

	@Override
	public void setSettings(ItemStack stack, String type, NBTTagCompound data) {
		NBTTagCompound tag = getTag(stack);
		tag.setString("type", type);
		tag.setTag("data", data);
	}

	@Override
	public String getType(ItemStack stack) {
		String name = getTag(stack).getString("type");
		return name == null || name.isEmpty() ? null : name;
	}

	@Override
	public NBTTagCompound getData(ItemStack stack) {
		return (NBTTagCompound) getTag(stack).getCompoundTag("data").copy();
	}

	@Override
	public void notifyPlayer(EntityPlayer player, Messages message) {
		player.addChatMessage(message.getChatMessage());
	}

	@Override
	public boolean doesSneakBypassUse(World world, int x, int y, int z, EntityPlayer player) {
		return true;
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		if (player.isSneaking() && !world.isRemote) {
			((IDataCard) stack.getItem()).notifyPlayer(player, Messages.Cleared);
			stack.setTagCompound(null);
			return true;
		}

		return false;
	}
}
