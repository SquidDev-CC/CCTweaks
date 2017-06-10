package org.squiddev.cctweaks.items;

import dan200.computercraft.api.turtle.TurtleSide;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.items.ComputerItemFactory;
import dan200.computercraft.shared.computer.items.IComputerItem;
import dan200.computercraft.shared.computer.items.ItemComputer;
import dan200.computercraft.shared.pocket.items.ItemPocketComputer;
import dan200.computercraft.shared.pocket.items.PocketComputerItemFactory;
import dan200.computercraft.shared.turtle.items.ItemTurtleBase;
import dan200.computercraft.shared.turtle.items.TurtleItemFactory;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import org.squiddev.cctweaks.core.rom.CraftingSetRom;

import javax.annotation.Nonnull;

/**
 * Handles crafting with ComputerUpgrades
 */
public class CraftingComputerUpgrade implements IRecipe {

	@Override
	public boolean matches(@Nonnull InventoryCrafting inventorycrafting, @Nonnull World world) {
		return getComputerStack(inventorycrafting) != null;
	}

	@Nonnull
	@Override
	public ItemStack getCraftingResult(@Nonnull InventoryCrafting crafting) {
		ItemStack computerStack = getComputerStack(crafting);
		if (computerStack == null) return ItemStack.EMPTY;

		IComputerItem computerItem = (IComputerItem) computerStack.getItem();

		// Get the current data
		int id = computerItem.getComputerID(computerStack);
		String label = computerItem.getLabel(computerStack);

		ItemStack stack;
		if (computerItem instanceof ItemComputer) {
			stack = ComputerItemFactory.create(id, label, ComputerFamily.Advanced);
		} else if (computerItem instanceof ItemTurtleBase) {
			ItemTurtleBase turtle = (ItemTurtleBase) computerItem;

			stack = TurtleItemFactory.create(
				id, label, turtle.getColour(computerStack), ComputerFamily.Advanced,
				turtle.getUpgrade(computerStack, TurtleSide.Left),
				turtle.getUpgrade(computerStack, TurtleSide.Right),
				turtle.getFuelLevel(computerStack), null
			);
		} else if (computerItem instanceof ItemPocketComputer) {
			ItemPocketComputer pocket = (ItemPocketComputer) computerItem;
			stack = PocketComputerItemFactory.create(id, label, pocket.getColour(computerStack), ComputerFamily.Advanced, pocket.getUpgrade(computerStack));

			NBTTagCompound toTag = ItemBase.getTag(stack);
			NBTTagCompound fromTag = ItemBase.getTag(computerStack);

			if (fromTag.hasKey("upgrade")) toTag.setTag("upgrade", fromTag.getTag("upgrade"));
			if (fromTag.hasKey("upgrade_name")) toTag.setTag("upgrade_name", fromTag.getTag("upgrade_name"));
			if (fromTag.hasKey("upgrade_info")) toTag.setTag("upgrade_info", fromTag.getTag("upgrade_info"));
		} else {
			return ItemStack.EMPTY;
		}

		return CraftingSetRom.copyRom(stack, computerStack);
	}

	/**
	 * Find the ItemStack for the computer
	 *
	 * @param crafting The crafting inventory space
	 * @return The ItemStack with a computer type in, or null if the recipe doesn't match
	 */
	private ItemStack getComputerStack(InventoryCrafting crafting) {
		int size = crafting.getSizeInventory();

		ItemStack computerStack = null;
		boolean hasUpgrade = false;
		int gold = 0;

		for (int i = 0; i < size; i++) {
			ItemStack itemStack = crafting.getStackInSlot(i);
			if (itemStack.isEmpty()) {
				continue;
			}

			Item item = itemStack.getItem();
			if (item == Items.GOLD_INGOT) {
				gold++;
			} else if (item instanceof IComputerItem) {
				// If its a 'computer'
				// Don't allow more than one computer and it must be a normal computer
				if (computerStack != null || ((IComputerItem) item).getFamily(itemStack) != ComputerFamily.Normal) {
					return null;
				}

				computerStack = itemStack;
			} else if (item instanceof ItemComputerUpgrade) {
				// Don't allow more than one upgrade
				if (hasUpgrade) return null;

				hasUpgrade = true;
			}
		}

		if (computerStack == null || !hasUpgrade) {
			return null;
		} else if (computerStack.getItem() instanceof ItemTurtleBase && gold < 7) {
			return null;
		} else {
			return computerStack;
		}
	}

	@Override
	public int getRecipeSize() {
		return 2;
	}

	@Nonnull
	@Override
	public ItemStack getRecipeOutput() {
		return ComputerItemFactory.create(-1, null, ComputerFamily.Advanced);
	}

	@Nonnull
	@Override
	public NonNullList<ItemStack> getRemainingItems(@Nonnull InventoryCrafting inventory) {
		NonNullList<ItemStack> result = NonNullList.withSize(inventory.getSizeInventory(), ItemStack.EMPTY);

		for (int i = 0; i < result.size(); ++i) {
			result.set(i, ForgeHooks.getContainerItem(inventory.getStackInSlot(i)));
		}

		return result;
	}
}
