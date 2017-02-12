package org.squiddev.cctweaks.core.pocket;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.pocket.items.PocketComputerItemFactory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.RecipeSorter;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.api.pocket.IPocketUpgrade;
import org.squiddev.cctweaks.core.registry.Module;

import javax.annotation.Nonnull;

public class CraftingPocketUpgrade extends Module implements IRecipe {
	@Override
	public boolean matches(@Nonnull InventoryCrafting crafting, @Nonnull World worldIn) {
		return !getCraftingResult(crafting).isEmpty();
	}

	@Nonnull
	@Override
	public ItemStack getCraftingResult(@Nonnull InventoryCrafting inventory) {
		IPocketUpgrade upgrade = null;
		ItemStack pocket = null;

		int size = inventory.getSizeInventory();
		for (int i = 0; i < size; i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (stack.isEmpty()) continue;

			if (stack.getItem() == ComputerCraft.Items.pocketComputer) {
				// If we already found a pocket computer, then abort
				if (pocket != null) return ItemStack.EMPTY;

				// If we have an upgrade then abort
				if (stack.hasTagCompound() && stack.getTagCompound().getShort("upgrade") != 0) return ItemStack.EMPTY;

				pocket = stack;
			} else {
				// If we already found an upgrade then abort
				if (upgrade != null) return ItemStack.EMPTY;

				upgrade = PocketRegistry.instance.getFromItemStack(stack);

				// If it isn't a valid one then abort
				if (upgrade == null) return ItemStack.EMPTY;
			}
		}

		if (upgrade == null || pocket == null) return ItemStack.EMPTY;

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

	@Nonnull
	@Override
	public ItemStack getRecipeOutput() {
		return PocketComputerItemFactory.create(-1, null, ComputerFamily.Normal, false);
	}

	@Nonnull
	@Override
	public NonNullList<ItemStack> getRemainingItems(@Nonnull InventoryCrafting inventory) {
		NonNullList<ItemStack> results = NonNullList.withSize(inventory.getSizeInventory(), ItemStack.EMPTY);

		for (int i = 0; i < results.size(); ++i) {
			ItemStack stack = inventory.getStackInSlot(i);
			results.set(i, ForgeHooks.getContainerItem(stack));
		}

		return results;
	}

	@Override
	public void init() {
		RecipeSorter.register(CCTweaks.ID + ":pocket_upgrade_crafting", CraftingPocketUpgrade.class, RecipeSorter.Category.SHAPELESS, "after:minecraft:shapeless");
		GameRegistry.addRecipe(this);
	}

	public static ItemStack setNBT(ItemStack stack, IPocketUpgrade upgrade) {
		NBTTagCompound tag = stack.getTagCompound();
		if (tag == null) stack.setTagCompound(tag = new NBTTagCompound());

		tag.setShort("upgrade", PocketRegistry.FLAG);
		tag.setString("upgrade_name", upgrade.getUpgradeID().toString());
		return stack;
	}
}
