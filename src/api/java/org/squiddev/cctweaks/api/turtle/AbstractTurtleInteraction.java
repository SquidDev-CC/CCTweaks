package org.squiddev.cctweaks.api.turtle;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.TurtleCommandResult;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.util.FakePlayer;

/**
 * Abstract implementation of {@link ITurtleInteraction} with no methods implemented.
 * It is probably a good idea to override at least one of them.
 *
 * @see ITurtleRegistry#registerInteraction(ITurtleInteraction)
 * @see ITurtleRegistry#registerInteraction(Item, ITurtleInteraction)
 */
public abstract class AbstractTurtleInteraction implements ITurtleInteraction {
	@Override
	public TurtleCommandResult swing(ITurtleAccess turtle, IComputerAccess computer, FakePlayer player, ItemStack stack, EnumFacing direction, MovingObjectPosition hit) throws LuaException {
		return null;
	}

	@Override
	public boolean canSwing(ITurtleAccess turtle, FakePlayer player, ItemStack stack, EnumFacing direction, MovingObjectPosition hit) {
		return false;
	}

	@Override
	public TurtleCommandResult use(ITurtleAccess turtle, IComputerAccess computer, FakePlayer player, ItemStack stack, EnumFacing direction, MovingObjectPosition hit) throws LuaException {
		return null;
	}

	@Override
	public boolean canUse(ITurtleAccess turtle, FakePlayer player, ItemStack stack, EnumFacing direction, MovingObjectPosition hit) {
		return false;
	}
}
