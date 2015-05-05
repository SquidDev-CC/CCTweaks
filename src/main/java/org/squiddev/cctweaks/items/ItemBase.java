package org.squiddev.cctweaks.items;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.core.registry.IModule;

public abstract class ItemBase extends Item implements IModule {
	protected final String name;

	public ItemBase(String itemName, int stackSize) {
		name = itemName;

		setUnlocalizedName(CCTweaks.RESOURCE_DOMAIN + "." + itemName);
		setTextureName(CCTweaks.RESOURCE_DOMAIN + ":" + itemName);

		setCreativeTab(CCTweaks.getCreativeTab());
		setMaxStackSize(stackSize);
	}

	public ItemBase(String itemName) {
		this(itemName, 64);
	}

	public NBTTagCompound getTag(ItemStack stack) {
		NBTTagCompound tag = stack.getTagCompound();
		if (tag == null) stack.setTagCompound(tag = new NBTTagCompound());
		return tag;
	}

	@Override
	public boolean canLoad() {
		return true;
	}

	@Override
	public void preInit() {
		GameRegistry.registerItem(this, name);
	}

	@Override
	public void init() {
	}
}
