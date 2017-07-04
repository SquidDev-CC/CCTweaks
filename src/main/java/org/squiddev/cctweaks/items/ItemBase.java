package org.squiddev.cctweaks.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.core.registry.IModule;
import org.squiddev.cctweaks.core.utils.Helpers;

public abstract class ItemBase extends Item implements IModule {
	protected final String name;

	public ItemBase(String itemName, int stackSize) {
		name = itemName;

		setUnlocalizedName(CCTweaks.ID + "." + name);

		setCreativeTab(CCTweaks.getCreativeTab());
		setMaxStackSize(stackSize);
	}

	public ItemBase(String itemName) {
		this(itemName, 64);
	}

	public static NBTTagCompound getTag(ItemStack stack) {
		NBTTagCompound tag = stack.getTagCompound();
		if (tag == null) stack.setTagCompound(tag = new NBTTagCompound());
		return tag;
	}

	@Override
	public void preInit() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void registerItems(RegistryEvent.Register<Item> event) {
		event.getRegistry().register(this.setRegistryName(new ResourceLocation(CCTweaks.ID, name)));
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void registerModels(ModelRegistryEvent event) {
		Helpers.setupModel(this, 0, name);
	}
}
