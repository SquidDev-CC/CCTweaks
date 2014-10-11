package squiddev.cctweaks.items;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import squiddev.cctweaks.CCTweaks;
import squiddev.cctweaks.interfaces.IAdditionalTooltip;
import squiddev.cctweaks.reference.Localisation;
import squiddev.cctweaks.reference.ModInfo;
import squiddev.cctweaks.utils.KeyboardUtils;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemBase extends Item {

	private final String name;

	public ItemBase(String itemName, int stackSize) {
		name = itemName;

		setUnlocalizedName(ModInfo.RESOURCE_DOMAIN + "." + name);
		setCreativeTab(CCTweaks.creativeTab);
		setMaxStackSize(stackSize);
	}
	public ItemBase(String itemName) {
		this(itemName, 64);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister registry) {
		itemIcon = registry.registerIcon(ModInfo.RESOURCE_DOMAIN + ":" + name);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
		Item item = stack.getItem();
		if (item instanceof IAdditionalTooltip) {
			if (KeyboardUtils.isShiftKeyDown()) {
				((IAdditionalTooltip) item).addAdditionalTooltip(stack, player, list, bool);
			} else {
				list.add(Localisation.Tooltips.ShiftInfo.getLocalised());
			}
		}
	}

	public void registerItem(){
		GameRegistry.registerItem(this, getUnlocalizedName());
	}
}