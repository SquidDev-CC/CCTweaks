package org.squiddev.cctweaks.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.squiddev.cctweaks.api.IDataCard;
import org.squiddev.cctweaks.core.utils.Helpers;

import java.util.List;

/**
 * Item to bind networks together
 */
public class ItemDataCard extends ItemBase implements IDataCard {
	public ItemDataCard() {
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
	public boolean doesSneakBypassUse(World world, BlockPos pos, EntityPlayer player) {
		return true;
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
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
