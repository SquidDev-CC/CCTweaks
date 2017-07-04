package org.squiddev.cctweaks.integration;

import dan200.computercraft.api.turtle.ITurtleAccess;
import net.minecraft.item.ItemStack;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import org.squiddev.cctweaks.api.CCTweaksAPI;
import org.squiddev.cctweaks.api.turtle.ITurtleFuelProvider;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.core.registry.IModule;

import javax.annotation.Nonnull;

/**
 * Forge energy provider for turtles
 */
public class ForgeIntegration implements IModule {
	@Override
	public void init() {
		CCTweaksAPI.instance().fuelRegistry().addFuelProvider(new ITurtleFuelProvider() {
			@Override
			public boolean canRefuel(@Nonnull ITurtleAccess turtle, @Nonnull ItemStack stack) {
				return Config.Turtle.fluxRefuelAmount > 0 && stack.hasCapability(CapabilityEnergy.ENERGY, null);
			}

			@Override
			public int refuel(@Nonnull ITurtleAccess turtle, @Nonnull ItemStack stack, int limit) {
				int fluxAmount = Config.Turtle.fluxRefuelAmount;

				// Avoid over refueling
				int maxRefuel = turtle.getFuelLimit() - turtle.getFuelLevel();
				int fluxLimit = (limit >= 64 ? maxRefuel : limit) * fluxAmount;

				IEnergyStorage producer = stack.getCapability(CapabilityEnergy.ENERGY, null);

				int change = 1;
				int progress = 0;
				while (progress < fluxLimit && change > 0) {
					change = producer.extractEnergy(fluxLimit - progress, false);
					progress += change;
				}
				return progress / fluxAmount;
			}
		});
	}
}
