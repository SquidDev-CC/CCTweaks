package org.squiddev.cctweaks.core.turtle;

import com.google.common.base.Preconditions;
import dan200.computercraft.api.turtle.ITurtleAccess;
import net.minecraft.item.ItemStack;
import org.squiddev.cctweaks.api.turtle.ITurtleFuelProvider;
import org.squiddev.cctweaks.api.turtle.ITurtleFuelRegistry;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

/**
 * Registry for turtle fuels
 */
public final class TurtleFuelRegistry implements ITurtleFuelRegistry {
	private final Set<ITurtleFuelProvider> providers = new HashSet<ITurtleFuelProvider>();

	@Override
	public void addFuelProvider(@Nonnull ITurtleFuelProvider provider) {
		Preconditions.checkNotNull(provider, "provider cannot be null");
		providers.add(provider);
	}

	@Override
	public ITurtleFuelProvider getProvider(@Nonnull ITurtleAccess turtle, @Nonnull ItemStack stack) {
		Preconditions.checkNotNull(turtle, "turtle cannot be null");
		Preconditions.checkNotNull(stack, "stack cannot be null");
		for (ITurtleFuelProvider source : providers) {
			if (source.canRefuel(turtle, stack)) {
				return source;
			}
		}

		return null;
	}
}
