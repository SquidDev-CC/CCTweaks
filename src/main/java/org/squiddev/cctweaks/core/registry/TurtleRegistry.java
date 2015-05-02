package org.squiddev.cctweaks.core.registry;

import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.shared.util.InventoryUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import org.squiddev.cctweaks.api.turtle.ITurtleFuelProvider;
import org.squiddev.cctweaks.api.turtle.TurtleFuelRegistry;

/**
 * Registers turtle related things
 */
public class TurtleRegistry implements IModule {
	@Override
	public boolean canLoad() {
		return true;
	}

	@Override
	public void preInit() {
	}

	public void init() {
		// Add default furnace fuel provider
		TurtleFuelRegistry.addFuelProvider(new ITurtleFuelProvider() {
			@Override
			public boolean canRefuel(ITurtleAccess turtle, ItemStack stack, int limit) {
				return TileEntityFurnace.isItemFuel(stack);
			}

			@Override
			public int refuel(ITurtleAccess turtle, ItemStack stack, int limit) {
				int fuelToGive = TileEntityFurnace.getItemBurnTime(stack) * 5 / 100 * limit;
				ItemStack replacementStack = stack.getItem().getContainerItem(stack);

				if (replacementStack != null) {
					// If item is empty (bucket) then replace
					InventoryUtil.storeItems(replacementStack, turtle.getInventory(), 0, turtle.getInventory().getSizeInventory(), turtle.getSelectedSlot());
				} else {
					// Else we just remove 'n' items from the stack.
					InventoryUtil.takeItems(limit, turtle.getInventory(), 0, turtle.getInventory().getSizeInventory(), turtle.getSelectedSlot());
				}

				return fuelToGive;
			}
		});
	}
}
