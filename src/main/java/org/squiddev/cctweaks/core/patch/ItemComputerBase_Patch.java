package org.squiddev.cctweaks.core.patch;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.items.ComputerItemFactory;
import dan200.computercraft.shared.computer.items.ItemComputerBase;
import dan200.computercraft.shared.turtle.items.ItemTurtleBase;
import dan200.computercraft.shared.turtle.items.TurtleItemFactory;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import org.squiddev.cctweaks.api.IComputerItemFactory;
import org.squiddev.patcher.visitors.MergeVisitor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Implements {@link IComputerItemFactory} on this.
 */
public abstract class ItemComputerBase_Patch extends ItemComputerBase implements IComputerItemFactory {
	@MergeVisitor.Stub
	protected ItemComputerBase_Patch() {
		super(null);
	}

	@Nonnull
	@Override
	public ItemStack createComputer(int id, @Nullable String label, @Nonnull ComputerFamily family) {
		if (ItemTurtleBase.class.isInstance(this)) {
			return TurtleItemFactory.create(id, label, null, family, null, null, 0, null);
		} else {
			return ComputerItemFactory.create(id, label, family);
		}
	}

	@Nonnull
	@Override
	public Set<ComputerFamily> getSupportedFamilies() {
		Block block = getBlock();
		if (block == ComputerCraft.Blocks.commandComputer) {
			return EnumSet.of(ComputerFamily.Command);
		} else if (block == ComputerCraft.Blocks.computer) {
			return EnumSet.of(ComputerFamily.Normal, ComputerFamily.Advanced);
		} else if (block == ComputerCraft.Blocks.turtleExpanded || block == ComputerCraft.Blocks.turtle) {
			return EnumSet.of(ComputerFamily.Normal);
		} else if (block == ComputerCraft.Blocks.turtleAdvanced) {
			return EnumSet.of(ComputerFamily.Advanced);
		} else {
			return Collections.emptySet();
		}
	}

	@Nonnull
	@Override
	public ComputerFamily getDefaultFamily() {
		Block block = getBlock();
		if (block == ComputerCraft.Blocks.commandComputer) {
			return ComputerFamily.Command;
		} else if (block == ComputerCraft.Blocks.computer) {
			return ComputerFamily.Normal;
		} else if (block == ComputerCraft.Blocks.turtleExpanded || block == ComputerCraft.Blocks.turtle) {
			return ComputerFamily.Normal;
		} else if (block == ComputerCraft.Blocks.turtleAdvanced) {
			return ComputerFamily.Advanced;
		} else {
			return ComputerFamily.Normal;
		}
	}
}
