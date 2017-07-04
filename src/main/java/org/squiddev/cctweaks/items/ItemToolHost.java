package org.squiddev.cctweaks.items;

import dan200.computercraft.ComputerCraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.squiddev.cctweaks.core.utils.Helpers;
import org.squiddev.cctweaks.turtle.TurtleUpgradeToolHost;
import org.squiddev.cctweaks.turtle.TurtleUpgradeToolManipulator;

import javax.annotation.Nonnull;

/**
 * Simply a holder item for the turtle tool host
 */
public class ItemToolHost extends ItemBase {
	public ItemToolHost() {
		super("toolHost");
		setHasSubtypes(true);
	}

	@Override
	public void getSubItems(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> list) {
		if (!isInCreativeTab(tab)) return;
		list.add(new ItemStack(this, 1, 0));
		list.add(new ItemStack(this, 1, 1));
	}

	@Nonnull
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		switch (stack.getItemDamage()) {
			case 0:
			default:
				return getUnlocalizedName();
			case 1:
				return getUnlocalizedName() + ".advanced";
		}
	}

	@Override
	public int getMetadata(int metadata) {
		return metadata;
	}

	@Override
	public void init() {
		ComputerCraft.registerTurtleUpgrade(new TurtleUpgradeToolHost());
		ComputerCraft.registerTurtleUpgrade(new TurtleUpgradeToolManipulator());
	}

	@Override
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void registerModels(ModelRegistryEvent event) {
		Helpers.setupModel(this, 0, name);
		Helpers.setupModel(this, 1, "toolHostAdvanced");
	}
}
