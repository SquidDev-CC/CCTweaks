package org.squiddev.cctweaks.core.turtle;

import dan200.computercraft.api.turtle.ITurtleAccess;
import net.minecraft.item.ItemStack;
import org.squiddev.cctweaks.api.turtle.ITurtleFuelProvider;
import org.squiddev.cctweaks.api.turtle.ITurtleFuelRegistry;

import java.util.HashSet;
import java.util.Set;

/**
 * Registry for turtle fuels
 */
public final class TurtleFuelRegistry implements ITurtleFuelRegistry {
	private final Set<ITurtleFuelProvider> providers = new HashSet<ITurtleFuelProvider>();

	@Override
	public void addFuelProvider(ITurtleFuelProvider provider) {
		providers.add(provider);
	}

	@Override
	public ITurtleFuelProvider getProvider(ITurtleAccess turtle, ItemStack stack) {
		for (ITurtleFuelProvider source : providers) {
			if (source.canRefuel(turtle, stack)) {
				return source;
			}
		}

		return null;
	}
}
