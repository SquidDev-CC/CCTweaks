package org.squiddev.cctweaks.core.registry;

import dan200.computercraft.api.turtle.TurtleSide;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.items.ComputerItemFactory;
import dan200.computercraft.shared.computer.items.IComputerItem;
import dan200.computercraft.shared.computer.items.ItemComputer;
import dan200.computercraft.shared.pocket.items.ItemPocketComputer;
import dan200.computercraft.shared.pocket.items.PocketComputerItemFactory;
import dan200.computercraft.shared.turtle.items.ItemTurtleBase;
import dan200.computercraft.shared.turtle.items.TurtleItemFactory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import org.squiddev.cctweaks.core.items.ComputerUpgradeItem;

/**
 * Handles crafting with ComputerUpgrades
 */
public class ComputerUpgradeCrafting implements IRecipe {

	@Override
	public boolean matches(InventoryCrafting inventorycrafting, World world) {
		return getComputerStack(inventorycrafting) != null;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inventorycrafting) {
		ItemStack computerStack = getComputerStack(inventorycrafting);
		if (computerStack == null) return null;

		IComputerItem computerItem = (IComputerItem) computerStack.getItem();

		// Get the current data
		int id = computerItem.getComputerID(computerStack);
		String label = computerItem.getLabel(computerStack);
		ComputerFamily family = ComputerFamily.Advanced;

		if (computerItem instanceof ItemComputer) {
			return ComputerItemFactory.create(id, label, family);
		} else if (computerItem instanceof ItemTurtleBase) {
			ItemTurtleBase turtle = (ItemTurtleBase) computerItem;

			return TurtleItemFactory.create(
				id, label, turtle.getColour(computerStack), family,
				turtle.getUpgrade(computerStack, TurtleSide.Left),
				turtle.getUpgrade(computerStack, TurtleSide.Right),
				turtle.getFuelLevel(computerStack)
			);
		} else if (computerItem instanceof ItemPocketComputer) {
			ItemPocketComputer pocket = (ItemPocketComputer) computerItem;

			return PocketComputerItemFactory.create(id, label, family, pocket.getHasModem(computerStack));
		}

		return null;
	}

	/**
	 * Find the ItemStack for the computer
	 *
	 * @param crafting The crafting inventory space
	 * @return The ItemStack with a computer type in, or null if the recipe doesn't match
	 */
	protected ItemStack getComputerStack(InventoryCrafting crafting) {
		int size = crafting.getSizeInventory();

		ItemStack computerStack = null;
		boolean hasUpgrade = false;

		for (int i = 0; i < size; i++) {
			ItemStack itemStack = crafting.getStackInSlot(i);
			if (itemStack == null) {
				continue;
			}

			Item item = itemStack.getItem();

			// If its a 'computer'
			if (item instanceof IComputerItem) {
				// Don't allow more than one computer and it must be a normal computer
				if (computerStack != null || ((IComputerItem) item).getFamily(itemStack) != ComputerFamily.Normal) {
					return null;
				}

				computerStack = itemStack;
			} else if (item instanceof ComputerUpgradeItem) {
				// Don't allow more than one upgrade
				if (hasUpgrade) return null;

				hasUpgrade = true;
			}
		}

		// Don't return computer stack
		return hasUpgrade ? computerStack : null;
	}

	@Override
	public int getRecipeSize() {
		return 2;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return null;
	}
}
