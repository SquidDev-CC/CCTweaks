package org.squiddev.cctweaks.api.turtle;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.TurtleCommandResult;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.util.FakePlayer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Add custom interactions to the turtle tool manipulator
 *
 * @see ITurtleRegistry#registerInteraction(ITurtleInteraction)
 * @see ITurtleRegistry#registerInteraction(Item, ITurtleInteraction)
 */
public interface ITurtleInteraction {
	/**
	 * Swing (left click) a tool
	 *
	 * The code is already being run in the main thread, and so you cannot yield.
	 *
	 * @param turtle    The turtle swinging the tool
	 * @param computer  The current computer
	 * @param player    The turtle player,
	 * @param stack     The item to be swung
	 * @param direction The direction to swing in
	 * @param hit       Place the item was used at. May be {@code null}.
	 * @return Result of interaction, or {@code null} if no action could be taken
	 * @throws LuaException If an error occurs
	 */
	@Nullable
	TurtleCommandResult swing(@Nonnull ITurtleAccess turtle, @Nonnull IComputerAccess computer, @Nonnull FakePlayer player, @Nonnull ItemStack stack, @Nonnull EnumFacing direction, @Nullable RayTraceResult hit) throws LuaException;

	/**
	 * Is the tool appropriate for this job
	 *
	 * @param turtle    The turtle swinging the tool
	 * @param player    The turtle player,
	 * @param stack     The item to be swung
	 * @param direction The direction to swing in
	 * @param hit       Place the item was used at. May be {@code null}.
	 * @return If the tool is appropriate: can it attack or dig the block?
	 */
	boolean canSwing(@Nonnull ITurtleAccess turtle, @Nonnull FakePlayer player, @Nonnull ItemStack stack, @Nonnull EnumFacing direction, @Nullable RayTraceResult hit);

	/**
	 * Use (right click) a tool
	 *
	 * The code is already being run in the main thread, and so you cannot yield.
	 *
	 * @param turtle    The turtle swinging the tool
	 * @param computer  The current computer
	 * @param player    The turtle player
	 * @param stack     The item to be swung
	 * @param direction The direction to swing in
	 * @param hit       Place the item was used at. May be {@code null}.
	 * @return Result of interaction, or {@code null} if no action could be taken
	 * @throws LuaException If an error occurs
	 */
	@Nullable
	TurtleCommandResult use(@Nonnull ITurtleAccess turtle, @Nonnull IComputerAccess computer, @Nonnull FakePlayer player, @Nonnull ItemStack stack, @Nonnull EnumFacing direction, @Nullable RayTraceResult hit) throws LuaException;

	/**
	 * Is the tool appropriate for this job
	 *
	 * @param turtle    The turtle swinging the tool
	 * @param player    The turtle player,
	 * @param stack     The item to be swung
	 * @param direction The direction to swing in
	 * @param hit       Place the item was used at. May be {@code null}.
	 * @return If the tool can be right clicked.
	 */
	boolean canUse(@Nonnull ITurtleAccess turtle, @Nonnull FakePlayer player, @Nonnull ItemStack stack, @Nonnull EnumFacing direction, @Nullable RayTraceResult hit);
}
