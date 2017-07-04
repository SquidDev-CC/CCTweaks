package org.squiddev.cctweaks.integration;

import dan200.computercraft.api.turtle.ITurtleAccess;
import net.darkhax.tesla.api.ITeslaProducer;
import net.darkhax.tesla.capability.TeslaCapabilities;
import net.minecraft.item.ItemStack;
import org.squiddev.cctweaks.api.CCTweaksAPI;
import org.squiddev.cctweaks.api.turtle.ITurtleFuelProvider;
import org.squiddev.cctweaks.core.Config;

import javax.annotation.Nonnull;

/**
 * Tesla energy provider for turtles
 */
public class TeslaIntegration extends APIIntegration {
	public TeslaIntegration() {
		super("tesla");
	}

	@Override
	public void init() {
		CCTweaksAPI.instance().fuelRegistry().addFuelProvider(new ITurtleFuelProvider() {
			@Override
			public boolean canRefuel(@Nonnull ITurtleAccess turtle, @Nonnull ItemStack stack) {
				return Config.Turtle.fluxRefuelAmount > 0 && stack.hasCapability(TeslaCapabilities.CAPABILITY_PRODUCER, null);
			}

			@Override
			public int refuel(@Nonnull ITurtleAccess turtle, @Nonnull ItemStack stack, int limit) {
				int fluxAmount = Config.Turtle.fluxRefuelAmount;

				// Avoid over refueling
				int maxRefuel = turtle.getFuelLimit() - turtle.getFuelLevel();
				int fluxLimit = (limit >= 64 ? maxRefuel : limit) * fluxAmount;

				ITeslaProducer producer = stack.getCapability(TeslaCapabilities.CAPABILITY_PRODUCER, null);

				long change = 1;
				long progress = 0;
				while (progress < fluxLimit && change > 0) {
					change = producer.takePower(fluxLimit - progress, false);
					progress += change;
				}
				return (int) (progress / fluxAmount);
			}
		});
	}
}

