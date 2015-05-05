package org.squiddev.cctweaks.integration;

import cofh.api.energy.IEnergyContainerItem;
import dan200.computercraft.api.turtle.ITurtleAccess;
import net.minecraft.item.ItemStack;
import org.squiddev.cctweaks.api.turtle.ITurtleFuelProvider;
import org.squiddev.cctweaks.api.turtle.TurtleFuelRegistry;
import org.squiddev.cctweaks.core.Config;

/**
 * Registers Redstone Flux as a fuel
 */
public class RedstoneFluxIntegration extends APIIntegration {
	public RedstoneFluxIntegration() {
		super("CoFHAPI|energy");
	}

	@Override
	public void preInit() {
	}

	@Override
	public void init() {
		TurtleFuelRegistry.addFuelProvider(new ITurtleFuelProvider() {
			@Override
			public boolean canRefuel(ITurtleAccess turtle, ItemStack stack, int limit) {
				return Config.config.turtleFluxRefuelEnable && stack.getItem() instanceof IEnergyContainerItem;
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
