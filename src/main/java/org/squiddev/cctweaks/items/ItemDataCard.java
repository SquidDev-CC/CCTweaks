package org.squiddev.cctweaks.items;

import joptsimple.internal.Strings;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.squiddev.cctweaks.api.IDataCard;
import org.squiddev.cctweaks.core.utils.Helpers;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Item to bind networks together
 */
public class ItemDataCard extends ItemBase implements IDataCard {
	public ItemDataCard() {
		super("dataCard", 1);
	}

	@Override
	public void setSettings(@Nonnull ItemStack stack, @Nonnull String type, NBTTagCompound data) {
		NBTTagCompound tag = getTag(stack);
		tag.setString("type", type);
		tag.setTag("data", data);
	}

	@Nonnull
	@Override
	public String getType(@Nonnull ItemStack stack) {
		String name = getTag(stack).getString("type");
		return name == null || name.isEmpty() ? EMPTY : name;
	}

	@Override
	public NBTTagCompound getData(@Nonnull ItemStack stack) {
		if (!stack.hasTagCompound()) return null;
		return getTag(stack).getCompoundTag("data");
	}

	@Override
	public void notifyPlayer(@Nonnull EntityPlayer player, @Nonnull Messages message) {
		player.sendMessage(message.getChatMessage());
	}

	@Override
	public boolean doesSneakBypassUse(ItemStack stack, IBlockAccess world, BlockPos pos, EntityPlayer player) {
		return true;
	}

	@Nonnull
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (player.isSneaking()) {
			if (world.isRemote) return EnumActionResult.SUCCESS;

			ItemStack stack = player.getHeldItem(hand);
			((IDataCard) stack.getItem()).notifyPlayer(player, Messages.Cleared);
			stack.setTagCompound(null);
			return EnumActionResult.SUCCESS;
		}

		return EnumActionResult.PASS;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
		super.addInformation(stack, world, tooltip, flag);

		String type = getType(stack);
		tooltip.add(Helpers.translateAny("gui.tooltip." + type, type));

		if (stack.hasTagCompound()) {
			NBTTagCompound data = stack.getTagCompound().getCompoundTag("data");
			if (data != null) {
				String msg;
				if (!Strings.isNullOrEmpty(msg = data.getString("tooltip"))) tooltip.add(msg);
				if (flag.isAdvanced() && !Strings.isNullOrEmpty(msg = data.getString("details"))) tooltip.add(msg);
			}
		}
	}
}
