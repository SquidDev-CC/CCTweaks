package squiddev.cctweaks.core.registry;

import cofh.api.energy.IEnergyContainerItem;
import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.shared.util.InventoryUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import squiddev.cctweaks.core.reference.Config;
import squiddev.cctweaks.core.turtle.ITurtleRefuelSource;

import java.util.ArrayList;
import java.util.List;

public class TurtleRefuelList {
	public static final List<ITurtleRefuelSource> refuelList;

	static {
		refuelList = new ArrayList<ITurtleRefuelSource>();

		// Furnace handler
		refuelList.add(new ITurtleRefuelSource() {
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

		if (Config.config.turtleFluxRefuelEnable) {
			// IEnergyContainerItem
			refuelList.add(new ITurtleRefuelSource() {
				@Override
				public boolean canRefuel(ITurtleAccess turtle, ItemStack stack, int limit) {
					return stack.getItem() instanceof IEnergyContainerItem;
				}

				@Override
				public int refuel(ITurtleAccess turtle, ItemStack stack, int limit) {
					int fluxAmount = Config.config.turtleFluxRefuelAmount;

					// Avoid over refueling
					int maxRefuel = turtle.getFuelLimit() - turtle.getFuelLevel();
					int fluxLimit = (limit >= 64 ? maxRefuel : limit) * fluxAmount;

					IEnergyContainerItem container = (IEnergyContainerItem) stack.getItem();

					// Don't want to pull to much
					fluxLimit = Math.min(container.getMaxEnergyStored(stack), fluxLimit);

					int change = 1;
					int progress = 0;
					while (progress < fluxLimit && change > 0) {
						change = container.extractEnergy(stack, fluxLimit - progress, false);
						progress += change;
					}
					return progress / fluxAmount;
				}
			});
		}

	}
}
