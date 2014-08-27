package squiddev.cctweaks.interfaces;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface IAdditionalTooltip {
	@SideOnly(Side.CLIENT)
	public void addAdditionalTooltip(ItemStack stack, EntityPlayer player, List<String> list, boolean bool);
}
