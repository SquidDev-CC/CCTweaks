package org.squiddev.cctweaks.core.items;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.squiddev.cctweaks.api.IDataCard;
import org.squiddev.cctweaks.core.utils.Helpers;

import java.util.List;

/**
 * Item to bind networks together
 */
public class DataCardItem extends BaseItem implements IDataCard {
	public DataCardItem() {
		super("dataCard", 1);
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
		return name == null || name.isEmpty() ? EMPTY : name;
	}

	@Override
	public NBTTagCompound getData(ItemStack stack) {
		if (!stack.hasTagCompound()) return null;
		return getTag(stack).getCompoundTag("data");
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

	@SuppressWarnings("unchecked")
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean extraInfo) {
		super.addInformation(stack, player, tooltip, extraInfo);

		String type = getType(stack);
		tooltip.add(Helpers.translateAny("gui.tooltip." + type, type));

		if (stack.hasTagCompound()) {
			NBTTagCompound data = stack.getTagCompound().getCompoundTag("data");
			if (data != null) {
				String msg;
				if ((msg = data.getString("tooltip")) != null && !msg.isEmpty()) tooltip.add(msg);
				if (extraInfo && (msg = data.getString("details")) != null && !msg.isEmpty()) tooltip.add(msg);
			}
		}
	}

	@Override
	public void init() {
		super.init();
		GameRegistry.addRecipe(new ItemStack(this),
			"SSS",
			"GRR",

			'G', Items.gold_ingot,
			'R', Items.redstone,
			'S', Blocks.stone
		);
	}
}
