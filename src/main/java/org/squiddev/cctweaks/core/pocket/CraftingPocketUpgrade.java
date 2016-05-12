package org.squiddev.cctweaks.core.pocket;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.pocket.items.PocketComputerItemFactory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.RecipeSorter;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.api.pocket.IPocketUpgrade;
import org.squiddev.cctweaks.core.registry.Module;

public class CraftingPocketUpgrade extends Module implements IRecipe {
	@Override
	public boolean matches(InventoryCrafting crafting, World worldIn) {
		return getCraftingResult(crafting) != null;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inventory) {
		IPocketUpgrade upgrade = null;
		ItemStack pocket = null;

		int size = inventory.getSizeInventory();
		for (int i = 0; i < size; i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (stack == null) continue;

			if (stack.getItem() == ComputerCraft.Items.pocketComputer) {
				// If we already found a pocket computer, then abort
				if (pocket != null) return null;

				// If we have an upgrade then abort
				if (stack.hasTagCompound() && stack.getTagCompound().getShort("upgrade") != 0) return null;

				pocket = stack;
			} else {
				// If we already found an upgrade then abort
				if (upgrade != null) return null;

				upgrade = PocketRegistry.instance.getFromItemStack(stack);

				// If it isn't a valid one then abort
				if (upgrade == null) return null;
			}
		}

		if (upgrade == null || pocket == null) return null;

		pocket = pocket.copy();
		if (!pocket.hasTagCompound()) pocket.setTagCompound(new NBTTagCompound());
		NBTTagCompound tag = pocket.getTagCompound();
		tag.setShort("upgrade", PocketRegistry.FLAG);
		tag.setString("upgrade_name", upgrade.getUpgradeID().toString());

		// Ensure a new computer is created
		tag.removeTag("instanceID");
		tag.removeTag("sessionID");

		return pocket;
	}

	@Override
	public int getRecipeSize() {
		return 2;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return PocketComputerItemFactory.create(-1, null, ComputerFamily.Normal, false);
	}

	@Override
	public ItemStack[] getRemainingItems(InventoryCrafting inventory) {
		ItemStack[] results = new ItemStack[inventory.getSizeInventory()];

		for (int i = 0; i < results.length; ++i) {
			ItemStack stack = inventory.getStackInSlot(i);
			results[i] = ForgeHooks.getContainerItem(stack);
		}

		return results;
	}

	@Override
	public void init() {
		RecipeSorter.register(CCTweaks.RESOURCE_DOMAIN + ":pocket_upgrade_crafting", CraftingPocketUpgrade.class, RecipeSorter.Category.SHAPELESS, "after:minecraft:shapeless");
		GameRegistry.addRecipe(this);
	}
}
