package org.squiddev.cctweaks.integration.jei;

import com.google.common.collect.Lists;
import dan200.computercraft.api.turtle.ITurtleUpgrade;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.turtle.items.TurtleItemFactory;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

public class TurtleUpgradeWrapper extends BlankRecipeWrapper {
	private static final ComputerFamily[] FAMILIES = new ComputerFamily[]{
		ComputerFamily.Normal, ComputerFamily.Advanced,
	};

	private final ITurtleUpgrade upgrade;

	public TurtleUpgradeWrapper(ITurtleUpgrade upgrade) {
		this.upgrade = upgrade;
	}

	@Nonnull
	@Override
	public List<?> getInputs() {
		List<ItemStack> turtles = Lists.newArrayListWithExpectedSize(FAMILIES.length);
		for (ComputerFamily family : FAMILIES) {
			turtles.add(TurtleItemFactory.create(-1, null, null, family, null, null, 0, null));
		}

		return Arrays.asList(upgrade.getCraftingItem(), turtles);
	}

	@Nonnull
	@Override
	public List<?> getOutputs() {
		List<ItemStack> turtles = Lists.newArrayListWithExpectedSize(FAMILIES.length);
		for (ComputerFamily family : FAMILIES) {
			turtles.add(TurtleItemFactory.create(-1, null, null, family, upgrade, null, 0, null));
		}

		return turtles;
	}
}
