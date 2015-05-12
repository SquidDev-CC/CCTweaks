package org.squiddev.cctweaks.api.turtle;

import dan200.computercraft.api.turtle.ITurtleAccess;
import net.minecraft.item.ItemStack;

/**
 * Provides a method of refueling turtles to be registered
 * with {@link ITurtleFuelRegistry#addFuelProvider(ITurtleFuelProvider)}
 */
public interface ITurtleFuelProvider {
	/**
	 * Check if the turtle can refuel from this item
	 *
	 * @param turtle The turtle to refuel
	 * @param stack  The fuel to refuel with
	 * @return If a refuel can occur
	 */
	boolean canRefuel(ITurtleAccess turtle, ItemStack stack);

	/**
	 * Refuels the turtle.
	 * This should consume the stack but not add fuel to the turtle.
	 *
	 * @param turtle The turtle to refuel
	 * @param stack  The fuel to refuel with
	 * @param limit  The maximum number of items to consume
	 * @return The fuel added to the turtle
	 */
	int refuel(ITurtleAccess turtle, ItemStack stack, int limit);
}
