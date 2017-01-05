package org.squiddev.cctweaks.core.turtle;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.TurtleCommandResult;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.util.FakePlayer;
import org.squiddev.cctweaks.api.turtle.ITurtleInteraction;
import org.squiddev.cctweaks.api.turtle.ITurtleRegistry;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class TurtleRegistry implements ITurtleRegistry, ITurtleInteraction {
	public static final TurtleRegistry instance = new TurtleRegistry();

	private final Multimap<Item, ITurtleInteraction> itemInteractions = MultimapBuilder.hashKeys().arrayListValues().build();
	private final List<ITurtleInteraction> genericInteractions = new ArrayList<ITurtleInteraction>();

	@Override
	public void registerInteraction(@Nonnull ITurtleInteraction interaction) {
		Preconditions.checkNotNull(interaction, "interaction cannot be null");
		genericInteractions.add(interaction);
	}

	@Override
	public void registerInteraction(@Nonnull Item item, @Nonnull ITurtleInteraction interaction) {
		Preconditions.checkNotNull(item, "item cannot be null");
		Preconditions.checkNotNull(interaction, "interaction cannot be null");

		itemInteractions.put(item, interaction);
	}

	@Override
	public TurtleCommandResult swing(@Nonnull ITurtleAccess turtle, @Nonnull IComputerAccess computer, @Nonnull FakePlayer player, @Nonnull ItemStack stack, @Nonnull EnumFacing direction, MovingObjectPosition hit) throws LuaException {
		for (ITurtleInteraction interaction : itemInteractions.get(stack.getItem())) {
			TurtleCommandResult result = interaction.swing(turtle, computer, player, stack, direction, hit);
			if (result != null) return result;
		}

		for (ITurtleInteraction interaction : genericInteractions) {
			TurtleCommandResult result = interaction.swing(turtle, computer, player, stack, direction, hit);
			if (result != null) return result;
		}

		return null;
	}

	@Override
	public TurtleCommandResult use(@Nonnull ITurtleAccess turtle, @Nonnull IComputerAccess computer, @Nonnull FakePlayer player, @Nonnull ItemStack stack, @Nonnull EnumFacing direction, MovingObjectPosition hit) throws LuaException {
		for (ITurtleInteraction interaction : itemInteractions.get(stack.getItem())) {
			TurtleCommandResult result = interaction.use(turtle, computer, player, stack, direction, hit);
			if (result != null) return result;
		}

		for (ITurtleInteraction interaction : genericInteractions) {
			TurtleCommandResult result = interaction.use(turtle, computer, player, stack, direction, hit);
			if (result != null) return result;
		}

		return null;
	}

	@Override
	public boolean canUse(@Nonnull ITurtleAccess turtle, @Nonnull FakePlayer player, @Nonnull ItemStack stack, @Nonnull EnumFacing direction, MovingObjectPosition hit) {
		for (ITurtleInteraction interaction : itemInteractions.get(stack.getItem())) {
			boolean result = interaction.canUse(turtle, player, stack, direction, hit);
			if (result) return true;
		}

		for (ITurtleInteraction interaction : genericInteractions) {
			boolean result = interaction.canUse(turtle, player, stack, direction, hit);
			if (result) return true;
		}

		return false;
	}

	@Override
	public boolean canSwing(@Nonnull ITurtleAccess turtle, @Nonnull FakePlayer player, @Nonnull ItemStack stack, @Nonnull EnumFacing direction, MovingObjectPosition hit) {
		for (ITurtleInteraction interaction : itemInteractions.get(stack.getItem())) {
			boolean result = interaction.canSwing(turtle, player, stack, direction, hit);
			if (result) return true;
		}

		for (ITurtleInteraction interaction : genericInteractions) {
			boolean result = interaction.canSwing(turtle, player, stack, direction, hit);
			if (result) return true;
		}

		return false;
	}
}
