package squiddev.cctweaks.crafting;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import squiddev.cctweaks.items.ItemComputerUpgrade;
import dan200.computercraft.api.turtle.TurtleSide;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.items.ComputerItemFactory;
import dan200.computercraft.shared.computer.items.IComputerItem;
import dan200.computercraft.shared.computer.items.ItemComputer;
import dan200.computercraft.shared.pocket.items.ItemPocketComputer;
import dan200.computercraft.shared.pocket.items.PocketComputerItemFactory;
import dan200.computercraft.shared.turtle.items.ItemTurtleBase;
import dan200.computercraft.shared.turtle.items.TurtleItemFactory;

public class ComputerUpgradeCrafting implements IRecipe{

	@Override
	public boolean matches(InventoryCrafting inventorycrafting, World world) {
		int size = inventorycrafting.getSizeInventory();
		boolean containsUpgrade = false;
		boolean containsComputer = false;

		// Find
		for(int i = 0; i < size; i++){
			ItemStack itemStack = inventorycrafting.getStackInSlot(i);
			if(itemStack == null){
				continue;
			}

			Item item = itemStack.getItem();

			if(item instanceof ItemComputerUpgrade){
				containsUpgrade = true;
			}else if(item instanceof IComputerItem){
				IComputerItem computer = (IComputerItem)item;
				if(((IComputerItem) item).getFamily(itemStack) != ComputerFamily.Normal){
					return false;
				}
				containsComputer = true;
			}

			if(containsUpgrade && containsComputer){
				return true;
			}
		}
		return false;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inventorycrafting) {
		int size = inventorycrafting.getSizeInventory();

		ItemStack computerStack = null;

		for(int i = 0; i < size; i++){
			ItemStack itemStack = inventorycrafting.getStackInSlot(i);
			if(itemStack == null){
				continue;
			}

			// If its a 'computer'
			if(itemStack.getItem() instanceof IComputerItem){
				computerStack = itemStack;
				break;
			}
		}

		if(computerStack == null){
			return null;
		}

		IComputerItem computerItem = (IComputerItem)computerStack.getItem();
		int id = computerItem.getComputerID(computerStack);
		String label = computerItem.getLabel(computerStack);
		ComputerFamily family = ComputerFamily.Advanced;

		if(computerItem instanceof ItemComputer){
			return ComputerItemFactory.create(id, label, family);
		}else if(computerItem instanceof ItemTurtleBase){
			ItemTurtleBase turtle = (ItemTurtleBase)computerItem;

			return TurtleItemFactory.create(
					id, label, turtle.getColour(computerStack), family,
					turtle.getUpgrade(computerStack, TurtleSide.Left),
					turtle.getUpgrade(computerStack, TurtleSide.Right),
					turtle.getFuelLevel(computerStack)
					);
		}else if(computerItem instanceof ItemPocketComputer){
			ItemPocketComputer pocket = (ItemPocketComputer)computerItem;

			return PocketComputerItemFactory.create(id, label, family, pocket.getHasModem(computerStack));
		}

		return null;
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
